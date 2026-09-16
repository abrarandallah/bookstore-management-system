"""
One-time (or re-runnable) batch translator for the BookStore app.

Finds every book/book_page that doesn't yet have a translation for the
target languages below, sends the English text to a self-hosted
LibreTranslate instance, and inserts the result into book_translations /
book_page_translations. Safe to re-run: already-translated rows are
skipped (matches BookTranslationService's own "per-book, per-page,
partial coverage is fine" design), and new books added later just get
picked up on the next run.

Requires a running LibreTranslate container on the same Docker network
as the app (no API key, no account, fully free):

    docker run -d --name libretranslate \
        --network bookstore-management-system_default \
        -p 5000:5000 libretranslate/libretranslate --load-only en,ar,fr

Usage (from the project root, with both the app's docker-compose network
and the libretranslate container above already up):

    docker run --rm --network bookstore-management-system_default \
        -v "${PWD}:/app" -w /app \
        python:3.12-slim \
        sh -c "pip install -q pymysql requests && python -u translate_books.py"

Environment variables:
    LIBRETRANSLATE_URL (default: http://libretranslate:5000/translate)
    DB_HOST         (default: mysql)
    DB_USER         (default: root)
    DB_PASSWORD     (default: abrar - matches docker-compose.yml's default)
    DB_NAME         (default: book)
    TARGET_LANGUAGES (default: ar,fr)
"""

import os
import time

import pymysql
import requests

LIBRETRANSLATE_URL = os.environ.get("LIBRETRANSLATE_URL", "http://libretranslate:5000/translate")
DB_HOST = os.environ.get("DB_HOST", "mysql")
DB_USER = os.environ.get("DB_USER", "root")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "abrar")
DB_NAME = os.environ.get("DB_NAME", "book")
TARGET_LANGUAGES = os.environ.get("TARGET_LANGUAGES", "ar,fr").split(",")


def translate_text(text, target_lang):
    """Translates a single string via the local LibreTranslate instance.
    Retries a few times on transient failure (timeouts under load are
    common with a single small self-hosted instance); returns None if
    every attempt fails, so the caller can skip this row rather than
    crash the whole run."""
    for attempt in range(4):
        try:
            response = requests.post(
                LIBRETRANSLATE_URL,
                json={
                    "q": text,
                    "source": "en",
                    "target": target_lang,
                    "format": "text",
                },
                timeout=120,
            )
            response.raise_for_status()
            return response.json()["translatedText"]
        except Exception as e:
            print(f"    (translation attempt {attempt + 1}/4 failed: {e})")
            time.sleep(5)
    return None


def main():
    print("Connecting to database...", flush=True)
    conn = pymysql.connect(
        host=DB_HOST,
        user=DB_USER,
        password=DB_PASSWORD,
        database=DB_NAME,
        charset="utf8mb4",
        connect_timeout=15,
    )
    conn.autocommit(False)
    print("Connected.", flush=True)

    try:
        with conn.cursor() as cur:
            for lang in TARGET_LANGUAGES:
                lang = lang.strip()
                print(f"\n=== Translating into '{lang}' ===")

                # --- Books ---
                cur.execute(
                    """
                    SELECT b.id, b.name, b.author
                    FROM book b
                    LEFT JOIN book_translations bt
                        ON bt.book_id = b.id AND bt.language = %s
                    WHERE bt.id IS NULL
                    """,
                    (lang,),
                )
                books = cur.fetchall()
                print(f"{len(books)} book(s) need a '{lang}' translation.")

                skipped_books = 0
                for book_id, name, author in books:
                    translated_name = translate_text(name, lang)
                    translated_author = translate_text(author, lang)
                    if translated_name is None or translated_author is None:
                        print(f"  book {book_id}: SKIPPED (translation failed repeatedly, will retry on next run)")
                        skipped_books += 1
                        continue
                    cur.execute(
                        """
                        INSERT IGNORE INTO book_translations (book_id, language, name, author)
                        VALUES (%s, %s, %s, %s)
                        """,
                        (book_id, lang, translated_name, translated_author),
                    )
                    print(f"  book {book_id}: '{name}' -> '{translated_name}'")
                    conn.commit()
                if skipped_books:
                    print(f"  ({skipped_books} book(s) skipped - rerun the script later to retry them)")

                # --- Book pages (takeaways) ---
                cur.execute(
                    """
                    SELECT p.id, p.heading, p.content
                    FROM book_page p
                    LEFT JOIN book_page_translations pt
                        ON pt.book_page_id = p.id AND pt.language = %s
                    WHERE pt.id IS NULL
                    """,
                    (lang,),
                )
                pages = cur.fetchall()
                print(f"{len(pages)} page(s) need a '{lang}' translation.")

                skipped_pages = 0
                for page_id, heading, content in pages:
                    translated_heading = translate_text(heading, lang)
                    translated_content = translate_text(content, lang)
                    if translated_heading is None or translated_content is None:
                        print(f"  page {page_id}: SKIPPED (translation failed repeatedly, will retry on next run)")
                        skipped_pages += 1
                        continue
                    cur.execute(
                        """
                        INSERT IGNORE INTO book_page_translations (book_page_id, language, heading, content)
                        VALUES (%s, %s, %s, %s)
                        """,
                        (page_id, lang, translated_heading, translated_content),
                    )
                    print(f"  page {page_id}: '{heading[:40]}...' -> '{translated_heading[:40]}...'")
                    conn.commit()
                if skipped_pages:
                    print(f"  ({skipped_pages} page(s) skipped - rerun the script later to retry them)")

        print("\nDone.")
    finally:
        conn.close()


if __name__ == "__main__":
    main()