package com.example.ebook.model;

public class BookResponse {
    private boolean success;
    private Book book;

    public boolean isSuccess() {
        return success;
    }

    public Book getBook() {
        return book;
    }
}
