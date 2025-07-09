package com.example.ebook.model;

public class SingleCommentResponse {
    private boolean success;
    private String message;
    private Comment comment;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Comment getComment() { return comment; }
}

