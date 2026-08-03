package com.abrar.BOOKSTORE.controller.dto;

import java.util.List;

public class BookImportRequest {
    private String name;
    private String author;
    private String price;
    private List<TakeawayImportRequest> takeaways;

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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<TakeawayImportRequest> getTakeaways() {
        return takeaways;
    }

    public void setTakeaways(List<TakeawayImportRequest> takeaways) {
        this.takeaways = takeaways;
    }
}