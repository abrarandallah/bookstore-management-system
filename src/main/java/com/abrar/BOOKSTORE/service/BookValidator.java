package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookValidator {

    /**
     * @return a human-readable error message, or null if the book is valid.
     */
    public String validate(Book b) {
        if (b.getName() == null || b.getName().isBlank()) {
            return "Book name is required.";
        }
        if (b.getAuthor() == null || b.getAuthor().isBlank()) {
            return "Author is required.";
        }
        if (b.getPrice() == null || b.getPrice().isBlank()) {
            return "Price is required.";
        }
        try {
            if (new BigDecimal(b.getPrice().trim()).signum() < 0) {
                return "Price cannot be negative.";
            }
        } catch (NumberFormatException ex) {
            return "Price must be a valid number.";
        }
        if (b.getTakeaways().isEmpty()) {
            return "Add at least 1 takeaway.";
        }
        if (b.getTakeaways().size() > 10) {
            return "You can have at most 10 takeaways.";
        }
        for (BookPage p : b.getTakeaways()) {
            boolean headingBlank = p.getHeading() == null || p.getHeading().isBlank();
            boolean contentBlank = p.getContent() == null || p.getContent().isBlank();
            if (headingBlank || contentBlank) {
                return "Takeaway " + p.getPageNumber() + " needs both a heading and content.";
            }
        }
        return null;
    }
}