package com.abrar.BOOKSTORE.controller.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class LanguagePopupModelAdviceTest {

    private final LanguagePopupModelAdvice advice = new LanguagePopupModelAdvice();

    @Test
    void keepsThePathOfNormalPages() {
        assertEquals("/genres", advice.currentPath(new MockHttpServletRequest("GET", "/genres")));
    }

    @Test
    void keepsTheQueryStringOnlyOnTheBookList() {
        MockHttpServletRequest list = new MockHttpServletRequest("GET", "/available_books");
        list.setQueryString("genre=5&q=dune");
        assertEquals("/available_books?genre=5&q=dune", advice.currentPath(list));
    }

    @Test
    void dropsTheQueryStringElsewhereBecauseItMayHoldAToken() {
        MockHttpServletRequest reset = new MockHttpServletRequest("GET", "/reset-password");
        reset.setQueryString("token=secret");
        assertEquals("/reset-password", advice.currentPath(reset));
    }

    @Test
    void errorPagesGoHome() {
        assertEquals("/", advice.currentPath(new MockHttpServletRequest("GET", "/error")));
    }
}
