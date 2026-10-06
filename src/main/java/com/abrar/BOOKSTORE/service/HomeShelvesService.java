package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.repository.BookRepository;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The little "shelves" shown on the home page: Continue reading, Newest,
 * Highest rated and Quick reads.
 *
 * Registered under the bean name "homeShelves" so home.html can call it
 * directly (the same way templates already call @genreLocalizer) - that keeps
 * BookController#home() untouched, so it still adds nothing to the model.
 * Every method returns books that actually have takeaways (a book with none
 * can't be read), already translated into the visitor's language.
 */
@Service("homeShelves")
public class HomeShelvesService {

    public static final int SHELF_SIZE = 6;
    public static final int CONTINUE_LIMIT = 4;
    // 4 takeaways is about 6 minutes (see Book#getEstimatedReadMinutes).
    public static final int QUICK_READ_MAX_TAKEAWAYS = 4;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ReadingProgressRepository readingProgressRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private BookTranslationService bookTranslationService;

    /**
     * One book card on a shelf.
     *
     * @param step    the takeaway the reader is on (1-based), or 0 when this
     *                shelf doesn't show progress.
     * @param percent how far through the book the reader is (0 when step is 0).
     * @param rating  null when nobody has reviewed the book yet.
     */
    public record ShelfItem(Book book, String name, String author, int takeawayCount, int minutes,
            RatingSummary rating, int step, int percent) {
    }

    /** The reader's unfinished books, most recently read first. */
    public List<ShelfItem> continueReading(String username, String language) {
        if (username == null) {
            return List.of();
        }
        Optional<User> user = userRepository.findByUsernameOrEmail(username);
        if (user.isEmpty()) {
            return List.of();
        }
        List<ReadingProgress> unfinished = readingProgressRepository.findByUserOrderByLastReadAtDesc(user.get())
                .stream()
                .filter(p -> !p.isFinished())
                .filter(p -> !p.getBook().getTakeaways().isEmpty())
                .limit(CONTINUE_LIMIT)
                .toList();
        List<Book> books = unfinished.stream().map(ReadingProgress::getBook).toList();
        Map<Integer, BookTranslationService.LocalizedBook> localized = bookTranslationService
                .localizeBooks(books, language);
        Map<Integer, RatingSummary> ratings = reviewService.summariesForAllBooks();
        return unfinished.stream().map(p -> {
            int total = p.getBook().getTakeaways().size();
            // The saved position can be past the end if takeaways were removed since.
            int step = Math.min(Math.max(p.getCurrentPage(), 0), total - 1) + 1;
            return item(p.getBook(), localized, ratings, step);
        }).toList();
    }

    /** The most recently added books. */
    public List<ShelfItem> newest(String language) {
        List<Book> books = bookRepository
                .findAll(PageRequest.of(0, SHELF_SIZE, Sort.by("id").descending()))
                .getContent().stream()
                .filter(b -> !b.getTakeaways().isEmpty())
                .toList();
        return items(books, language);
    }

    /** Books with reviews, best average first (more reviews win a tie). */
    public List<ShelfItem> topRated(String language) {
        Map<Integer, RatingSummary> ratings = reviewService.summariesForAllBooks();
        List<Integer> topIds = ratings.entrySet().stream()
                .sorted((a, b) -> {
                    int byAverage = Double.compare(b.getValue().getAverage(), a.getValue().getAverage());
                    return byAverage != 0 ? byAverage
                            : Long.compare(b.getValue().getCount(), a.getValue().getCount());
                })
                .limit(SHELF_SIZE)
                .map(Map.Entry::getKey)
                .toList();
        if (topIds.isEmpty()) {
            return List.of();
        }
        Map<Integer, Book> byId = bookRepository.findAllById(topIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        List<Book> ordered = topIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .filter(b -> !b.getTakeaways().isEmpty())
                .toList();
        return items(ordered, language);
    }

    /** Short books (a few takeaways), newest first. */
    public List<ShelfItem> quickReads(String language) {
        List<Book> books = bookRepository
                .findQuickReads(QUICK_READ_MAX_TAKEAWAYS, PageRequest.of(0, SHELF_SIZE));
        return items(books, language);
    }

    private List<ShelfItem> items(List<Book> books, String language) {
        if (books.isEmpty()) {
            return List.of();
        }
        Map<Integer, BookTranslationService.LocalizedBook> localized = bookTranslationService
                .localizeBooks(books, language);
        Map<Integer, RatingSummary> ratings = reviewService.summariesForAllBooks();
        return books.stream().map(b -> item(b, localized, ratings, 0)).toList();
    }

    private ShelfItem item(Book book, Map<Integer, BookTranslationService.LocalizedBook> localized,
            Map<Integer, RatingSummary> ratings, int step) {
        BookTranslationService.LocalizedBook lb = localized.get(book.getId());
        String name = lb != null ? lb.name() : book.getName();
        String author = lb != null ? lb.author() : book.getAuthor();
        int count = book.getTakeaways().size();
        int percent = step > 0 && count > 0 ? (step * 100) / count : 0;
        return new ShelfItem(book, name, author, count, book.getEstimatedReadMinutes(), ratings.get(book.getId()),
                step, percent);
    }
}
