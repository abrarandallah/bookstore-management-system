package com.abrar.BOOKSTORE.entity;

import com.abrar.BOOKSTORE.Login.user.User;
import jakarta.persistence.*;
import lombok.Getter;

// Previously this entity's @Id was the *book's* id reused directly as the
// primary key here too - which meant two different users adding the same
// book would collide on the same row, and "my books" was really "everyone's
// books" (no user link at all). Fixed: own auto-generated id, an explicit
// bookId reference, and a real link to the owning user, with a uniqueness
// constraint so the same user can't add the same book twice.
@Getter
@Entity
@Table(name = "MyBooks", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "book_id" }))
public class MyBookList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "book_id")
    private int bookId;

    private String name;
    private String author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public MyBookList() {
        super();
    }

    public MyBookList(int bookId, String name, String author, User user) {
        super();
        this.bookId = bookId;
        this.name = name;
        this.author = author;
        this.user = user;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setUser(User user) {
        this.user = user;
    }
}