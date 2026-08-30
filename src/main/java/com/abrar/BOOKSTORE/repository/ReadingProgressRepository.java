package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Bulk deletes - see the comment on ReviewRepository's equivalents for
    // why these use @Modifying + an explicit query instead of a plain
    // deleteByX method.
    @Modifying
    @Transactional
    @Query("DELETE FROM ReadingProgress p WHERE p.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM ReadingProgress p WHERE p.book = :book")
    void deleteByBook(@Param("book") Book book);
}