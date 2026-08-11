package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.MyBookList;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Transactional
    void deleteByUser(User user);
}