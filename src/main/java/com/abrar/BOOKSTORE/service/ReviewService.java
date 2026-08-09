package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.Review;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsForBook(int bookId) {
        return reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId);
    }

    public Optional<Review> findOwnReview(int bookId, User user) {
        return reviewRepository.findByBookIdAndUser(bookId, user);
    }

    /**
     * Creates the user's review for this book, or updates it if one
     * already exists - one review per user per book, so re-submitting the
     * form from the reader page is always an edit rather than piling up
     * duplicates.
     *
     * @throws IllegalArgumentException if rating is outside 1-5.
     */
    public Review submitReview(Book book, User user, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        String trimmedComment = comment == null ? null : comment.trim();
        if (trimmedComment != null && trimmedComment.isEmpty()) {
            trimmedComment = null;
        }
        // Lambdas can only capture effectively-final locals, and
        // trimmedComment gets reassigned just above - so it's handed to
        // the lambda through this separate final variable instead.
        final String commentForNewReview = trimmedComment;
        Review review = reviewRepository.findByBookIdAndUser(book.getId(), user)
                .orElseGet(() -> new Review(book, user, rating, commentForNewReview));
        review.setRating(rating);
        review.setComment(trimmedComment);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    /**
     * @throws ResourceNotFoundException if no review with this id exists
     *                                   for this specific user - either it
     *                                   never existed, or it belongs to
     *                                   someone else, and either way we
     *                                   don't distinguish the two in the
     *                                   error.
     */
    public void deleteById(long id, User user) {
        Review review = reviewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }

    public RatingSummary summaryForBook(int bookId) {
        Double avg = reviewRepository.averageRatingForBook(bookId);
        long count = reviewRepository.countByBookId(bookId);
        return new RatingSummary(avg == null ? 0.0 : avg, count);
    }

    /** bookId -> summary, for every book that has at least one review. */
    public Map<Integer, RatingSummary> summariesForAllBooks() {
        Map<Integer, RatingSummary> summaries = new HashMap<>();
        for (Object[] row : reviewRepository.ratingSummaries()) {
            int bookId = (Integer) row[0];
            double avg = (Double) row[1];
            long count = (Long) row[2];
            summaries.put(bookId, new RatingSummary(avg, count));
        }
        return summaries;
    }
}