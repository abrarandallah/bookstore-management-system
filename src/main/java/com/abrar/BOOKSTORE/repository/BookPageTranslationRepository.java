package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.BookPageTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookPageTranslationRepository extends JpaRepository<BookPageTranslation, Long> {

    Optional<BookPageTranslation> findByBookPage_IdAndLanguage(long bookPageId, String language);

    // A book's reader page (bookRead.html) shows every takeaway at once, so
    // this fetches all of a book's translated pages for a language in one
    // query rather than one per takeaway - see
    // BookTranslationService.localizePages().
    List<BookPageTranslation> findByBookPage_Book_IdAndLanguage(int bookId, String language);
}