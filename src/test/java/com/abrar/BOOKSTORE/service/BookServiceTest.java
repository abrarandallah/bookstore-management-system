package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { BookService.class })
@ExtendWith(SpringExtension.class)
class BookServiceTest {
    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    /**
     * Method under test: {@link BookService#save(Book)}
     */
    @Test
    void testSave() {
        Book book = new Book();
        book.setAuthor("JaneDoe");
        book.setId(1);
        book.setName("Name");
        book.setPrice("Price");
        when(bookRepository.save(Mockito.<Book>any())).thenReturn(book);

        Book b = new Book();
        b.setAuthor("JaneDoe");
        b.setId(1);
        b.setName("Name");
        b.setPrice("Price");
        bookService.save(b);
        verify(bookRepository).save(Mockito.<Book>any());
        assertEquals("JaneDoe", b.getAuthor());
        assertEquals("Price", b.getPrice());
        assertEquals("Name", b.getName());
        assertEquals(1, b.getId());
        assertTrue(bookService.getAllBook().isEmpty());
    }

    /**
     * Method under test: {@link BookService#getAllBook()}
     */
    @Test
    void testGetAllBook() {
        ArrayList<Book> bookList = new ArrayList<>();
        when(bookRepository.findAll()).thenReturn(bookList);
        List<Book> actualAllBook = bookService.getAllBook();
        assertSame(bookList, actualAllBook);
        assertTrue(actualAllBook.isEmpty());
        verify(bookRepository).findAll();
    }

    /**
     * Method under test: {@link BookService#getBookById(int)}
     */
    @Test
    void testGetBookById() {
        Book book = new Book();
        book.setAuthor("JaneDoe");
        book.setId(1);
        book.setName("Name");
        book.setPrice("Price");
        Optional<Book> ofResult = Optional.of(book);
        when(bookRepository.findById(Mockito.<Integer>any())).thenReturn(ofResult);
        assertSame(book, bookService.getBookById(1));
        verify(bookRepository, atLeast(1)).findById(Mockito.<Integer>any());
    }

    /**
     * Method under test: {@link BookService#getBookById(int)}
     */
    @Test
    void testGetBookById2() {
        Optional<Book> emptyResult = Optional.empty();
        when(bookRepository.findById(Mockito.<Integer>any())).thenReturn(emptyResult);
        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(1));
        verify(bookRepository).findById(Mockito.<Integer>any());
    }

    /**
     * Method under test: {@link BookService#deleteById(int)}
     */
    @Test
    void testDeleteById() {
        when(bookRepository.existsById(Mockito.<Integer>any())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(Mockito.<Integer>any());
        bookService.deleteById(1);
        verify(bookRepository).deleteById(Mockito.<Integer>any());
        assertTrue(bookService.getAllBook().isEmpty());
    }

    /**
     * Method under test: {@link BookService#deleteById(int)}
     */
    @Test
    void testDeleteByIdNotFound() {
        when(bookRepository.existsById(Mockito.<Integer>any())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteById(1));
    }
}