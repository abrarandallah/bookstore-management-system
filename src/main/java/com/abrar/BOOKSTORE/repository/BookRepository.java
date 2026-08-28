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

        // Separate query rather than an optional-genre param on search() above:
        // a LEFT JOIN with a nullable genreId would need SELECT DISTINCT to
        // dodge duplicate rows whenever a book has more than one genre, which
        // only matters once a genre filter is actually in play. Keeping the
        // unfiltered path untouched also means the existing search() tests and
        // their mocks don't need to change.
        @Query("SELECT DISTINCT b FROM Book b JOIN b.genres g "
                        + "WHERE (LOWER(b.name) LIKE LOWER(CONCAT('%', :term, '%')) "
                        + "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :term, '%'))) "
                        + "AND g.id = :genreId")
        List<Book> searchByGenre(@Param("term") String term, @Param("genreId") Integer genreId, Sort sort);

        // Every book currently tagged with a genre - used by GenreService#merge
        // to move each one onto the target genre before the source genre is
        // deleted.
        List<Book> findByGenres_Id(Integer genreId);
}