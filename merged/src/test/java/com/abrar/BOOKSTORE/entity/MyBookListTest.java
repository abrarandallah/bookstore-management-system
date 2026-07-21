package com.abrar.BOOKSTORE.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MyBookListTest {
    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link MyBookList#MyBookList()}
     *   <li>{@link MyBookList#setAuthor(String)}
     *   <li>{@link MyBookList#setId(int)}
     *   <li>{@link MyBookList#setName(String)}
     *   <li>{@link MyBookList#setPrice(String)}
     *   <li>{@link MyBookList#getAuthor()}
     *   <li>{@link MyBookList#getId()}
     *   <li>{@link MyBookList#getName()}
     *   <li>{@link MyBookList#getPrice()}
     * </ul>
     */
    @Test
    void testConstructor() {
        MyBookList actualMyBookList = new MyBookList();
        actualMyBookList.setAuthor("JaneDoe");
        actualMyBookList.setId(1);
        actualMyBookList.setName("Name");
        actualMyBookList.setPrice("Price");
        String actualAuthor = actualMyBookList.getAuthor();
        int actualId = actualMyBookList.getId();
        String actualName = actualMyBookList.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
        assertEquals("Price", actualMyBookList.getPrice());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link MyBookList#MyBookList(int, String, String, String)}
     *   <li>{@link MyBookList#setAuthor(String)}
     *   <li>{@link MyBookList#setId(int)}
     *   <li>{@link MyBookList#setName(String)}
     *   <li>{@link MyBookList#setPrice(String)}
     *   <li>{@link MyBookList#getAuthor()}
     *   <li>{@link MyBookList#getId()}
     *   <li>{@link MyBookList#getName()}
     *   <li>{@link MyBookList#getPrice()}
     * </ul>
     */
    @Test
    void testConstructor2() {
        MyBookList actualMyBookList = new MyBookList(1, "Name", "JaneDoe", "Price");
        actualMyBookList.setAuthor("JaneDoe");
        actualMyBookList.setId(1);
        actualMyBookList.setName("Name");
        actualMyBookList.setPrice("Price");
        String actualAuthor = actualMyBookList.getAuthor();
        int actualId = actualMyBookList.getId();
        String actualName = actualMyBookList.getName();
        assertEquals("JaneDoe", actualAuthor);
        assertEquals(1, actualId);
        assertEquals("Name", actualName);
        assertEquals("Price", actualMyBookList.getPrice());
    }
}

