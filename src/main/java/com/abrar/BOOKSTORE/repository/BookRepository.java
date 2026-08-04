package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.Book;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query("SELECT b FROM Book b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :term, '%')) "
            + "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Book> search(@Param("term") String term, Sort sort);
}