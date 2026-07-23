package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.MyBookList;
import com.abrar.BOOKSTORE.exception.ResourceNotFoundException;
import com.abrar.BOOKSTORE.repository.MyBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MyBookListService {

    @Autowired
    private MyBookRepository mybook;

    public void saveMyBooks(MyBookList book) {
        mybook.save(book);
    }

    public List<MyBookList> getAllMyBooks() {
        return mybook.findAll();

    }

    /**
     * @throws ResourceNotFoundException if no entry exists with the given id,
     *                                   instead of letting an
     *                                   EmptyResultDataAccessException escape.
     */
    public void deleteById(int id) {
        if (!mybook.existsById(id)) {
            throw new ResourceNotFoundException("Book not found in your list with id: " + id);
        }
        mybook.deleteById(id);
    }

}