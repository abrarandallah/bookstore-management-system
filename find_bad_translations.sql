-- Books whose Arabic name/author still contains Latin letters (a-z/A-Z) -
-- a reliable signal that LibreTranslate left it (partially or fully)
-- untranslated, e.g. "Memory Engine" staying exactly as-is.
SELECT b.id, b.name AS english_name, bt.name AS arabic_name
FROM book b
JOIN book_translations bt ON bt.book_id = b.id AND bt.language = 'ar'
WHERE bt.name REGEXP '[a-zA-Z]'
ORDER BY b.id;