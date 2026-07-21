package com.abrar.BOOKSTORE.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link Book#Book()}
     *   <li>{@link Book#setAuthor(String)}
     *   <li>{@link Book#setId(int)}
     *   <li>{@link Book#setName(String)}
     *   <li>{@link Book#setPrice(String)}
     *   <li>{@link Book#getAuthor()}
     *   <li>{@link Book#getId()}
     *   <li>{@link Book#getName()}
     *   <li>{@link Book#getPrice()}
     * </ul>
     */
    @Test
    void testConstructor() {
        Book actualBook = new Book();
        actualBook.setAuthor("JaneDoe");
        actualBook.setId(1);
        actualBook.setName("Name");
        actualBook.setPrice("Price");
        String actualAuthor = actualBook.getAuthor();
        int actualId = actualBook.getId();
        String actualName = actualBook.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
        assertEquals("Price", actualBook.getPrice());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link Book#Book(int, String, String, String)}
     *   <li>{@link Book#setAuthor(String)}
     *   <li>{@link Book#setId(int)}
     *   <li>{@link Book#setName(String)}
     *   <li>{@link Book#setPrice(String)}
     *   <li>{@link Book#getAuthor()}
     *   <li>{@link Book#getId()}
     *   <li>{@link Book#getName()}
     *   <li>{@link Book#getPrice()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        Book actualBook = new Book(1, "Name", "JaneDoe", "Price");
        actualBook.setAuthor("JaneDoe");
        actualBook.setId(1);
        actualBook.setName("Name");
        actualBook.setPrice("Price");
        String actualAuthor = actualBook.getAuthor();
        int actualId = actualBook.getId();
        String actualName = actualBook.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
        assertEquals("Price", actualBook.getPrice());
    }
}

