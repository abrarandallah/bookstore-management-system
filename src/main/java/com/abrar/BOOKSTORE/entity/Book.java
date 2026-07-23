package com.abrar.BOOKSTORE.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Book name is required.")
    private String name;

    @NotBlank(message = "Author name is required.")
    private String author;

    @NotBlank(message = "Price is required.")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Price must be a positive number, e.g. 19.99.")
    private String price;

    public Book(int id, String name, String author, String price) {
        super();
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
    }

    public Book() {
        super();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}