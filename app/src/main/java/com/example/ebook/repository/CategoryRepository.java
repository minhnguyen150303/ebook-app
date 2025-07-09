package com.example.ebook.repository;

import android.content.Context;

import com.example.ebook.api.ApiClient;
import com.example.ebook.api.CategoryApi;

import java.util.Map;

import retrofit2.Call;

public class CategoryRepository {
    private final CategoryApi api;

    public CategoryRepository(Context context) {
        api = ApiClient.getClient(context).create(CategoryApi.class);
    }

    public Call<Map<String, Object>> getAllCategories() {
        return api.getCategories();
    }

    public Call<Map<String, Object>> addCategory(String name) {
        return api.addCategory(Map.of("name", name));
    }

    public Call<Map<String, String>> deleteCategory(String id) {
        return api.deleteCategory(id);
    }
}
