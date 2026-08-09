package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class ReviewController {

    @Autowired
    private BookService bookService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;

    private User currentUser(Principal principal) {
        return userRepository.findByUsernameOrEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + principal.getName()));
    }

    // POST-only (not GET): this mutates data, and only state-changing
    // methods are covered by CSRF protection - same reasoning as every
    // other write endpoint in this app (see BookController.getMylist).
    // Re-submitting is an upsert (see ReviewService.submitReview), so this
    // one endpoint covers both "leave a review" and "edit my review".
    @PostMapping("/available_books/{id}/review")
    public String submitReview(@PathVariable("id") int id, @RequestParam int rating,
            @RequestParam(required = false) String comment, Principal principal, Model model) {
        Book book = bookService.getBookById(id);
        User user = currentUser(principal);
        try {
            reviewService.submitReview(book, user, rating, comment);
        } catch (IllegalArgumentException ex) {
            // Only reachable via a tampered request - the star widget on
            // the reader page only ever submits 1-5. Re-render the read
            // page directly (rather than redirect+flash) to match how
            // BookController.addBook() handles validation failures
            // elsewhere in this app.
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviewService.getReviewsForBook(id));
            model.addAttribute("ownReview", reviewService.findOwnReview(id, user).orElse(null));
            model.addAttribute("ratingSummary", reviewService.summaryForBook(id));
            model.addAttribute("reviewError", ex.getMessage());
            return "bookRead";
        }
        return "redirect:/available_books/" + id + "/read";
    }

    @PostMapping("/available_books/{bookId}/review/{reviewId}/delete")
    public String deleteReview(@PathVariable int bookId, @PathVariable long reviewId, Principal principal) {
        reviewService.deleteById(reviewId, currentUser(principal));
        return "redirect:/available_books/" + bookId + "/read";
    }
}