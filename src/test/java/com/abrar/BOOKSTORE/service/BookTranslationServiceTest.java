package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.BookPageTranslation;
import com.abrar.BOOKSTORE.entity.BookTranslation;
import com.abrar.BOOKSTORE.repository.BookPageTranslationRepository;
import com.abrar.BOOKSTORE.repository.BookTranslationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { BookTranslationService.class })
@ExtendWith(SpringExtension.class)
class BookTranslationServiceTest {

    @Autowired
    private BookTranslationService bookTranslationService;

    @MockBean
    private BookTranslationRepository bookTranslationRepository;

    @MockBean
    private BookPageTranslationRepository bookPageTranslationRepository;

    @Test
    void testLocalizeBookAlwaysReturnsOriginalForEnglishRegardlessOfTranslationRows() {
        Book book = new Book(1, "Dune", "Frank Herbert");

        var result = bookTranslationService.localizeBook(book, "en");

        assertEquals("Dune", result.name());
        assertEquals("Frank Herbert", result.author());
    }

    @Test
    void testLocalizeBookReturnsTranslationWhenOneExists() {
        Book book = new Book(1, "Dune", "Frank Herbert");
        BookTranslation translation = new BookTranslation(book, "fr", "Dune", "Frank Herbert (trad.)");
        when(bookTranslationRepository.findByBook_IdAndLanguage(1, "fr")).thenReturn(Optional.of(translation));

        var result = bookTranslationService.localizeBook(book, "fr");

        assertEquals("Dune", result.name());
        assertEquals("Frank Herbert (trad.)", result.author());
    }

    @Test
    void testLocalizeBookFallsBackToEnglishWhenNoTranslationExistsYet() {
        Book book = new Book(1, "Dune", "Frank Herbert");
        when(bookTranslationRepository.findByBook_IdAndLanguage(1, "ar")).thenReturn(Optional.empty());

        var result = bookTranslationService.localizeBook(book, "ar");

        assertEquals("Dune", result.name());
        assertEquals("Frank Herbert", result.author());
    }

    @Test
    void testLocalizePagesMixesTranslatedAndUntranslatedPagesWithinTheSameBook() {
        Book book = new Book(1, "Dune", "Frank Herbert");
        BookPage translatedPage = new BookPage(1, "The Desert Planet", "Arrakis is...");
        translatedPage.setId(100L);
        translatedPage.setBook(book);
        BookPage untranslatedPage = new BookPage(2, "House Atreides", "The Atreides accept...");
        untranslatedPage.setId(101L);
        untranslatedPage.setBook(book);

        BookPageTranslation translation = new BookPageTranslation(translatedPage, "fr", "La planète désertique",
                "Arrakis est...");
        when(bookPageTranslationRepository.findByBookPage_Book_IdAndLanguage(1, "fr"))
                .thenReturn(List.of(translation));

        Map<Long, BookTranslationService.LocalizedPage> result = bookTranslationService
                .localizePages(List.of(translatedPage, untranslatedPage), "fr");

        assertEquals("La planète désertique", result.get(100L).heading());
        assertEquals("Arrakis est...", result.get(100L).content());
        // No French row for this page yet - falls back to the original English.
        assertEquals("House Atreides", result.get(101L).heading());
        assertEquals("The Atreides accept...", result.get(101L).content());
    }

    @Test
    void testLocalizePagesReturnsOriginalTextForEnglishWithoutQueryingTranslations() {
        Book book = new Book(1, "Dune", "Frank Herbert");
        BookPage page = new BookPage(1, "The Desert Planet", "Arrakis is...");
        page.setId(100L);
        page.setBook(book);

        Map<Long, BookTranslationService.LocalizedPage> result = bookTranslationService
                .localizePages(List.of(page), "en");

        assertEquals("The Desert Planet", result.get(100L).heading());
    }
}