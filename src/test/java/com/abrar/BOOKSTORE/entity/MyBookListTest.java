package com.abrar.BOOKSTORE.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.abrar.BOOKSTORE.Login.user.User;
import org.junit.jupiter.api.Test;

class MyBookListTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        MyBookList actual = new MyBookList();
        actual.setAuthor("JaneDoe");
        actual.setId(1L);
        actual.setBookId(7);
        actual.setName("Name");
        actual.setPrice("Price");
        actual.setUser(user);

        assertEquals("JaneDoe", actual.getAuthor());
        assertEquals(1L, actual.getId());
        assertEquals(7, actual.getBookId());
        assertEquals("Name", actual.getName());
        assertEquals("Price", actual.getPrice());
        assertSame(user, actual.getUser());
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User("reader", "reader@example.com", "hash", "ROLE_USER");
        MyBookList actual = new MyBookList(7, "Name", "JaneDoe", "Price", user);

        assertEquals(7, actual.getBookId());
        assertEquals("Name", actual.getName());
        assertEquals("JaneDoe", actual.getAuthor());
        assertEquals("Price", actual.getPrice());
        assertSame(user, actual.getUser());
    }
}