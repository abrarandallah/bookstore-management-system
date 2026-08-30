package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// A translated name/author for a Book, in one language. The Book's own
// name/author fields (see Book.java) are never overwritten - they stay the
// English original, and this table only ever holds the *other* languages.
// A book with no row here for a given language just isn't translated into
// that language yet; see BookTranslationService for the fallback lookup
// this is built around.
@Getter
@Entity
@Table(name = "book_translations", uniqueConstraints = @UniqueConstraint(columnNames = { "book_id", "language" }))
public class BookTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // ISO 639-1 code: "fr", "ar". Never "en" - see class comment above.
    @NotBlank
    private String language;

    @NotBlank(message = "Translated book name is required.")
    private String name;

    @NotBlank(message = "Translated author name is required.")
    private String author;

    public BookTranslation() {
        super();
    }

    public BookTranslation(Book book, String language, String name, String author) {
        this.book = book;
        this.language = language;
        this.name = name;
        this.author = author;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}