package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
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