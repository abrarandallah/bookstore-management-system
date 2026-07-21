package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.repository.MyBookRepository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {MyBookListService.class})
@ExtendWith(SpringExtension.class)
class MyBookListServiceTest {
    @Autowired
    private MyBookListService myBookListService;

    @MockBean
    private MyBookRepository myBookRepository;

    /**
     * Method under test: {@link MyBookListService#saveMyBooks(MyBookList)}
     */
    @Test
    void testSaveMyBooks() {
        MyBookList myBookList = new MyBookList();
        myBookList.setAuthor("JaneDoe");
        myBookList.setId(1);
        myBookList.setName("Name");
        myBookList.setPrice("Price");
        when(myBookRepository.save(Mockito.<MyBookList>any())).thenReturn(myBookList);

        MyBookList book = new MyBookList();
        book.setAuthor("JaneDoe");
        book.setId(1);
        book.setName("Name");
        book.setPrice("Price");
        myBookListService.saveMyBooks(book);
        verify(myBookRepository).save(Mockito.<MyBookList>any());
        assertEquals("JaneDoe", book.getAuthor());
        assertEquals("Price", book.getPrice());
        assertEquals("Name", book.getName());
        assertEquals(1, book.getId());
        assertTrue(myBookListService.getAllMyBooks().isEmpty());
    }

    /**
     * Method under test: {@link MyBookListService#getAllMyBooks()}
     */
    @Test
    void testGetAllMyBooks() {
        ArrayList<MyBookList> myBookListList = new ArrayList<>();
        when(myBookRepository.findAll()).thenReturn(myBookListList);
        List<MyBookList> actualAllMyBooks = myBookListService.getAllMyBooks();
        assertSame(myBookListList, actualAllMyBooks);
        assertTrue(actualAllMyBooks.isEmpty());
        verify(myBookRepository).findAll();
    }

    /**
     * Method under test: {@link MyBookListService#deleteById(int)}
     */
    @Test
    void testDeleteById() {
        doNothing().when(myBookRepository).deleteById(Mockito.<Integer>any());
        myBookListService.deleteById(1);
        verify(myBookRepository).deleteById(Mockito.<Integer>any());
        assertTrue(myBookListService.getAllMyBooks().isEmpty());
    }
}

