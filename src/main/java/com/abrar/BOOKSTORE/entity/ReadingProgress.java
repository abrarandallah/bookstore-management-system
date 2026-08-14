package com.abrar.BOOKSTORE.entity;

import com.abrar.BOOKSTORE.Login.user.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

// One row per (user, book) they've opened the reader for - separate from
// MyBookList, which is an explicit "bookmark" the user adds themselves.
// This is created automatically the first time BookController.readBook()
// is hit, regardless of whether the book is on their "My Books" list.
@Getter
@Entity
@Table(name = "reading_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "book_id" }))
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // 0-indexed, matches the reader's client-side page index in bookRead.html.
    @Column(nullable = false)
    private int currentPage;

    @Column(nullable = false)
    private Instant startedAt;

    @Column(nullable = false)
    private Instant lastReadAt;

    // Null until the reader reaches the last takeaway and confirms finishing.
    private Instant finishedAt;

    public ReadingProgress() {
    }

    public ReadingProgress(User user, Book book) {
        this.user = user;
        this.book = book;
        this.currentPage = 0;
        Instant now = Instant.now();
        this.startedAt = now;
        this.lastReadAt = now;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        this.lastReadAt = Instant.now();
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public boolean isFinished() {
        return finishedAt != null;
    }
}