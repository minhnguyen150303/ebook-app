package com.example.ebook.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CategoryApi {

    @GET("category")
    Call<Map<String, Object>> getCategories();

    @POST("category")
    Call<Map<String, Object>> addCategory(@Body Map<String, String> body);

    @DELETE("category/{id}")
    Call<Map<String, String>> deleteCategory(@Path("id") String id);

    @GET("category")
    Call<Map<String, Object>> getAllCategories();

}
