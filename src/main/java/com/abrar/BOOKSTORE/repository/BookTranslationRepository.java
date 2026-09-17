package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.BookTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookTranslationRepository extends JpaRepository<BookTranslation, Long> {

    Optional<BookTranslation> findByBook_IdAndLanguage(int bookId, String language);

    // The book list page shows many books at once, so this fetches every
    // matching book's translation in one query rather than one per book -
    // see BookTranslationService.localizeBooks().
    List<BookTranslation> findByBook_IdInAndLanguage(List<Integer> bookIds, String language);
}