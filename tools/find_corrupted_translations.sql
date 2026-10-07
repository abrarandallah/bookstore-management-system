-- A different, more serious corruption pattern than find_bad_translations.sql
-- checks for: repeated literal "?" characters (2 or more in a row) in the
-- translated text. This is the signature of a character-encoding failure at
-- write time (the real translated characters never made it into the
-- database at all, and got replaced with "?" instead) - not the "still
-- says the English text" pattern the other script and retry_untranslated.py
-- check for, which is why this needs its own query. Covers every
-- translated field, not just book names, and every language (this
-- corruption pattern is language-agnostic, unlike the Latin-letters check).

SELECT 'book_translations.name' AS field, t.language, t.id, b.name AS english, t.name AS translated
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language IN ('ar', 'fr')
WHERE t.name REGEXP '\\?{2,}'

UNION ALL

SELECT 'book_translations.author', t.language, t.id, b.author, t.author
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language IN ('ar', 'fr')
WHERE t.author REGEXP '\\?{2,}'

UNION ALL

SELECT 'book_page_translations.heading', pt.language, pt.id, p.heading, pt.heading
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language IN ('ar', 'fr')
WHERE pt.heading REGEXP '\\?{2,}'

UNION ALL

SELECT 'book_page_translations.content', pt.language, pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language IN ('ar', 'fr')
WHERE pt.content REGEXP '\\?{2,}';