package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.Login.user.User;
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

    public boolean alreadyInList(int bookId, User user) {
        return mybook.existsByBookIdAndUser(bookId, user);
    }

    public List<MyBookList> getMyBooks(User user) {
        return mybook.findByUser(user);
    }

    /**
     * @throws ResourceNotFoundException if no entry with this id exists for
     *                                   this specific user - either it never
     *                                   existed, or it belongs to someone
     *                                   else, and either way we don't
     *                                   distinguish the two in the error.
     */
    public void deleteById(long id, User user) {
        MyBookList entry = mybook.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found in your list with id: " + id));
        mybook.delete(entry);
    }
}