package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.MyBookList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyBookRepository extends JpaRepository<MyBookList, Long> {
    List<MyBookList> findByUser(User user);

    // Scoped to the owning user, not just the row id - this is what stops one
    // user from deleting another user's list entry by guessing/incrementing
    // ids in the delete request.
    Optional<MyBookList> findByIdAndUser(long id, User user);

    boolean existsByBookIdAndUser(int bookId, User user);

    // Bulk deletes - see the comment on ReviewRepository's equivalents for
    // why these use @Modifying + an explicit query instead of a plain
    // deleteByX method.
    @Modifying
    @Transactional
    @Query("DELETE FROM MyBookList m WHERE m.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM MyBookList m WHERE m.bookId = :bookId")
    void deleteByBookId(@Param("bookId") int bookId);
}