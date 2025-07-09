package com.example.ebook.model;

public class Comment {
    private String _id;
    private String comment;
    private String book;
    private User user;
    private float rating;

    private boolean is_hidden;
    private String created_at;

    public String get_id() { return _id; }
    public String getComment() { return comment; }
    public String getBook() { return book; }
    public User getUser() { return user; }
    public boolean isHidden() { return is_hidden; }
    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public String getCreated_at() { return created_at; }
}
