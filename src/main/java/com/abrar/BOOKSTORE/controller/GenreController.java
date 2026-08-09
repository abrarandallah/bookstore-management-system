package com.abrar.BOOKSTORE.controller;

import com.abrar.BOOKSTORE.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/admin/genres")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String manageGenres(Model model) {
        model.addAttribute("genreCounts", genreService.allWithBookCounts());
        return "admin/genres";
    }

    @PostMapping("/admin/genres")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String createGenre(@RequestParam String name, Model model) {
        try {
            genreService.create(name);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("genreCounts", genreService.allWithBookCounts());
            return "admin/genres";
        }
        return "redirect:/admin/genres";
    }

    // POST-only, same reasoning as the other admin delete actions: this
    // mutates data, so it needs to be behind CSRF protection, which only
    // covers state-changing (non-GET) requests.
    @PostMapping("/admin/genres/{id}/delete")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String deleteGenre(@PathVariable int id) {
        genreService.deleteById(id);
        return "redirect:/admin/genres";
    }
}