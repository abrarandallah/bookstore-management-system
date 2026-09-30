"""
Dumps every book_page_translations row (language='ar') alongside its
English source, as JSON, for manual read-through quality review.

Deliberately writes to a file rather than printing to console - PowerShell
mangles Arabic text on screen, so the file must be attached to chat (or
opened in a proper UTF-8-aware editor) rather than pasted as terminal
output.

Usage (from the project root, with the app's docker-compose network up):

    docker run --rm --network bookstore-management-system_default \
        -v "${PWD}:/app" -w /app \
        python:3.12-slim \
        sh -c "pip install -q pymysql && python dump_arabic_review.py --content"

Writes to: arabic_review_dump.json

Environment variables:
    DB_HOST     (default: mysql)
    DB_USER     (default: root)
    DB_PASSWORD (default: abrar)
    DB_NAME     (default: book)
"""

import argparse
import json
import os

import pymysql

DB_HOST = os.environ.get("DB_HOST", "mysql")
DB_USER = os.environ.get("DB_USER", "root")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "abrar")
DB_NAME = os.environ.get("DB_NAME", "book")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--content", action="store_true",
        help="Dump book_page content passages (default: dump names/headings instead)",
    )
    args = parser.parse_args()

    conn = pymysql.connect(
        host=DB_HOST, user=DB_USER, password=DB_PASSWORD,
        database=DB_NAME, charset="utf8mb4",
    )

    try:
        with conn.cursor() as cur:
            if args.content:
                cur.execute(
                    """
                    SELECT pt.id, p.book_id, p.id AS page_id, p.content AS english, pt.content AS arabic
                    FROM book_page p
                    JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
                    ORDER BY p.book_id, p.id
                    """
                )
                rows = cur.fetchall()
                data = [
                    {"translation_id": r[0], "book_id": r[1], "page_id": r[2], "english": r[3], "arabic": r[4]}
                    for r in rows
                ]
                out_file = "arabic_review_dump.json"
            else:
                cur.execute(
                    """
                    SELECT t.id, b.id AS book_id, b.name AS english_name, t.name AS arabic_name,
                           b.author AS english_author, t.author AS arabic_author
                    FROM book b
                    JOIN book_translations t ON t.book_id = b.id AND t.language = 'ar'
                    ORDER BY b.id
                    """
                )
                rows = cur.fetchall()
                data = [
                    {
                        "translation_id": r[0], "book_id": r[1],
                        "english_name": r[2], "arabic_name": r[3],
                        "english_author": r[4], "arabic_author": r[5],
                    }
                    for r in rows
                ]
                out_file = "arabic_review_dump.json"

        with open(out_file, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

        print(f"Wrote {len(data)} rows to {out_file}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()