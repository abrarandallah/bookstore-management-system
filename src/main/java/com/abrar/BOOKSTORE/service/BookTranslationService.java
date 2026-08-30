package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.BookPageTranslation;
import com.abrar.BOOKSTORE.entity.BookTranslation;
import com.abrar.BOOKSTORE.repository.BookPageTranslationRepository;
import com.abrar.BOOKSTORE.repository.BookTranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Looks up the translated name/author/takeaway text for a book in a given
// language, falling back to the book's own (English) fields whenever no
// translation row exists yet. This is the only place that fallback logic
// lives - controllers/templates should always go through here rather than
// reading Book/BookPage fields directly once a page needs to respect the
// visitor's chosen language. See Phase 1 of the language project notes for
// why this is a separate table rather than extra columns on Book/BookPage.
@Service
public class BookTranslationService {

    // The language the Book/BookPage entities' own fields are written in.
    // Never looked up in the translation tables - see those entities'
    // class comments.
    public static final String SOURCE_LANGUAGE = "en";

    @Autowired
    private BookTranslationRepository bookTranslationRepository;

    @Autowired
    private BookPageTranslationRepository bookPageTranslationRepository;

    /**
     * The book's name/author for {@code language}, or the original English
     * ones if no translation exists yet.
     */
    public LocalizedBook localizeBook(Book book, String language) {
        if (SOURCE_LANGUAGE.equals(language)) {
            return new LocalizedBook(book.getName(), book.getAuthor());
        }
        return bookTranslationRepository.findByBook_IdAndLanguage(book.getId(), language)
                .map(t -> new LocalizedBook(t.getName(), t.getAuthor()))
                .orElseGet(() -> new LocalizedBook(book.getName(), book.getAuthor()));
    }

    /**
     * Every one of {@code book}'s takeaways, keyed by page id, each
     * translated into {@code language} where a translation exists and left
     * as the original English otherwise - a mix of translated and
     * not-yet-translated pages within the same book is expected and fine,
     * that's the whole point of doing this per-page rather than per-book.
     * One query regardless of how many takeaways the book has, rather than
     * one query per takeaway.
     */
    public Map<Long, LocalizedPage> localizePages(List<BookPage> pages, String language) {
        Map<Long, LocalizedPage> result = new HashMap<>();
        for (BookPage page : pages) {
            result.put(page.getId(), new LocalizedPage(page.getHeading(), page.getContent()));
        }
        if (SOURCE_LANGUAGE.equals(language) || pages.isEmpty()) {
            return result;
        }
        int bookId = pages.get(0).getBook().getId();
        List<BookPageTranslation> translations = bookPageTranslationRepository
                .findByBookPage_Book_IdAndLanguage(bookId, language);
        for (BookPageTranslation t : translations) {
            result.put(t.getBookPage().getId(), new LocalizedPage(t.getHeading(), t.getContent()));
        }
        return result;
    }

    public record LocalizedBook(String name, String author) {
    }

    public record LocalizedPage(String heading, String content) {
    }
}