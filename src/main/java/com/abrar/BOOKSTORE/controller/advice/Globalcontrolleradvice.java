package com.abrar.BOOKSTORE.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerAdvice {

    // Readers hitting librarian-only pages (e.g. /book_register, /editBook/**)
    // get a plain, friendly message instead of Spring's default whitelabel error.
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(Model model) {
        model.addAttribute("message", "You don't have permission to do that.");
        return "error";
    }
}