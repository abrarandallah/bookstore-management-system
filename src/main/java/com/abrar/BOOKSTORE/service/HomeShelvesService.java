package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.repository.BookRepository;
import com.abrar.BOOKSTORE.repository.GenreRepository;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The little "shelves" shown on the home page: Continue reading, Newest,
 * and Highest rated.
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
    public static final int RELATED_LIMIT = 3;
    public static final int SHOWCASE_SIZE = 14;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;
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

    /**
     * The home page's "Takeaway of the day".
     *
     * @param date    the day this pick is for (changes every day)
     * @param content the full text of the takeaway; the page shortens it
     */
    public record DailyPick(Book book, String name, String author, String heading, String content, LocalDate date) {
    }

    /**
     * One takeaway, the same for everyone all day and different tomorrow, so
     * the home page has a reason to visit again. Nothing is stored: the book
     * and takeaway are worked out from the date, so the pick only changes when
     * books are added or removed. Null when there is nothing to show.
     */
    public DailyPick takeawayOfTheDay(String language) {
        return takeawayOfTheDay(language, LocalDate.now());
    }

    public DailyPick takeawayOfTheDay(String language, LocalDate date) {
        List<Integer> ids = bookRepository.findReadableBookIds();
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        long day = date.toEpochDay();
        Optional<Book> found = bookRepository.findById(ids.get((int) Math.floorMod(day, (long) ids.size())));
        if (found.isEmpty() || found.get().getTakeaways().isEmpty()) {
            return null;
        }
        Book book = found.get();
        List<BookPage> pages = book.getTakeaways();
        // Different takeaway each time the same book comes round again.
        BookPage page = pages.get((int) Math.floorMod(day / ids.size(), (long) pages.size()));

        BookTranslationService.LocalizedBook lb = bookTranslationService.localizeBook(book, language);
        Map<Long, BookTranslationService.LocalizedPage> localizedPages = bookTranslationService
                .localizePages(List.of(page), language);
        BookTranslationService.LocalizedPage lp = localizedPages == null ? null : localizedPages.get(page.getId());
        return new DailyPick(book,
                lb != null ? lb.name() : book.getName(),
                lb != null ? lb.author() : book.getAuthor(),
                lp != null ? lp.heading() : page.getHeading(),
                lp != null ? lp.content() : page.getContent(),
                date);
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

    /** The numbers shown on the home page: how much there is to read. */
    public record SiteStats(long books, long genres, long takeaways) {
    }

    public SiteStats siteStats() {
        List<Integer> ids = bookRepository.findReadableBookIds();
        return new SiteStats(ids == null ? 0 : ids.size(), genreRepository.count(), bookRepository.countTakeaways());
    }

    /**
     * The scrolling strip of covers on the home page: a handful of readable
     * books, reshuffled once a day (same for everyone on a given day, so
     * nothing is stored) so the strip shows different genres each visit
     * instead of just the latest uploads.
     */
    public List<ShelfItem> showcase(String language) {
        return showcase(language, LocalDate.now());
    }

    public List<ShelfItem> showcase(String language, LocalDate date) {
        List<Integer> ids = bookRepository.findReadableBookIds();
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Integer> shuffled = new ArrayList<>(ids);
        Collections.shuffle(shuffled, new Random(date.toEpochDay()));
        List<Integer> picked = shuffled.subList(0, Math.min(SHOWCASE_SIZE, shuffled.size()));
        Map<Integer, Book> byId = bookRepository.findAllById(picked).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        List<Book> ordered = picked.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .filter(b -> !b.getTakeaways().isEmpty())
                .toList();
        return items(ordered, language);
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

    /**
     * "You might also like": other books sharing at least one genre with the
     * given book, skipping the book itself and any the reader has already
     * finished. Books sharing the most genres come first, then the newest.
     * Used on the page shown after finishing a book (bookFinished.html).
     */
    public List<ShelfItem> relatedBooks(int bookId, String username, String language) {
        Optional<Book> source = bookRepository.findById(bookId);
        if (source.isEmpty() || source.get().getGenres().isEmpty()) {
            return List.of();
        }
        Map<Integer, Book> candidates = new LinkedHashMap<>();
        Map<Integer, Integer> sharedGenres = new HashMap<>();
        for (Genre genre : source.get().getGenres()) {
            for (Book candidate : bookRepository.findByGenres_Id(genre.getId())) {
                if (candidate.getId() == bookId || candidate.getTakeaways().isEmpty()) {
                    continue;
                }
                candidates.putIfAbsent(candidate.getId(), candidate);
                sharedGenres.merge(candidate.getId(), 1, Integer::sum);
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }
        Set<Integer> alreadyFinished = new HashSet<>();
        if (username != null) {
            userRepository.findByUsernameOrEmail(username).ifPresent(user -> readingProgressRepository
                    .findByUserAndBookIn(user, new ArrayList<>(candidates.values())).stream()
                    .filter(ReadingProgress::isFinished)
                    .forEach(p -> alreadyFinished.add(p.getBook().getId())));
        }
        Comparator<Book> byMostSharedGenres = Comparator
                .<Book>comparingInt(b -> sharedGenres.get(b.getId())).reversed();
        Comparator<Book> byNewest = Comparator.<Book>comparingInt(Book::getId).reversed();
        List<Book> picks = candidates.values().stream()
                .filter(b -> !alreadyFinished.contains(b.getId()))
                .sorted(byMostSharedGenres.thenComparing(byNewest))
                .limit(RELATED_LIMIT)
                .toList();
        return items(picks, language);
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
