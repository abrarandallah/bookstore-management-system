package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @RequestParam(required = false) String comment, Principal principal,
            RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id);
        User user = currentUser(principal);
        try {
            reviewService.submitReview(book, user, rating, comment);
        } catch (IllegalArgumentException ex) {
            // Only reachable via a tampered request - the star widget on
            // the reader page only ever submits 1-5. Redirect back to the
            // reader (GET) with the error flashed (a translation key) instead
            // of re-rendering bookRead from here: that page needs many other
            // model values (translated title and takeaways, resume position,
            // ...) that only BookController#readBook knows how to build, and
            // leaving any out made the page fail to render.
            redirectAttributes.addFlashAttribute("reviewError", "review.error.rating.range");
        }
        return "redirect:/available_books/" + id + "/read";
    }

    @PostMapping("/available_books/{bookId}/review/{reviewId}/delete")
    public String deleteReview(@PathVariable int bookId, @PathVariable long reviewId, Principal principal) {
        reviewService.deleteById(reviewId, currentUser(principal));
        return "redirect:/available_books/" + bookId + "/read";
    }
}