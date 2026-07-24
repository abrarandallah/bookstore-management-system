package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.MyBookListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping(method = { RequestMethod.DELETE,
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT })
public class BookController {

    @Autowired
    private BookService service;
    @Autowired
    private MyBookListService myBookService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/book_register")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String BookRegister() {
        return "bookRegister";
    }

    @GetMapping("/available_books")
    public ModelAndView getAllBook() {
        List<Book> list = service.getAllBook();
        return new ModelAndView("bookList", "book", list);
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String addBook(@ModelAttribute Book b, Model model) {
        String error = validate(b);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("book", b);
            // id is 0 for a brand-new book (never persisted), non-zero when editing
            return b.getId() == 0 ? "bookRegister" : "bookEdit";
        }
        service.save(b);
        return "redirect:/available_books";
    }

    private String validate(Book b) {
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
        return null;
    }

    @GetMapping("/my_books")
    public String getMyBooks(Model model) {
        List<MyBookList> list = myBookService.getAllMyBooks();
        model.addAttribute("book", list);
        return "myBooks";
    }

    @RequestMapping("/mylist/{id}")
    public String getMylist(@PathVariable("id") int id) {
        Book b = service.getBookById(id);
        MyBookList mb = new MyBookList(b.getId(), b.getName(), b.getAuthor(), b.getPrice());
        myBookService.saveMyBooks(mb);
        return "redirect:/my_books";
    }

    @GetMapping("/editBook/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String editBook(@PathVariable("id") int id, Model model) {
        Book b = service.getBookById(id);
        model.addAttribute("book", b);
        return "bookEdit";
    }

    @RequestMapping("/deleteBook/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String deleteBook(@PathVariable("id") int id) {
        service.deleteById(id);
        return "redirect:/available_books";
    }

    @RequestMapping("/deleteMyBook/{id}")
    public String deleteMyBook(@PathVariable("id") int id) {
        myBookService.deleteById(id);
        return "redirect:/available_books";
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