"""
ONE-TIME diagnostic script - delete after use.

Dumps EVERY current Arabic book name and page heading next to its
English source, into arabic_review_dump.json, so the translations can
be read and judged for quality (bad word choice, broken grammar,
English place names transliterated instead of translated, etc.).
No regex can catch those, which is why this dumps everything instead
of filtering.

The "id" in each entry is the book_translations /
book_page_translations row id (what an UPDATE ... WHERE id = %s needs).

Usage (from the project root, one line):

    docker run --rm --network bookstore-management-system_default -v "${PWD}:/app" -w /app python:3.12-slim sh -c "pip install -q pymysql && python -u dump_arabic_review.py"

Add "--content" after the script name to also dump the (much larger)
page content passages:

    ... python -u dump_arabic_review.py --content
"""

import json
import os
import sys

import pymysql

DB_HOST = os.environ.get("DB_HOST", "mysql")
DB_USER = os.environ.get("DB_USER", "root")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "abrar")
DB_NAME = os.environ.get("DB_NAME", "book")
LANG = "ar"


def fetch(cur, sql):
    cur.execute(sql, (LANG,))
    return [{"id": r[0], "english": r[1], "arabic": r[2]} for r in cur.fetchall()]


def main():
    include_content = "--content" in sys.argv
    print("Connecting to database...", flush=True)
    conn = pymysql.connect(
        host=DB_HOST, user=DB_USER, password=DB_PASSWORD, database=DB_NAME,
        charset="utf8mb4", connect_timeout=15,
    )
    print("Connected.", flush=True)

    try:
        with conn.cursor() as cur:
            data = {
                "book_names": fetch(cur, """
                    SELECT bt.id, b.name, bt.name
                    FROM book b
                    JOIN book_translations bt ON bt.book_id = b.id AND bt.language = %s
                    ORDER BY bt.id
                """),
                "page_headings": fetch(cur, """
                    SELECT pt.id, p.heading, pt.heading
                    FROM book_page p
                    JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
                    ORDER BY pt.id
                """),
            }
            if include_content:
                data["page_content"] = fetch(cur, """
                    SELECT pt.id, p.content, pt.content
                    FROM book_page p
                    JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = %s
                    ORDER BY pt.id
                """)
    finally:
        conn.close()

    with open("arabic_review_dump.json", "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)

    print("\nWrote arabic_review_dump.json:")
    for key, rows in data.items():
        print(f"  {len(rows)} {key}")


if __name__ == "__main__":
    main()