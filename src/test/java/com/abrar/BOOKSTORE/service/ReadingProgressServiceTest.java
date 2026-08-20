package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.ReadingProgress;
import com.abrar.BOOKSTORE.repository.ReadingProgressRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { ReadingProgressService.class })
@ExtendWith(SpringExtension.class)
class ReadingProgressServiceTest {

    @Autowired
    private ReadingProgressService readingProgressService;

    @MockBean
    private ReadingProgressRepository readingProgressRepository;

    private final User owner = new User("reader", "reader@example.com", "hash", "ROLE_USER");
    private final Book book = new Book(1, "Atomic Habits", "James Clear");

    /**
     * The IDOR guard: scoped to (id, user) together, same pattern as
     * ReviewService.deleteById - one user cannot dismiss/delete another
     * user's reading-history row by guessing its id.
     */
    @Test
    void testDeleteHistoryEntryRemovesAnOwnedEntry() {
        ReadingProgress progress = new ReadingProgress(owner, book);
        when(readingProgressRepository.findByIdAndUser(9L, owner)).thenReturn(Optional.of(progress));
        doNothing().when(readingProgressRepository).delete(progress);

        readingProgressService.deleteHistoryEntry(9L, owner);

        verify(readingProgressRepository).delete(progress);
    }

    /**
     * Per the method's documented contract, this must silently no-op
     * (never throw, never call delete) rather than reveal whether id 9
     * exists at all under a different user.
     */
    @Test
    void testDeleteHistoryEntrySilentlyNoOpsForAnEntryOwnedBySomeoneElse() {
        when(readingProgressRepository.findByIdAndUser(9L, owner)).thenReturn(Optional.empty());

        readingProgressService.deleteHistoryEntry(9L, owner);

        verify(readingProgressRepository, never()).delete(Mockito.<ReadingProgress>any());
    }

    @Test
    void testGetOrCreateReturnsExistingRowWithoutCreatingADuplicate() {
        ReadingProgress existing = new ReadingProgress(owner, book);
        when(readingProgressRepository.findByUserAndBook(owner, book)).thenReturn(Optional.of(existing));

        ReadingProgress result = readingProgressService.getOrCreate(owner, book);

        assertEquals(existing, result);
        verify(readingProgressRepository, never()).save(Mockito.<ReadingProgress>any());
    }

    @Test
    void testGetOrCreateCreatesARowOnFirstVisit() {
        when(readingProgressRepository.findByUserAndBook(owner, book)).thenReturn(Optional.empty());
        when(readingProgressRepository.save(Mockito.<ReadingProgress>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingProgress result = readingProgressService.getOrCreate(owner, book);

        assertEquals(owner, result.getUser());
        assertEquals(book, result.getBook());
        assertFalse(result.isFinished());
    }

    @Test
    void testMarkFinishedIsIdempotent() {
        ReadingProgress progress = new ReadingProgress(owner, book);
        progress.setFinishedAt(java.time.Instant.now().minusSeconds(60));
        when(readingProgressRepository.findByUserAndBook(owner, book)).thenReturn(Optional.of(progress));

        ReadingProgress result = readingProgressService.markFinished(owner, book);

        assertTrue(result.isFinished());
        // Already finished before this call - markFinished should leave it
        // alone rather than re-save it (and, per BookController, re-trigger
        // achievement checks a second time).
        verify(readingProgressRepository, never()).save(Mockito.<ReadingProgress>any());
    }
}