package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.MyBookRepository;

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

@ContextConfiguration(classes = { MyBookListService.class })
@ExtendWith(SpringExtension.class)
class MyBookListServiceTest {
    @Autowired
    private MyBookListService myBookListService;

    @MockBean
    private MyBookRepository myBookRepository;

    private final User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");

    @Test
    void testSaveMyBooks() {
        MyBookList book = new MyBookList(1, "Name", "JaneDoe", user);
        when(myBookRepository.save(Mockito.<MyBookList>any())).thenReturn(book);

        myBookListService.saveMyBooks(book);
        verify(myBookRepository).save(Mockito.<MyBookList>any());
    }

    @Test
    void testGetMyBooksReturnsOnlyThatUsersEntries() {
        List<MyBookList> books = new ArrayList<>();
        books.add(new MyBookList(1, "Name", "JaneDoe", user));
        when(myBookRepository.findByUser(user)).thenReturn(books);

        List<MyBookList> actual = myBookListService.getMyBooks(user);
        assertEquals(1, actual.size());
        verify(myBookRepository).findByUser(user);
    }

    @Test
    void testAlreadyInList() {
        when(myBookRepository.existsByBookIdAndUser(1, user)).thenReturn(true);
        assertTrue(myBookListService.alreadyInList(1, user));
    }

    @Test
    void testDeleteByIdRemovesOwnedEntry() {
        MyBookList entry = new MyBookList(1, "Name", "JaneDoe", user);
        when(myBookRepository.findByIdAndUser(5L, user)).thenReturn(Optional.of(entry));
        doNothing().when(myBookRepository).delete(entry);

        myBookListService.deleteById(5L, user);
        verify(myBookRepository).delete(entry);
    }

    @Test
    void testDeleteByIdRejectsEntryThatIsntTheUsersOwn() {
        when(myBookRepository.findByIdAndUser(5L, user)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> myBookListService.deleteById(5L, user));
    }
}
