-- A different, more serious corruption pattern than find_bad_translations.sql
-- checks for: repeated literal "?" characters (2 or more in a row) in the
-- Arabic text. This is the signature of a character-encoding failure at
-- write time (the real Arabic characters never made it into the database
-- at all, and got replaced with "?" instead) - not the "still says the
-- English text" pattern the other script and retry_untranslated.py check
-- for, which is why this needs its own query. Covers every translated
-- field, not just book names.

SELECT 'book_translations.name' AS field, bt.id, b.name AS english, bt.name AS arabic
FROM book b JOIN book_translations bt ON bt.book_id = b.id AND bt.language = 'ar'
WHERE bt.name REGEXP '\?{2,}'

UNION ALL

SELECT 'book_translations.author', bt.id, b.author, bt.author
FROM book b JOIN book_translations bt ON bt.book_id = b.id AND bt.language = 'ar'
WHERE bt.author REGEXP '\?{2,}'

UNION ALL

SELECT 'book_page_translations.heading', pt.id, p.heading, pt.heading
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.heading REGEXP '\?{2,}'

UNION ALL

SELECT 'book_page_translations.content', pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.content REGEXP '\?{2,}';