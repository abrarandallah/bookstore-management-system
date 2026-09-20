-- Run this right after retry_untranslated.py finishes, to see how much
-- it actually cleared. Numbers only, so safe to read straight off the
-- screen. Compare against the original: 23 / 61 / 36 / 41.
SELECT
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND name    REGEXP '[a-zA-Z]') AS names_still_bad,
  (SELECT COUNT(*) FROM book_translations      WHERE language='ar' AND author  REGEXP '[a-zA-Z]') AS authors_still_bad,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND heading REGEXP '[a-zA-Z]') AS headings_still_bad,
  (SELECT COUNT(*) FROM book_page_translations WHERE language='ar' AND content REGEXP '[a-zA-Z]') AS content_still_bad;