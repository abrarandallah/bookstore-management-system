package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.repository.BookRepository;
import com.abrar.BOOKSTORE.repository.GenreRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { GenreService.class })
@ExtendWith(SpringExtension.class)
class GenreServiceTest {

    @Autowired
    private GenreService genreService;

    @MockBean
    private GenreRepository genreRepository;

    @MockBean
    private BookRepository bookRepository;

    private Genre genreWithId(int id, String name) {
        Genre genre = new Genre(name);
        genre.setId(id);
        return genre;
    }

    @Test
    void testRenameUpdatesTheName() {
        Genre genre = genreWithId(1, "Scince Fiction");
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));
        when(genreRepository.findByNameIgnoreCase("Science Fiction")).thenReturn(Optional.empty());

        genreService.rename(1, "Science Fiction");

        assertEquals("Science Fiction", genre.getName());
        verify(genreRepository).save(genre);
    }

    @Test
    void testRenameRejectsBlankName() {
        Genre genre = genreWithId(1, "Sci-Fi");
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));

        assertThrows(IllegalArgumentException.class, () -> genreService.rename(1, "   "));
        assertEquals("Sci-Fi", genre.getName());
    }

    @Test
    void testRenameRejectsWhenNameAlreadyUsedByAnotherGenre() {
        Genre genre = genreWithId(1, "Sci-Fi");
        Genre otherGenre = genreWithId(2, "Fantasy");
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(otherGenre));

        assertThrows(IllegalArgumentException.class, () -> genreService.rename(1, "Fantasy"));
        assertEquals("Sci-Fi", genre.getName());
    }

    @Test
    void testMergeMovesBooksOntoTargetAndDeletesSource() {
        Genre source = genreWithId(1, "Sci-Fi");
        Genre target = genreWithId(2, "Science Fiction");
        Book book = new Book(10, "Dune", "Frank Herbert");
        book.getGenres().add(source);
        when(genreRepository.findById(1)).thenReturn(Optional.of(source));
        when(genreRepository.findById(2)).thenReturn(Optional.of(target));
        when(bookRepository.findByGenres_Id(1)).thenReturn(List.of(book));

        genreService.merge(1, 2);

        assertFalse(book.getGenres().contains(source));
        assertTrue(book.getGenres().contains(target));
        verify(bookRepository).save(book);
        verify(genreRepository).delete(source);
    }

    @Test
    void testMergeDoesNotDuplicateWhenABookAlreadyHasBothGenres() {
        // The book on the shelf under both the typo'd genre and the
        // already-correct one - merging shouldn't leave it double-tagged
        // (or crash trying to).
        Genre source = genreWithId(1, "Sci-Fi");
        Genre target = genreWithId(2, "Science Fiction");
        Book book = new Book(10, "Dune", "Frank Herbert");
        book.getGenres().add(source);
        book.getGenres().add(target);
        when(genreRepository.findById(1)).thenReturn(Optional.of(source));
        when(genreRepository.findById(2)).thenReturn(Optional.of(target));
        when(bookRepository.findByGenres_Id(1)).thenReturn(List.of(book));

        genreService.merge(1, 2);

        assertEquals(1, book.getGenres().size());
        assertTrue(book.getGenres().contains(target));
    }

    @Test
    void testMergeRejectsMergingIntoItself() {
        assertThrows(IllegalArgumentException.class, () -> genreService.merge(1, 1));
    }
}