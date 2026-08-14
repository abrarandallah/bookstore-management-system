package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReadingProgressService {

    @Autowired
    private ReadingProgressRepository readingProgressRepository;

    /**
     * Creates a row on first visit to the reader; otherwise returns the existing
     * one untouched.
     */
    public ReadingProgress getOrCreate(User user, Book book) {
        return readingProgressRepository.findByUserAndBook(user, book)
                .orElseGet(() -> readingProgressRepository.save(new ReadingProgress(user, book)));
    }

    /** Saves the reader's current position - called on every page turn. */
    public void updatePage(User user, Book book, int pageIndex) {
        ReadingProgress progress = getOrCreate(user, book);
        if (progress.isFinished()) {
            return; // Re-reading a finished book doesn't reopen/re-track it.
        }
        progress.setCurrentPage(pageIndex);
        readingProgressRepository.save(progress);
    }

    /**
     * Marks a book finished. Idempotent - re-finishing an already-finished
     * book just returns the existing row without re-triggering achievement
     * checks (see BookController, which only calls this from the "Done"
     * button on the last page).
     */
    public ReadingProgress markFinished(User user, Book book) {
        ReadingProgress progress = getOrCreate(user, book);
        if (!progress.isFinished()) {
            progress.setFinishedAt(Instant.now());
            readingProgressRepository.save(progress);
        }
        return progress;
    }

    public List<ReadingProgress> getHistory(User user) {
        return readingProgressRepository.findByUserOrderByLastReadAtDesc(user);
    }

    public long countFinished(User user) {
        return readingProgressRepository.countByUserAndFinishedAtIsNotNull(user);
    }
}