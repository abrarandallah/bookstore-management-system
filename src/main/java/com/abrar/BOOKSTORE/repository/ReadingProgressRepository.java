package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserAndBook(User user, Book book);

    List<ReadingProgress> findByUserOrderByLastReadAtDesc(User user);

    List<ReadingProgress> findByUserAndBookIn(User user, List<Book> books);

    long countByUserAndFinishedAtIsNotNull(User user);

    // Scoped to the owning user so one user can't delete another's history
    // row just by guessing/crafting an id.
    Optional<ReadingProgress> findByIdAndUser(Long id, User user);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByBook(Book book);
}