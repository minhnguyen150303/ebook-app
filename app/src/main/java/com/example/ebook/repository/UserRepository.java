package com.example.ebook.repository;

import com.example.ebook.api.ApiClient;
import com.example.ebook.api.ApiService;
import com.example.ebook.model.User;
import com.example.ebook.model.UserResponse;
import com.example.ebook.model.UserResponseList;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class UserRepository {
    private final ApiService api;

    public UserRepository() {
        api = ApiClient.getClient().create(ApiService.class);
    }

    public Call<UserResponse> getUserById(String id) {
        return api.getUserById(id);
    }

    public Call<UserResponseList> getAllUsers(String token) {
        return api.getAllUsers("Bearer " + token);
    }

    public Call<ResponseBody> toggleUserStatus(String userId, String token) {
        return api.toggleUserStatus(userId, "Bearer " + token);
    }


}
