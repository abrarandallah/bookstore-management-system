package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.repository.BookRepository;
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

    @Autowired
    private BookRepository bookRepository;

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
     * Reuses an existing genre by name (case-insensitive) if one exists,
     * or creates it. Used by bulk import, where the same genre name is
     * likely to show up across many rows and shouldn't produce a
     * duplicate each time. Also the only way a brand-new genre gets
     * created now - see BookImportController and the note on the Add
     * Books page's genre picker.
     */
    public Genre findOrCreateByName(String name) {
        String trimmed = name == null ? "" : name.trim();
        return genreRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> genreRepository.save(new Genre(trimmed)));
    }

    /**
     * Renames a genre in place. Rejected if another genre already has that
     * name (case-insensitively, same rule findOrCreateByName uses) - fixing
     * a typo shouldn't be able to silently collide two genres into one name
     * while leaving both rows behind; merge() is the explicit way to do
     * that instead.
     */
    @Transactional
    public void rename(int id, String newName) {
        String trimmed = newName == null ? "" : newName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Genre name can't be empty.");
        }
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found."));
        genreRepository.findByNameIgnoreCase(trimmed).ifPresent(existing -> {
            if (existing.getId() != id) {
                throw new IllegalArgumentException(
                        "\"" + trimmed + "\" already exists - merge into it instead of renaming to a duplicate.");
            }
        });
        genre.setName(trimmed);
        genreRepository.save(genre);
    }

    /**
     * Moves every book tagged with {@code sourceId} onto {@code targetId},
     * then deletes the now-empty source genre. Used to clean up a
     * genuine duplicate (two different spellings of the same genre) -
     * findOrCreateByName's case-insensitive match already prevents casing
     * duplicates on import, this is for the ones it can't catch.
     */
    @Transactional
    public void merge(int sourceId, int targetId) {
        if (sourceId == targetId) {
            throw new IllegalArgumentException("Can't merge a genre into itself.");
        }
        Genre source = genreRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found."));
        Genre target = genreRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target genre not found."));
        // Book owns the book_genres join table, so reassigning happens
        // through each Book's genres set rather than a Genre-side field.
        // source/target were loaded above through genreRepository in this
        // same transaction, so Hibernate's session-level identity map hands
        // back those exact same instances here too - Genre has no
        // equals/hashCode override (see its class comment), so this Set's
        // add()/remove() only work correctly because of that, not because
        // of value equality.
        for (Book book : bookRepository.findByGenres_Id(sourceId)) {
            book.getGenres().remove(source);
            book.getGenres().add(target);
            bookRepository.save(book);
        }
        genreRepository.delete(source);
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