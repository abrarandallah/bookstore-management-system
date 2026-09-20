-- Extends the book.name check you already ran to the 3 remaining Arabic
-- columns: book_translations.author, book_page_translations.heading,
-- book_page_translations.content. Same REGEXP '[a-zA-Z]' approach.

-- 1) Scope summary first - just counts, so we know what we're dealing with
--    before looking at any individual rows.
SELECT
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND name    REGEXP '[a-zA-Z]') AS bad_names,
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND author  REGEXP '[a-zA-Z]') AS bad_authors,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND heading REGEXP '[a-zA-Z]') AS bad_headings,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND content REGEXP '[a-zA-Z]') AS bad_content;

-- 2) Authors left in English - same shape as the book-name query you
--    already have results for.
SELECT b.id, b.author AS english_author, bt.author AS arabic_author
FROM book b
JOIN book_translations bt ON bt.book_id = b.id AND bt.language = 'ar'
WHERE bt.author REGEXP '[a-zA-Z]'
ORDER BY b.id;

-- 3) Takeaway page HEADINGS left in English.
SELECT bk.id AS book_id, bk.name AS book_name, bp.page_number,
       bp.heading AS english_heading, bpt.heading AS arabic_heading
FROM book_page bp
JOIN book_page_translations bpt ON bpt.book_page_id = bp.id AND bpt.language = 'ar'
JOIN book bk ON bk.id = bp.book_id
WHERE bpt.heading REGEXP '[a-zA-Z]'
ORDER BY bk.id, bp.page_number;

-- 4) Takeaway page CONTENT left in English - IDs only for now, not the
--    full text. Content can run up to 4000 characters per row, so let's
--    see how many rows are actually affected before dumping any of it.
SELECT bk.id AS book_id, bk.name AS book_name, bp.page_number
FROM book_page bp
JOIN book_page_translations bpt ON bpt.book_page_id = bp.id AND bpt.language = 'ar'
JOIN book bk ON bk.id = bp.book_id
WHERE bpt.content REGEXP '[a-zA-Z]'
ORDER BY bk.id, bp.page_number;