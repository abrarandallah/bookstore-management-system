-- Row 729 showed up as "?" marks in PowerShell's terminal, in the
-- ARABIC check's results - per the lesson learned earlier this session,
-- that alone doesn't prove real corruption (PowerShell's console can't
-- always render non-ASCII output correctly even when the underlying
-- data is fine). This checks the actual stored bytes directly instead
-- of trusting the terminal's rendering of them.
SELECT
    id,
    CHAR_LENGTH(content) AS char_length,
    LENGTH(content) AS byte_length,
    HEX(LEFT(content, 20)) AS first_20_chars_hex
FROM book_page_translations
WHERE id = 729 AND language = 'ar';