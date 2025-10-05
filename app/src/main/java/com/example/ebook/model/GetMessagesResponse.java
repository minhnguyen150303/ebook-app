package com.example.ebook.model;

import java.util.List;

public class GetMessagesResponse {
    private boolean success;
    private List<Chat> messages;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<Chat> getMessages() { return messages; }
    public void setMessages(List<Chat> messages) { this.messages = messages; }
}

