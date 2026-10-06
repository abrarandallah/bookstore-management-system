package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.repository.BookRepository;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;
import com.abrar.BOOKSTORE.service.HomeShelvesService.ShelfItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { HomeShelvesService.class })
@ExtendWith(SpringExtension.class)
class HomeShelvesServiceTest {

    @Autowired
    private HomeShelvesService homeShelvesService;

    @MockBean
    private BookRepository bookRepository;

    @MockBean
    private ReadingProgressRepository readingProgressRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private BookTranslationService bookTranslationService;

    private final User reader = new User("reader", "reader@example.com", "hash", "ROLE_USER");

    /** A book with the given number of (empty) takeaway pages. */
    private Book bookWithTakeaways(int id, String name, int takeaways) {
        Book book = new Book(id, name, "Some Author");
        List<BookPage> pages = new ArrayList<>();
        for (int i = 0; i < takeaways; i++) {
            pages.add(new BookPage());
        }
        book.setTakeaways(pages);
        return book;
    }

    /** Only unfinished books show up, with how far along the reader is. */
    @Test
    void testContinueReadingShowsUnfinishedBooksWithProgress() {
        Book halfway = bookWithTakeaways(1, "Halfway Book", 4);
        Book done = bookWithTakeaways(2, "Finished Book", 3);
        ReadingProgress inProgress = new ReadingProgress(reader, halfway);
        inProgress.setCurrentPage(1); // 0-indexed: on the 2nd of 4 takeaways
        ReadingProgress finished = new ReadingProgress(reader, done);
        finished.setFinishedAt(Instant.now());
        when(userRepository.findByUsernameOrEmail("reader")).thenReturn(Optional.of(reader));
        when(readingProgressRepository.findByUserOrderByLastReadAtDesc(reader))
                .thenReturn(List.of(inProgress, finished));

        List<ShelfItem> items = homeShelvesService.continueReading("reader", "en");

        assertEquals(1, items.size());
        assertEquals("Halfway Book", items.get(0).name());
        assertEquals(2, items.get(0).step());
        assertEquals(50, items.get(0).percent());
    }

    /** A saved position past the end (takeaways were removed since) is clamped. */
    @Test
    void testContinueReadingClampsAPositionPastTheEnd() {
        Book book = bookWithTakeaways(1, "Shortened Book", 4);
        ReadingProgress progress = new ReadingProgress(reader, book);
        progress.setCurrentPage(9);
        when(userRepository.findByUsernameOrEmail("reader")).thenReturn(Optional.of(reader));
        when(readingProgressRepository.findByUserOrderByLastReadAtDesc(reader)).thenReturn(List.of(progress));

        List<ShelfItem> items = homeShelvesService.continueReading("reader", "en");

        assertEquals(4, items.get(0).step());
        assertEquals(100, items.get(0).percent());
    }

    @Test
    void testContinueReadingIsEmptyForAnUnknownUser() {
        when(userRepository.findByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

        assertTrue(homeShelvesService.continueReading("ghost", "en").isEmpty());
        assertTrue(homeShelvesService.continueReading(null, "en").isEmpty());
    }

    /** Best average first, regardless of the order the database returns the books in. */
    @Test
    void testTopRatedOrdersByAverageRating() {
        Book fourStars = bookWithTakeaways(1, "Four Stars", 3);
        Book fiveStars = bookWithTakeaways(2, "Five Stars", 3);
        when(reviewService.summariesForAllBooks())
                .thenReturn(Map.of(1, new RatingSummary(4.0, 2), 2, new RatingSummary(5.0, 1)));
        when(bookRepository.findAllById(Mockito.<Iterable<Integer>>any())).thenReturn(List.of(fourStars, fiveStars));

        List<ShelfItem> items = homeShelvesService.topRated("en");

        assertEquals(2, items.size());
        assertEquals("Five Stars", items.get(0).name());
        assertEquals("Four Stars", items.get(1).name());
    }

    @Test
    void testTopRatedIsEmptyWhenNothingHasBeenReviewed() {
        when(reviewService.summariesForAllBooks()).thenReturn(Map.of());

        assertTrue(homeShelvesService.topRated("en").isEmpty());
        verify(bookRepository, never()).findAllById(Mockito.<Iterable<Integer>>any());
    }

    @Test
    void testQuickReadsReturnsTheRepositoryResultAsShelfItems() {
        Book shortBook = bookWithTakeaways(5, "Short Book", 2);
        when(bookRepository.findQuickReads(Mockito.eq(HomeShelvesService.QUICK_READ_MAX_TAKEAWAYS),
                Mockito.any(Pageable.class))).thenReturn(List.of(shortBook));

        List<ShelfItem> items = homeShelvesService.quickReads("en");

        assertEquals(1, items.size());
        assertEquals("Short Book", items.get(0).name());
        assertEquals(2, items.get(0).takeawayCount());
        assertEquals(0, items.get(0).step());
    }
}
