-- French equivalent of find_bad_translations.sql. The "[a-zA-Z]" check
-- that script uses for Arabic doesn't work here, since French
-- legitimately uses Latin letters - so instead this looks for rows
-- where the French text is character-for-character IDENTICAL to the
-- English source, which is a strong signal LibreTranslate returned
-- the input unchanged rather than translating it (the same "give up
-- and echo it back" behavior seen with the Arabic book titles).
--
-- Note: a small number of genuine false positives are possible here
-- (a short word or proper noun that's legitimately spelled the same
-- in French, e.g. certain names), so treat matches as candidates to
-- eyeball rather than an automatic "definitely broken" the way the
-- Arabic check is. Skips NULL/empty author fields to avoid two blanks
-- trivially "matching".

SELECT 'book_translations.name' AS field, ft.id, b.name AS english, ft.name AS french
FROM book b
JOIN book_translations ft ON ft.book_id = b.id AND ft.language = 'fr'
WHERE ft.name = b.name

UNION ALL

SELECT 'book_translations.author', ft.id, b.author, ft.author
FROM book b
JOIN book_translations ft ON ft.book_id = b.id AND ft.language = 'fr'
WHERE ft.author = b.author AND b.author IS NOT NULL AND b.author != ''

UNION ALL

SELECT 'book_page_translations.heading', pt.id, p.heading, pt.heading
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'fr'
WHERE pt.heading = p.heading

UNION ALL

SELECT 'book_page_translations.content', pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'fr'
WHERE pt.content = p.content

ORDER BY field;