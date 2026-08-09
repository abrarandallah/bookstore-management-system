package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> findAll() {
        return genreRepository.findAllByOrderByNameAsc();
    }

    public List<Genre> findAllById(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return genreRepository.findAllById(ids);
    }

    /**
     * @throws IllegalArgumentException if the name is blank or already
     *                                  taken (case-insensitive) - lets
     *                                  the controller show a friendly
     *                                  error rather than a raw
     *                                  constraint-violation stack trace.
     */
    public Genre create(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Genre name is required.");
        }
        if (genreRepository.existsByNameIgnoreCase(trimmed)) {
            throw new IllegalArgumentException("A genre named \"" + trimmed + "\" already exists.");
        }
        return genreRepository.save(new Genre(trimmed));
    }

    /**
     * Reuses an existing genre by name (case-insensitive) if one exists,
     * or creates it. Used by bulk import, where the same genre name is
     * likely to show up across many rows and shouldn't produce a
     * duplicate each time.
     */
    public Genre findOrCreateByName(String name) {
        String trimmed = name == null ? "" : name.trim();
        return genreRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> genreRepository.save(new Genre(trimmed)));
    }

    public void deleteById(int id) {
        genreRepository.deleteById(id);
    }

    /**
     * Every genre, in name order, mapped to how many books currently carry
     * it - including genres with zero books, so the browse page still
     * shows the full set of categories rather than only the ones already
     * in use.
     */
    // Transactional so both queries run in one persistence context: Genre
    // has no equals/hashCode override, so this map's keys only line up
    // correctly between the two loops below if Hibernate's session-level
    // identity map hands back the *same* Genre instance for a given id both
    // times, which only happens within a single transaction.
    @Transactional(readOnly = true)
    public Map<Genre, Long> allWithBookCounts() {
        Map<Genre, Long> counts = new LinkedHashMap<>();
        for (Genre g : findAll()) {
            counts.put(g, 0L);
        }
        for (Object[] row : genreRepository.countBooksByGenre()) {
            counts.put((Genre) row[0], (Long) row[1]);
        }
        return counts;
    }
}