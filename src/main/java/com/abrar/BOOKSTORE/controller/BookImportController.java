package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.controller.dto.BookImportRequest;
import com.abrar.BOOKSTORE.controller.dto.TakeawayImportRequest;
import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.service.BookService;
import com.abrar.BOOKSTORE.service.BookValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/books")
@PreAuthorize("hasRole('LIBRARIAN')")
public class BookImportController {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookValidator bookValidator;

    private static final String EXAMPLE_JSON = """
            [
              {
                "name": "The Art of Unfinished Things",
                "author": "Laila Morningside",
                "takeaways": [
                  { "heading": "Start Before You're Ready", "content": "Progress begins when you stop waiting for perfect conditions." },
                  { "heading": "Celebrate Incompletion", "content": "Unfinished projects hold lessons just as valuable as finished ones." }
                ]
              }
            ]""";

    @GetMapping("/import")
    public String importForm(Model model) {
        model.addAttribute("exampleJson", EXAMPLE_JSON);
        return "admin/import";
    }

    @PostMapping("/import")
    public String runImport(@RequestParam String json, Model model) {
        model.addAttribute("exampleJson", EXAMPLE_JSON);
        model.addAttribute("submittedJson", json);

        List<BookImportRequest> requests;
        try {
            ObjectMapper mapper = new ObjectMapper();
            requests = mapper.readValue(json, new TypeReference<List<BookImportRequest>>() {
            });
        } catch (Exception ex) {
            model.addAttribute("error", "Couldn't parse that as JSON: " + ex.getMessage());
            return "admin/import";
        }

        if (requests.isEmpty()) {
            model.addAttribute("error", "That list was empty - nothing to import.");
            return "admin/import";
        }

        int imported = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            BookImportRequest req = requests.get(i);
            Book book = new Book();
            book.setName(req.getName());
            book.setAuthor(req.getAuthor());

            List<BookPage> pages = new ArrayList<>();
            if (req.getTakeaways() != null) {
                int pageNumber = 1;
                for (TakeawayImportRequest t : req.getTakeaways()) {
                    BookPage page = new BookPage(pageNumber++, t.getHeading(), t.getContent());
                    page.setBook(book);
                    pages.add(page);
                }
            }
            book.setTakeaways(pages);

            String label = (req.getName() == null || req.getName().isBlank()) ? "row " + (i + 1) : req.getName();
            String error = bookValidator.validate(book);
            if (error != null) {
                errors.add(label + ": " + error);
                continue;
            }
            bookService.save(book);
            imported++;
        }

        model.addAttribute("message", imported + " of " + requests.size() + " book(s) imported.");
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
        }
        return "admin/import";
    }
}