package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "Price is required.")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Price must be a positive number, e.g. 19.99.")
    private String price;

    // Path under /uploads/** the cover image was saved to, or null if the
    // book has no cover yet (falls back to a generated spine card).
    private String coverImageUrl;

    // The insights format: 1-10 short pages instead of the full book. Order
    // is driven by pageNumber, set when the form is submitted.
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pageNumber ASC")
    private List<BookPage> takeaways = new ArrayList<>();

    public Book(int id, String name, String author, String price) {
        super();
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
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

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setTakeaways(List<BookPage> takeaways) {
        this.takeaways = takeaways;
    }

    // A rough, honest estimate rather than a precise one: ~90 seconds per
    // takeaway, minimum 1 minute so an empty/unsaved book doesn't show "0 min".
    public int getEstimatedReadMinutes() {
        int minutes = (int) Math.ceil(takeaways.size() * 1.5);
        return Math.max(minutes, 1);
    }
}