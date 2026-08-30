-- Adds per-language translations for book titles/authors and takeaway
-- content. The original `book` / `book_page` rows are left untouched and
-- keep acting as the English source of truth - a book with no rows in
-- these tables for a given language just falls back to displaying that,
-- so translations can be added one book/language at a time instead of
-- needing to be all-or-nothing before anything ships.
--
-- Piloted against Arabic first (see README/session notes) specifically
-- to surface RTL layout issues before French is added, since RTL is a
-- frontend concern, not a data concern, and this table design is the
-- same either way.

-- ---------------------------------------------------------------------
-- book_translations (new entity: entity/BookTranslation.java)
-- ---------------------------------------------------------------------
CREATE TABLE book_translations (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    book_id   INT          NOT NULL,
    -- ISO 639-1 code: 'fr', 'ar'. English is NOT stored here - it's
    -- whatever's already on the `book` row, so there's nothing to keep
    -- in sync and no risk of the "en" row and the real row disagreeing.
    language  VARCHAR(5)   NOT NULL,
    name      VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    -- At most one translation per book per language - the lookup this
    -- whole feature depends on ("find the Arabic translation of book
    -- #42") relies on this being unique, not just usually true.
    UNIQUE KEY uk_book_translations_book_language (book_id, language),
    CONSTRAINT fk_book_translations_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- book_page_translations (new entity: entity/BookPageTranslation.java)
-- ---------------------------------------------------------------------
CREATE TABLE book_page_translations (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    book_page_id BIGINT        NOT NULL,
    language     VARCHAR(5)    NOT NULL,
    heading      VARCHAR(255)  NOT NULL,
    content      VARCHAR(4000) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_page_translations_page_language (book_page_id, language),
    CONSTRAINT fk_book_page_translations_page FOREIGN KEY (book_page_id) REFERENCES book_page (id)
) ENGINE = InnoDB;