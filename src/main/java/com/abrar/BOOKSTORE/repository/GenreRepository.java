package com.abrar.BOOKSTORE.repository;

import com.abrar.BOOKSTORE.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findAllByOrderByNameAsc();

    Optional<Genre> findByNameIgnoreCase(String name);

    // Genre doesn't hold a reference back to its books (see Genre's class
    // comment), so the count is queried from the Book side of the
    // relationship instead of an object-graph traversal. Genres with zero
    // books are intentionally left out here - the service layer fills those
    // back in at zero so browsing still shows every category.
    @Query("SELECT g, COUNT(b) FROM Book b JOIN b.genres g GROUP BY g")
    List<Object[]> countBooksByGenre();
}