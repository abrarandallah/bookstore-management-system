package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.Review;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.ReviewRepository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { ReviewService.class })
@ExtendWith(SpringExtension.class)
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @MockBean
    private ReviewRepository reviewRepository;

    private final User owner = new User("reader", "reader@example.com", "hash", "ROLE_USER");
    private final User attacker = new User("someone-else", "attacker@example.com", "hash", "ROLE_USER");
    private final Book book = new Book(1, "Atomic Habits", "James Clear");

    /**
     * The #1 thing IDOR-vulnerable apps get wrong: this must go through
     * findByIdAndUser (scoped to the caller), not findById, so one user
     * can't delete another user's review by guessing/incrementing ids.
     */
    @Test
    void testDeleteByIdRemovesOwnedReview() {
        Review review = new Review(book, owner, 5, "Loved it.");
        when(reviewRepository.findByIdAndUser(5L, owner)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteById(5L, owner);

        verify(reviewRepository).delete(review);
    }

    @Test
    void testDeleteByIdRejectsAReviewThatBelongsToSomeoneElse() {
        // Scoping the lookup by (id, user) together means a review that
        // exists but belongs to `attacker` looks identical, from `owner`'s
        // perspective, to a review that doesn't exist at all - both come
        // back empty from the repository call, and both surface as the
        // same 404 rather than leaking which case it was.
        when(reviewRepository.findByIdAndUser(5L, owner)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> reviewService.deleteById(5L, owner));
        assertEquals("Review not found with id: 5", ex.getMessage());
        verify(reviewRepository, never()).delete(Mockito.<Review>any());
    }

    @Test
    void testSubmitReviewRejectsRatingBelowRange() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.submitReview(book, owner, 0, "comment"));
        verify(reviewRepository, never()).save(Mockito.<Review>any());
    }

    @Test
    void testSubmitReviewRejectsRatingAboveRange() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.submitReview(book, owner, 6, "comment"));
        verify(reviewRepository, never()).save(Mockito.<Review>any());
    }

    @Test
    void testSubmitReviewUpsertsAnExistingReviewInsteadOfDuplicating() {
        Review existing = new Review(book, owner, 3, "It was okay.");
        when(reviewRepository.findByBookIdAndUser(book.getId(), owner)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(existing)).thenReturn(existing);

        Review result = reviewService.submitReview(book, owner, 5, "Actually, loved it.");

        assertEquals(5, result.getRating());
        assertEquals("Actually, loved it.", result.getComment());
        verify(reviewRepository).save(existing);
    }
}