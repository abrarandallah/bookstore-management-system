-- Baseline schema, reconstructed to match what Hibernate's
-- `ddl-auto=update` had already been generating from the JPA entities
-- (see application.properties for how this baseline interacts with
-- existing deployments vs. fresh installs).
--
-- Table/column names follow Spring Boot's default physical naming
-- strategy (camelCase -> snake_case) except where an entity explicitly
-- overrides them via @Table/@Column - those are called out inline.

-- ---------------------------------------------------------------------
-- users (Login/user/User.java)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    username_or_email VARCHAR(255) NULL,
    email             VARCHAR(255) NULL,
    password          VARCHAR(255) NULL,
    role              VARCHAR(255) NULL,
    avatar_url        VARCHAR(255) NULL,
    -- Explicit columnDefinition on the entity: existing rows are
    -- grandfathered in as verified when this column was first added.
    verified          TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username_or_email (username_or_email),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- achievements (entity/Achievement.java) - static catalog, seeded at
-- startup by AchievementSeeder; this table just defines its shape.
-- ---------------------------------------------------------------------
CREATE TABLE achievements (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    icon        VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_achievements_code (code)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- genres (entity/Genre.java)
-- ---------------------------------------------------------------------
CREATE TABLE genres (
    id   INT          NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_genres_name (name)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- book (entity/Book.java) - no @Table override, so the default table
-- name is the entity's simple name lowercased.
-- ---------------------------------------------------------------------
CREATE TABLE book (
    id                INT          NOT NULL AUTO_INCREMENT,
    name              VARCHAR(255) NULL,
    author            VARCHAR(255) NULL,
    cover_image_url   VARCHAR(255) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- book_page (entity/BookPage.java) - one "takeaway" per row, ordered by
-- page_number within a book (see Book.takeaways).
-- ---------------------------------------------------------------------
CREATE TABLE book_page (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    page_number INT           NOT NULL,
    heading     VARCHAR(255)  NULL,
    content     VARCHAR(4000) NULL,
    book_id     INT           NULL,
    PRIMARY KEY (id),
    KEY idx_book_page_book_id (book_id),
    CONSTRAINT fk_book_page_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- book_genres (Book.genres @JoinTable) - many-to-many, owned by Book.
-- No surrogate key: Hibernate doesn't add one to a plain @JoinTable.
-- ---------------------------------------------------------------------
CREATE TABLE book_genres (
    book_id  INT NOT NULL,
    genre_id INT NOT NULL,
    KEY idx_book_genres_genre_id (genre_id),
    CONSTRAINT fk_book_genres_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_book_genres_genre FOREIGN KEY (genre_id) REFERENCES genres (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- MyBooks (entity/MyBookList.java) - explicit @Table(name = "MyBooks")
-- ---------------------------------------------------------------------
CREATE TABLE MyBooks (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    book_id INT          NOT NULL,
    name    VARCHAR(255) NULL,
    author  VARCHAR(255) NULL,
    user_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mybooks_user_book (user_id, book_id),
    CONSTRAINT fk_mybooks_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- reading_progress (entity/ReadingProgress.java)
-- ---------------------------------------------------------------------
CREATE TABLE reading_progress (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    book_id       INT         NOT NULL,
    current_page  INT         NOT NULL,
    started_at    DATETIME(6) NOT NULL,
    last_read_at  DATETIME(6) NOT NULL,
    finished_at   DATETIME(6) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reading_progress_user_book (user_id, book_id),
    CONSTRAINT fk_reading_progress_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reading_progress_book FOREIGN KEY (book_id) REFERENCES book (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- reviews (entity/Review.java)
-- ---------------------------------------------------------------------
CREATE TABLE reviews (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    book_id    INT          NOT NULL,
    user_id    BIGINT       NOT NULL,
    rating     INT          NOT NULL,
    comment    VARCHAR(2000) NULL,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reviews_book_user (book_id, user_id),
    CONSTRAINT fk_reviews_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- user_achievements (entity/UserAchievement.java)
-- ---------------------------------------------------------------------
CREATE TABLE user_achievements (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    achievement_id BIGINT      NOT NULL,
    earned_at      DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_achievements_user_achievement (user_id, achievement_id),
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- email_verification_tokens (Login/user/EmailVerificationToken.java)
-- ---------------------------------------------------------------------
CREATE TABLE email_verification_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_verification_tokens_token (token),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- password_reset_tokens (Login/user/PasswordResetToken.java)
-- ---------------------------------------------------------------------
CREATE TABLE password_reset_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_tokens_token (token),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;