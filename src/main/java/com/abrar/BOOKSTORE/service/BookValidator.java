package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import org.springframework.stereotype.Component;

@Component
public class BookValidator {

    /**
     * @return a message KEY (see messages.properties; templates translate it), or null if the book is valid.
     */
    public String validate(Book b) {
        if (b.getName() == null || b.getName().isBlank()) {
            return "book.error.name.required";
        }
        if (b.getAuthor() == null || b.getAuthor().isBlank()) {
            return "book.error.author.required";
        }
        if (b.getTakeaways().isEmpty()) {
            return "book.error.takeaways.min";
        }
        if (b.getTakeaways().size() > 10) {
            return "book.error.takeaways.max";
        }
        for (BookPage p : b.getTakeaways()) {
            boolean headingBlank = p.getHeading() == null || p.getHeading().isBlank();
            boolean contentBlank = p.getContent() == null || p.getContent().isBlank();
            if (headingBlank || contentBlank) {
                return "book.error.takeaway.incomplete";
            }
        }
        return null;
    }
}