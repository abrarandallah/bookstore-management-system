-- Books/pages whose Arabic translation still contains a real run of
-- Latin letters (3+ in a row) - a reliable signal that LibreTranslate
-- left that word/phrase untranslated, e.g. "Memory Engine" staying
-- exactly as-is. Requires 3+ consecutive letters rather than a bare
-- [a-zA-Z] so it doesn't false-positive on legitimately-kept short
-- alphanumeric fragments like an apartment number ("3B") sitting inside
-- otherwise-correct Arabic text.

SELECT 'book_translations.name' AS field, t.id, b.name AS english, t.name AS arabic
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language = 'ar'
WHERE t.name REGEXP '[a-zA-Z]{3,}'

UNION ALL

SELECT 'book_translations.author', t.id, b.author, t.author
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language = 'ar'
WHERE t.author REGEXP '[a-zA-Z]{3,}'

UNION ALL

SELECT 'book_page_translations.heading', pt.id, p.heading, pt.heading
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.heading REGEXP '[a-zA-Z]{3,}'

UNION ALL

SELECT 'book_page_translations.content', pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.content REGEXP '[a-zA-Z]{3,}';