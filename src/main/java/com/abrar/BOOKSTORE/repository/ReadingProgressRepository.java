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

    long countByUserAndFinishedAtIsNotNull(User user);

    @Transactional
    void deleteByUser(User user);
}