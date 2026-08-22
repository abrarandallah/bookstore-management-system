package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GenreController {

    @Autowired
    private GenreService genreService;

    // Public browse page: every genre on the shelf with how many books
    // carry it, linking into the filtered book list. No @PreAuthorize -
    // like /available_books, this only needs to be logged in, which
    // SecurityConfig already requires for anything not explicitly
    // whitelisted.
    @GetMapping("/genres")
    public String browseGenres(Model model) {
        model.addAttribute("genreCounts", genreService.allWithBookCounts());
        return "genres";
    }
}