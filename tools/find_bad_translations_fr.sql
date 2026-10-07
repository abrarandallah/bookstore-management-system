-- French equivalent of find_bad_translations.sql. The "[a-zA-Z]" check
-- that script uses for Arabic doesn't work here, since French
-- legitimately uses Latin letters - so instead this looks for rows
-- where the French text is character-for-character IDENTICAL to the
-- English source, which is a strong signal LibreTranslate returned
-- the input unchanged rather than translating it.
--
-- Two lessons baked in from the first version of this file:
--  1. No author check - author names are SUPPOSED to stay identical
--     in French (you don't translate a person's name into another
--     Latin-script language), unlike Arabic where they get
--     transliterated. Checking author here can never give a useful
--     signal, so it's dropped rather than producing permanent noise.
--  2. Uses `= BINARY` (byte-exact) instead of plain `=`, because
--     MySQL's default collation is accent-insensitive - it treats
--     "Theo" and "Théo" as equal, which would falsely flag correctly
--     -accented French text as "untranslated."
--
-- A small number of genuine false positives are still possible (short
-- cognate words that are spelled identically in both languages, e.g.
-- "Ambition"), so treat matches here as candidates to eyeball rather
-- than an automatic "definitely broken."

SELECT 'book_translations.name' AS field, ft.id, b.name AS english, ft.name AS french
FROM book b
JOIN book_translations ft ON ft.book_id = b.id AND ft.language = 'fr'
WHERE ft.name = BINARY b.name

UNION ALL

SELECT 'book_page_translations.heading', pt.id, p.heading, pt.heading
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'fr'
WHERE pt.heading = BINARY p.heading

UNION ALL

SELECT 'book_page_translations.content', pt.id, LEFT(p.content, 60), LEFT(pt.content, 60)
FROM book_page p
JOIN book_page_translations pt ON pt.book_page_id = p.id AND pt.language = 'fr'
WHERE pt.content = BINARY p.content

ORDER BY field;