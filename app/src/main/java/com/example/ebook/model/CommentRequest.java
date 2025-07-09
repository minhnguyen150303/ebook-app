package com.example.ebook.model;

public class CommentRequest {
    private String book;
    private String comment;
    private float rating;

    // Constructor đầy đủ để truyền vào tất cả các tham số
    public CommentRequest(String book, String comment, float rating) {
        this.book = book;
        this.comment = comment;
        this.rating = rating;
    }

    // Getter và setter cho rating
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    // Getter cho book và comment
    public String getBook() {
        return book;
    }

    public String getComment() {
        return comment;
    }
}
