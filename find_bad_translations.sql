-- Books/pages whose Arabic translation still contains Latin letters
-- (a-z/A-Z) - a reliable signal that LibreTranslate left the text
-- (partially or fully) untranslated, e.g. "Memory Engine" staying
-- exactly as-is. Unlike the French equivalent (find_bad_translations_fr.sql),
-- a plain [a-zA-Z] check works fine here since Arabic never legitimately
-- contains Latin letters.

SELECT 'book_translations.name' AS field, t.id, b.name AS english, t.name AS arabic
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language = 'ar'
WHERE t.name REGEXP '[a-zA-Z]'

UNION ALL

SELECT 'book_translations.author', t.id, b.author, t.author
FROM book b JOIN book_translations t ON t.book_id = b.id AND t.language = 'ar'
WHERE t.author REGEXP '[a-zA-Z]'

UNION ALL

SELECT 'book_page_translations.heading', pt.id, p.heading, pt.heading
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.heading REGEXP '[a-zA-Z]'

UNION ALL

SELECT 'book_page_translations.content', pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'ar'
WHERE pt.content REGEXP '[a-zA-Z]';