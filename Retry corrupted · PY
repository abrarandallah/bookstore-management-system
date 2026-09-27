"""
Fixes the OTHER corruption pattern find_corrupted_translations.sql finds:
rows where the Arabic text is literal "?" characters (an encoding failure
at write time), as opposed to retry_untranslated.py's pattern (rows that
still contain Latin letters, i.e. never got translated at all). Same
connection pattern and LibreTranslate call as that script - just a
different WHERE clause and a different "did this actually get fixed"
check (here: the retry result shouldn't still contain "??", not "shouldn't
contain Latin letters").

Safe to re-run: anything that still fails is left untouched and printed,
never silently overwritten with more garbage.

Requires the same running LibreTranslate container as translate_books.py /
retry_untranslated.py - see retry_untranslated.py's own header comment if
it's not already running.

Usage (from the project root, one line, LibreTranslate already up):

    docker run --rm --network bookstore-management-system_default -v "${PWD}:/app" -w /app python:3.12-slim sh -c "pip install -q pymysql requests && python -u retry_corrupted.py"
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
LANG = "ar"


def translate_text(text, target_lang):
    for attempt in range(4):
        try:
            response = requests.post(
                LIBRETRANSLATE_URL,
                json={"q": text, "source": "en", "target": target_lang, "format": "text"},
                timeout=120,
            )
            response.raise_for_status()
            return response.json()["translatedText"]
        except Exception as e:
            print(f"    (attempt {attempt + 1}/4 failed: {e})")
            time.sleep(5)
    return None


def still_corrupted(text):
    return "??" in text


def retry_field(cur, conn, label, select_sql, update_sql):
    cur.execute(select_sql, (LANG,))
    rows = cur.fetchall()
    print(f"\n{len(rows)} {label} still corrupted (repeated '?'). Retrying...")
    fixed = 0
    for row_id, english_text in rows:
        translated = translate_text(english_text, LANG)
        if translated is None:
            print(f"  id {row_id}: SKIPPED (LibreTranslate failed repeatedly)")
            continue
        if still_corrupted(translated):
            print(f"  id {row_id}: retried, still corrupted -> keeping old value (needs a human)")
            continue
        cur.execute(update_sql, (translated, row_id))
        conn.commit()
        fixed += 1
        preview = translated[:50] + ("..." if len(translated) > 50 else "")
        print(f"  id {row_id}: fixed -> {preview}")
    print(f"  {fixed}/{len(rows)} {label} fixed by the retry.")


NAME_QUERY = r"""
SELECT bt.id, b.name
FROM book b
JOIN book_translations bt ON bt.book_id = b.id AND bt.language = %s
WHERE bt.name REGEXP '\?{2,}'
"""

AUTHOR_QUERY = r"""
SELECT bt.id, b.author
FROM book b
JOIN book_translations bt ON bt.book_id = b.id AND bt.language = %s
WHERE bt.author REGEXP '\?{2,}'
"""

HEADING_QUERY = r"""
SELECT pt.id, p.heading
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
WHERE pt.heading REGEXP '\?{2,}'
"""

CONTENT_QUERY = r"""
SELECT pt.id, p.content
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
WHERE pt.content REGEXP '\?{2,}'
"""


def main():
    print("Connecting to database...", flush=True)
    conn = pymysql.connect(
        host=DB_HOST, user=DB_USER, password=DB_PASSWORD, database=DB_NAME,
        charset="utf8mb4", connect_timeout=15,
    )
    conn.autocommit(False)
    print("Connected.", flush=True)

    try:
        with conn.cursor() as cur:
            retry_field(cur, conn, "book name(s)", NAME_QUERY,
                        "UPDATE book_translations SET name = %s WHERE id = %s")
            retry_field(cur, conn, "book author(s)", AUTHOR_QUERY,
                        "UPDATE book_translations SET author = %s WHERE id = %s")
            retry_field(cur, conn, "page heading(s)", HEADING_QUERY,
                        "UPDATE book_page_translations SET heading = %s WHERE id = %s")
            retry_field(cur, conn, "page content(s)", CONTENT_QUERY,
                        "UPDATE book_page_translations SET content = %s WHERE id = %s")
    finally:
        conn.close()
        print("\nDone. Re-run find_corrupted_translations.sql to confirm nothing '??' is left.")


if __name__ == "__main__":
    main()