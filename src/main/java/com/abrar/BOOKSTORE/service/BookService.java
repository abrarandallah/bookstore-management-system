package com.abrar.BOOKSTORE.service;

import com.abrar.BOOKSTORE.entity.Book;
import com.abrar.BOOKSTORE.repository.BookRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bRepo;
    public void save(Book b){
        bRepo.save(b);
    }
    public List<Book> getAllBook(){
        return bRepo.findAll();
    }
    public Book getBookById(int id){
        if (bRepo.findById(id).isPresent()) {
            return bRepo.findById(id).get();
        } else {
            return null;
        }
    }
    public void deleteById(int id){
        bRepo.deleteById(id);
    }
}
