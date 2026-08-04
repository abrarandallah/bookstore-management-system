package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bRepo;

    public void save(Book b) {
        bRepo.save(b);
    }

    public List<Book> getAllBook() {
        return bRepo.findAll();
    }

    /**
     * @param term   free-text match against name/author, or blank for
     *               everything.
     * @param sortBy one of: name_asc, author_asc, price_asc, price_desc,
     *               newest. Falls back to name_asc for anything else, rather
     *               than erroring on an unrecognized/tampered value.
     */
    public List<Book> search(String term, String sortBy) {
        Sort sort = switch (sortBy == null ? "" : sortBy) {
            case "author_asc" -> Sort.by("author").ascending();
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            // No createdAt column exists, but id is auto-incrementing, so
            // higher id reliably means "added more recently".
            case "newest" -> Sort.by("id").descending();
            default -> Sort.by("name").ascending();
        };
        String safeTerm = term == null ? "" : term.trim();
        return bRepo.search(safeTerm, sort);
    }

    /**
     * @throws ResourceNotFoundException if no book exists with the given id.
     *                                   Previously this returned null, which caused
     *                                   NullPointerExceptions in
     *                                   every caller (editBook, getMylist, etc.)
     *                                   whenever an id didn't exist.
     */
    public Book getBookById(int id) {
        return bRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    /**
     * @throws ResourceNotFoundException if no book exists with the given id,
     *                                   instead of letting an
     *                                   EmptyResultDataAccessException escape from
     *                                   deleteById().
     */
    public void deleteById(int id) {
        if (!bRepo.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bRepo.deleteById(id);
    }
}