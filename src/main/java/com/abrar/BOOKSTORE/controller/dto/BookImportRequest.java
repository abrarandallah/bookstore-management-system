package com.abrar.BOOKSTORE.controller.dto;

import java.util.List;

public class BookImportRequest {
    private String name;
    private String author;
    private List<TakeawayImportRequest> takeaways;
    // Optional - genre names to tag the book with. Each name is matched
    // case-insensitively against existing genres and created if it doesn't
    // exist yet (see GenreService.findOrCreateByName), so imports don't
    // fail just because a category hasn't been set up beforehand.
    private List<String> genres;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<TakeawayImportRequest> getTakeaways() {
        return takeaways;
    }

    public void setTakeaways(List<TakeawayImportRequest> takeaways) {
        this.takeaways = takeaways;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }
}