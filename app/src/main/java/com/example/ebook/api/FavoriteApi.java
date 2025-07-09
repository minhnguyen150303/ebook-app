package com.example.ebook.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteApi {

    // Toggle yêu thích (thêm hoặc bỏ)
    @POST("favorite/toggle/{bookId}")
    Call<Map<String, Object>> toggleFavorite(@Path("bookId") String bookId);

    // Lấy toàn bộ danh sách sách đã yêu thích của user hiện tại
    @GET("favorite")
    Call<Map<String, Object>> getFavorites();

}
