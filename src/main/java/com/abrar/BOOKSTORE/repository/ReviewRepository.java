package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookIdOrderByCreatedAtDesc(int bookId);

    Optional<Review> findByBookIdAndUser(int bookId, User user);

    // Scoped to the owning user, not just the row id - same reasoning as
    // MyBookRepository.findByIdAndUser: stops one user from deleting
    // another user's review by guessing/incrementing ids.
    Optional<Review> findByIdAndUser(long id, User user);

    long countByBookId(int bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double averageRatingForBook(@Param("bookId") int bookId);

    // Bulk per-book average + count in one pass, for the book list where
    // showing a rating on every card would otherwise mean two queries per
    // book (an N+1 pattern) instead of one query total.
    @Query("SELECT r.book.id, AVG(r.rating), COUNT(r) FROM Review r GROUP BY r.book.id")
    List<Object[]> ratingSummaries();

    @Transactional
    void deleteByUser(User user);
}