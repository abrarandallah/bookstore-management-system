-- Part 1: the one confirmed real bug from the author check - id 130
-- "Elias North" came back as "Elas North" (dropped the "i"). Every other
-- flagged author was byte-for-byte identical to the English, so this is
-- the only one that's an actual typo rather than a deliberate
-- "keep the Latin name as-is" choice.
UPDATE book_translations
SET author = 'Elias North'
WHERE book_id = 130 AND language = 'ar';

-- Part 2: does any Arabic column contain a literal '?' where real
-- content should be? That's different from being left in English - it
-- would mean the translation was actually lost somewhere, not just
-- skipped. Plain counts only, so this part is safe to read straight off
-- the screen even if your terminal can't render Arabic reliably.
SELECT
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND name    LIKE '%?%') AS name_has_qmark,
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND author  LIKE '%?%') AS author_has_qmark,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND heading LIKE '%?%') AS heading_has_qmark,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND content LIKE '%?%') AS content_has_qmark;

-- Part 3: full detail for whichever of those come back non-zero. This
-- part contains actual Arabic text, which is exactly why the command
-- below sends the whole script's output to a file instead of the
-- screen - so the real bytes survive instead of whatever your
-- terminal's font/codepage does to them.
SELECT bk.id AS book_id, bk.name AS book_name, bp.page_number,
       bp.heading AS english_heading, bpt.heading AS arabic_heading
FROM book_page bp
JOIN book_page_translations bpt ON bpt.book_page_id = bp.id AND bpt.language = 'ar'
JOIN book bk ON bk.id = bp.book_id
WHERE bpt.heading LIKE '%?%'
ORDER BY bk.id, bp.page_number;

SELECT bk.id AS book_id, bk.name AS book_name, bp.page_number,
       bpt.content AS arabic_content
FROM book_page bp
JOIN book_page_translations bpt ON bpt.book_page_id = bp.id AND bpt.language = 'ar'
JOIN book bk ON bk.id = bp.book_id
WHERE bpt.content LIKE '%?%'
ORDER BY bk.id, bp.page_number;

SELECT b.id, b.name AS english_name, bt.name AS arabic_name
FROM book b
JOIN book_translations bt ON bt.book_id = b.id AND bt.language = 'ar'
WHERE bt.name LIKE '%?%'
ORDER BY b.id;