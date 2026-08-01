package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// One "takeaway" - a short, focused page summarizing a single idea from the
// book. A Book has 1-10 of these instead of full text, per the insights
// format (see Book.takeaways).
@Getter
@Entity
public class BookPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int pageNumber;

    @NotBlank(message = "Each takeaway needs a heading.")
    private String heading;

    @NotBlank(message = "Each takeaway needs some content.")
    @Column(length = 4000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    public BookPage() {
        super();
    }

    public BookPage(int pageNumber, String heading, String content) {
        super();
        this.pageNumber = pageNumber;
        this.heading = heading;
        this.content = content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}