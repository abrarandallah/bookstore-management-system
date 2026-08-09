package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Book name is required.")
    private String name;

    @NotBlank(message = "Author name is required.")
    private String author;

    // Path under /uploads/** the cover image was saved to, or null if the
    // book has no cover yet (falls back to a generated spine card).
    private String coverImageUrl;

    // The insights format: 1-10 short pages instead of the full book. Order
    // is driven by pageNumber, set when the form is submitted.
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<BookPage> takeaways = new ArrayList<>();

    // Shelf categories this book is tagged with (0 or more). Owning side of
    // the relationship - the join table is the only place this link is
    // stored, Genre doesn't carry a reference back.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_genres", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @OrderBy("name ASC")
    private Set<Genre> genres = new LinkedHashSet<>();

    public Book(int id, String name, String author) {
        super();
        this.id = id;
        this.name = name;
        this.author = author;
    }

    public Book() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setTakeaways(List<BookPage> takeaways) {
        this.takeaways = takeaways;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    // A rough, honest estimate rather than a precise one: ~90 seconds per
    // takeaway, minimum 1 minute so an empty/unsaved book doesn't show "0 min".
    public int getEstimatedReadMinutes() {
        int minutes = (int) Math.ceil(takeaways.size() * 1.5);
        return Math.max(minutes, 1);
    }
}