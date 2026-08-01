package com.abrar.BOOKSTORE.controller.advice;

import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
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

    // Covers every MVC controller (editBook/{id}, deleteBook/{id}, mylist/{id},
    // deleteMyBook/{id}, etc.) hitting a stale/missing id.
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}