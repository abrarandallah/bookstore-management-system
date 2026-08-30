package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// Translated heading/content for one BookPage (takeaway), in one language.
// Same fallback relationship to BookPage that BookTranslation has to Book -
// see that class's comment, and BookTranslationService.
@Getter
@Entity
@Table(name = "book_page_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "book_page_id",
        "language" }))
public class BookPageTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_page_id", nullable = false)
    private BookPage bookPage;

    // ISO 639-1 code: "fr", "ar". Never "en" - the original lives on
    // BookPage itself.
    @NotBlank
    private String language;

    @NotBlank(message = "Translated heading is required.")
    private String heading;

    @NotBlank(message = "Translated content is required.")
    @Column(length = 4000)
    private String content;

    public BookPageTranslation() {
        super();
    }

    public BookPageTranslation(BookPage bookPage, String language, String heading, String content) {
        this.bookPage = bookPage;
        this.language = language;
        this.heading = heading;
        this.content = content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBookPage(BookPage bookPage) {
        this.bookPage = bookPage;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setContent(String content) {
        this.content = content;
    }
}