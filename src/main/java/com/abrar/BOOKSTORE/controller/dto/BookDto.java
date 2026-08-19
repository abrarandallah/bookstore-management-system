package com.abrar.BOOKSTORE.controller.dto;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.entity.BookPage;
import com.abrar.BOOKSTORE.entity.Genre;

import java.util.List;

/**
 * JSON shape for {@code GET /{id}} (see BookController). Replaces returning
 * the {@link Book} entity directly, which serialized its lazy
 * {@code takeaways}/{@code genres} collections straight from JPA - risking
 * a LazyInitializationException outside an open transaction/session, and
 * coupling the public API response shape 1:1 to the persistence model.
 */
public record BookDto(
        int id,
        String name,
        String author,
        String coverImageUrl,
        int estimatedReadMinutes,
        List<TakeawayDto> takeaways,
        List<String> genres) {

    public record TakeawayDto(long id, int pageNumber, String heading, String content) {
        public static TakeawayDto from(BookPage page) {
            return new TakeawayDto(page.getId(), page.getPageNumber(), page.getHeading(), page.getContent());
        }
    }

    public static BookDto from(Book book) {
        List<TakeawayDto> takeaways = book.getTakeaways().stream()
                .map(TakeawayDto::from)
                .toList();
        List<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .toList();
        return new BookDto(book.getId(), book.getName(), book.getAuthor(), book.getCoverImageUrl(),
                book.getEstimatedReadMinutes(), takeaways, genreNames);
    }
}