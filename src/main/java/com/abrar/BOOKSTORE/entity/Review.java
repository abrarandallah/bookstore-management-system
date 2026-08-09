package com.abrar.BOOKSTORE.entity;

import com.abrar.BOOKSTORE.Login.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.time.LocalDateTime;

// One user's rating + optional written review of one book. Same shape as
// MyBookList (own auto-generated id, an explicit FK to the owning user, a
// uniqueness constraint), except the constraint here means "one review per
// user per book" rather than "can't bookmark the same book twice" - so a
// second submission from the same user is treated as an edit, not a
// duplicate (see ReviewService.submitReview). Book doesn't carry a
// reference back to its reviews; lookups and aggregates go through
// ReviewRepository instead, the same way Genre stays decoupled from Book.
@Getter
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = { "book_id", "user_id" }))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Review() {
        super();
    }

    public Review(Book book, User user, int rating, String comment) {
        super();
        this.book = book;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}