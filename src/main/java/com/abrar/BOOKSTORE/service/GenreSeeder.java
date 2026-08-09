package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Genre;
import com.abrar.BOOKSTORE.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Seeds a starter set of genres on first run, the same way LibrarianSeeder
// bootstraps the first librarian account - otherwise a brand-new install
// has no categories to assign at all until a librarian thinks to add some.
// Only runs when the genres table is empty, so it never overwrites or
// duplicates anything a librarian has already set up.
@Component
public class GenreSeeder implements CommandLineRunner {

    private static final List<String> DEFAULT_GENRES = List.of(
            "Business", "Psychology", "Self-Help", "Science",
            "History", "Technology", "Health & Wellness", "Philosophy");

    @Autowired
    private GenreRepository genreRepository;

    @Override
    public void run(String... args) {
        if (genreRepository.count() > 0) {
            return;
        }
        for (String name : DEFAULT_GENRES) {
            genreRepository.save(new Genre(name));
        }
    }
}