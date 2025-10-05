package com.example.ebook.model;

public class SendMessageResponse {
    private boolean success;
    private Chat message;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Chat getMessage() { return message; }
    public void setMessage(Chat message) { this.message = message; }
}

