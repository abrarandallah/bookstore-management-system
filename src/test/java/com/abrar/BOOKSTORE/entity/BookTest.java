package com.abrar.BOOKSTORE.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class BookTest {
    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link Book#Book()}
     * <li>{@link Book#setAuthor(String)}
     * <li>{@link Book#setId(int)}
     * <li>{@link Book#setName(String)}
     * <li>{@link Book#getAuthor()}
     * <li>{@link Book#getId()}
     * <li>{@link Book#getName()}
     * </ul>
     */
    @Test
    void testConstructor() {
        Book actualBook = new Book();
        actualBook.setAuthor("JaneDoe");
        actualBook.setId(1);
        actualBook.setName("Name");
        String actualAuthor = actualBook.getAuthor();
        int actualId = actualBook.getId();
        String actualName = actualBook.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link Book#Book(int, String, String)}
     * <li>{@link Book#setAuthor(String)}
     * <li>{@link Book#setId(int)}
     * <li>{@link Book#setName(String)}
     * <li>{@link Book#getAuthor()}
     * <li>{@link Book#getId()}
     * <li>{@link Book#getName()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        Book actualBook = new Book(1, "Name", "JaneDoe");
        actualBook.setAuthor("JaneDoe");
        actualBook.setId(1);
        actualBook.setName("Name");
        String actualAuthor = actualBook.getAuthor();
        int actualId = actualBook.getId();
        String actualName = actualBook.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
    }

    @Test
    void testCoverImageUrl() {
        Book book = new Book();
        book.setCoverImageUrl("/uploads/covers/abc.jpg");
        assertEquals("/uploads/covers/abc.jpg", book.getCoverImageUrl());
    }

    @Test
    void testEstimatedReadMinutesWithNoTakeaways() {
        Book book = new Book();
        // Minimum of 1, even with nothing to read yet - never shows "0 min".
        assertEquals(1, book.getEstimatedReadMinutes());
    }

    @Test
    void testEstimatedReadMinutesScalesWithTakeaways() {
        Book book = new Book();
        List<BookPage> pages = new ArrayList<>();
        pages.add(new BookPage(1, "Heading 1", "Content 1"));
        pages.add(new BookPage(2, "Heading 2", "Content 2"));
        book.setTakeaways(pages);
        assertEquals(3, book.getEstimatedReadMinutes());
    }
}