package com.example.ebook.model;

public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private User user;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public User getUser() {
        return user;
    }
}
