package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.MyBookListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String BookRegister() {
        return "bookRegister";
    }

    @GetMapping("/available_books")
    public ModelAndView getAllBook() {
        List<Book> list = service.getAllBook();
        return new ModelAndView("bookList", "book", list);
    }

    @PostMapping("/save")
    public String addBook(@Valid @ModelAttribute("book") Book b, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Please correct the highlighted fields and try again.");
            return "bookRegister";
        }
        service.save(b);
        return "redirect:/available_books";
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

    @RequestMapping("/editBook/{id}")
    public String editBook(@PathVariable("id") int id, Model model) {
        Book b = service.getBookById(id);
        model.addAttribute("book", b);
        return "bookEdit";
    }

    @RequestMapping("/deleteBook/{id}")
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
    @PreAuthorize("hasRole('USER')") // Secure this endpoint for authenticated users
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        try {
            Book book = service.getBookById(Math.toIntExact(id));
            return ResponseEntity.ok(book);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Centralized handling for the MVC endpoints in this controller: rather than
     * letting a missing book (unknown id, already deleted, etc.) surface as an
     * unhandled 500 error / NullPointerException, send the user back to the book
     * list with a friendly, flashed error message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/available_books";
    }

}