package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.BookTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookTranslationRepository extends JpaRepository<BookTranslation, Long> {

    Optional<BookTranslation> findByBook_IdAndLanguage(int bookId, String language);
}