package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.BookValidator;
import com.abrar.BOOKSTORE.service.FileStorageService;
import com.abrar.BOOKSTORE.service.GenreService;
import com.abrar.BOOKSTORE.service.MyBookListService;
import com.abrar.BOOKSTORE.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(method = { RequestMethod.DELETE,
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT })
public class BookController {

    @Autowired
    private BookService service;
    @Autowired
    private MyBookListService myBookService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private BookValidator bookValidator;
    @Autowired
    private GenreService genreService;
    @Autowired
    private ReviewService reviewService;

    private User currentUser(Principal principal) {
        return userRepository.findByUsernameOrEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + principal.getName()));
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/book_register")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String BookRegister(Model model) {
        model.addAttribute("allGenres", genreService.findAll());
        return "bookRegister";
    }

    @GetMapping("/available_books")
    public String getAllBook(@RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "name_asc") String sort,
            @RequestParam(required = false) Integer genre, Model model) {
        List<Book> list = service.search(q, genre, sort);
        model.addAttribute("book", list);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("ratingSummaries", reviewService.summariesForAllBooks());
        return "bookList";
    }

    /**
     * Same data as {@link #getAllBook}, but returns just the results
     * fragment (grid + empty states) rather than the full page. Used by the
     * live/incremental search on the book list so typing doesn't trigger a
     * full page reload.
     */
    @GetMapping("/available_books/results")
    public String getAllBookResultsFragment(@RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "name_asc") String sort,
            @RequestParam(required = false) Integer genre, Model model) {
        List<Book> list = service.search(q, genre, sort);
        model.addAttribute("book", list);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("selectedGenre", genre);
        model.addAttribute("ratingSummaries", reviewService.summariesForAllBooks());
        return "bookList :: resultsFragment";
    }

    // "Surprise Me" on the home page - picks a random book and drops the
    // reader straight into it, rather than making them browse first.
    @GetMapping("/random_book")
    public String randomBook() {
        Book b = service.getRandomBook();
        return "redirect:/available_books/" + b.getId() + "/read";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String addBook(@ModelAttribute Book b, @RequestParam(required = false) MultipartFile cover,
            @RequestParam(required = false) String existingCoverImageUrl,
            @RequestParam(required = false) List<Integer> genreIds, Model model) {
        // Drop any takeaway rows the librarian left completely empty (extra
        // "add another" rows that never got filled in) before validating or
        // numbering the rest.
        Iterator<BookPage> it = b.getTakeaways().iterator();
        while (it.hasNext()) {
            BookPage p = it.next();
            boolean headingBlank = p.getHeading() == null || p.getHeading().isBlank();
            boolean contentBlank = p.getContent() == null || p.getContent().isBlank();
            if (headingBlank && contentBlank) {
                it.remove();
            }
        }
        int pageNumber = 1;
        for (BookPage p : b.getTakeaways()) {
            p.setBook(b);
            p.setPageNumber(pageNumber++);
        }

        String error = bookValidator.validate(b);
        if (error == null) {
            try {
                if (cover != null && !cover.isEmpty()) {
                    b.setCoverImageUrl(fileStorageService.store(cover, "covers"));
                } else {
                    b.setCoverImageUrl(existingCoverImageUrl);
                }
            } catch (IllegalArgumentException ex) {
                error = ex.getMessage();
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("book", b);
            model.addAttribute("allGenres", genreService.findAll());
            model.addAttribute("selectedGenreIds", genreIds == null ? List.of() : genreIds);
            // id is 0 for a brand-new book (never persisted), non-zero when editing
            return b.getId() == 0 ? "bookRegister" : "bookEdit";
        }
        b.setGenres(new LinkedHashSet<>(genreService.findAllById(genreIds)));
        service.save(b);
        return "redirect:/available_books";
    }

    @GetMapping("/my_books")
    public String getMyBooks(Model model, Principal principal) {
        List<MyBookList> list = myBookService.getMyBooks(currentUser(principal));
        model.addAttribute("book", list);
        return "myBooks";
    }

    // POST-only (not GET): these mutate data, and only state-changing methods
    // are covered by CSRF protection. A GET link here would be triggerable by
    // an <img>/prefetch/bookmark from anywhere, CSRF token or not.
    @PostMapping("/mylist/{id}")
    public String getMylist(@PathVariable("id") int id, Principal principal) {
        User user = currentUser(principal);
        if (!myBookService.alreadyInList(id, user)) {
            Book b = service.getBookById(id);
            MyBookList mb = new MyBookList(b.getId(), b.getName(), b.getAuthor(), user);
            myBookService.saveMyBooks(mb);
        }
        return "redirect:/my_books";
    }

    @GetMapping("/available_books/{id}/read")
    public String readBook(@PathVariable("id") int id, Model model, Principal principal) {
        Book b = service.getBookById(id);
        model.addAttribute("book", b);
        model.addAttribute("reviews", reviewService.getReviewsForBook(id));
        model.addAttribute("ratingSummary", reviewService.summaryForBook(id));
        model.addAttribute("ownReview", reviewService.findOwnReview(id, currentUser(principal)).orElse(null));
        return "bookRead";
    }

    @GetMapping("/editBook/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String editBook(@PathVariable("id") int id, Model model) {
        Book b = service.getBookById(id);
        model.addAttribute("book", b);
        model.addAttribute("allGenres", genreService.findAll());
        model.addAttribute("selectedGenreIds",
                b.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        return "bookEdit";
    }

    @PostMapping("/deleteBook/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String deleteBook(@PathVariable("id") int id) {
        service.deleteById(id);
        return "redirect:/available_books";
    }

    @PostMapping("/deleteMyBook/{id}")
    public String deleteMyBook(@PathVariable("id") long id, Principal principal) {
        myBookService.deleteById(id, currentUser(principal));
        return "redirect:/my_books";
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAnyRole('USER', 'LIBRARIAN')") // Secure this endpoint for authenticated users
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        // Your code to retrieve the book by ID
        Book book = service.getBookById(Math.toIntExact(id));
        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}