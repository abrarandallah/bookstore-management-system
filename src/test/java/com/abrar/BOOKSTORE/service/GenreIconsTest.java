package com.abrar.BOOKSTORE.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GenreIconsTest {

    private final GenreIcons icons = new GenreIcons();

    @Test
    void matchesKeywordsCaseInsensitively() {
        assertEquals("fa-dragon", icons.iconFor("Fantasy"));
        assertEquals("fa-rocket", icons.iconFor("SCIENCE FICTION"));
    }

    @Test
    void specificKeywordBeatsGeneralOne() {
        assertEquals("fa-brain", icons.iconFor("Psychological Horror"));
        assertEquals("fa-scale-balanced", icons.iconFor("Legal Thriller"));
    }

    @Test
    void unknownOrNullFallsBackToOpenBook() {
        assertEquals("fa-book-open", icons.iconFor("Something New"));
        assertEquals("fa-book-open", icons.iconFor(null));
    }
}
