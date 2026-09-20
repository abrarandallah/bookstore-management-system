"""
Retries LibreTranslate on every Arabic book/page field that still
contains Latin letters (REGEXP '[a-zA-Z]') - i.e. the "never actually
translated" failure mode, as opposed to the separate "translated into
meaningless phonetic gibberish" problem, which a retry can't fix since
the engine already "succeeded" the first time.

Adapted from translate_books.py - same connection pattern, same
LibreTranslate call - but instead of only filling in rows that have NO
translation yet, this looks for existing Arabic rows that still read
like English, and only overwrites a field if the retry comes back
without any Latin letters. If a retry still contains Latin letters (or
LibreTranslate fails outright), the old value is left untouched and
printed out so you know it still needs a human - never silently
replaced with an equally-bad result.

Safe to re-run: whatever still fails just gets picked up again next
time you run it.

Requires the same running LibreTranslate container as before, on the
same Docker network as the app:

    docker run -d --name libretranslate \
        --network bookstore-management-system_default \
        -p 5000:5000 libretranslate/libretranslate --load-only en,ar,fr

(only needed if it's not still running - check with `docker ps` first)

Usage (from the project root, one line, LibreTranslate already up):

    docker run --rm --network bookstore-management-system_default -v "${PWD}:/app" -w /app python:3.12-slim sh -c "pip install -q pymysql requests && python -u retry_untranslated.py"
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
LANG = "ar"  # this pass is Arabic-only - that's the only language flagged so far


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


def still_has_latin_letters(text):
    return any("a" <= c.lower() <= "z" for c in text)


def retry_field(cur, conn, label, select_sql, update_sql):
    cur.execute(select_sql, (LANG,))
    rows = cur.fetchall()
    print(f"\n{len(rows)} {label} still untranslated. Retrying...")
    fixed = 0
    for row_id, english_text in rows:
        translated = translate_text(english_text, LANG)
        if translated is None:
            print(f"  id {row_id}: SKIPPED (LibreTranslate failed repeatedly)")
            continue
        if still_has_latin_letters(translated):
            print(f"  id {row_id}: retried, still not translated -> keeping old value (needs a human)")
            continue
        cur.execute(update_sql, (translated, row_id))
        conn.commit()
        fixed += 1
        preview = translated[:50] + ("..." if len(translated) > 50 else "")
        print(f"  id {row_id}: fixed -> {preview}")
    print(f"  {fixed}/{len(rows)} {label} fixed by the retry.")


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
            retry_field(
                cur, conn, "book name(s)",
                """
                SELECT bt.id, b.name
                FROM book b
                JOIN book_translations bt ON bt.book_id = b.id AND bt.language = %s
                WHERE bt.name REGEXP '[a-zA-Z]'
                """,
                "UPDATE book_translations SET name = %s WHERE id = %s",
            )
            retry_field(
                cur, conn, "page heading(s)",
                """
                SELECT pt.id, p.heading
                FROM book_page p
                JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
                WHERE pt.heading REGEXP '[a-zA-Z]'
                """,
                "UPDATE book_page_translations SET heading = %s WHERE id = %s",
            )
            retry_field(
                cur, conn, "content passage(s)",
                """
                SELECT pt.id, p.content
                FROM book_page p
                JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
                WHERE pt.content REGEXP '[a-zA-Z]'
                """,
                "UPDATE book_page_translations SET content = %s WHERE id = %s",
            )
        print("\nDone. Anything still flagged above needs a human fix, not another retry.")
    finally:
        conn.close()


if __name__ == "__main__":
    main()