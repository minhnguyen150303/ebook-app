package com.example.ebook.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface BookmarkApi {

    @GET("bookmark/{bookId}")
    Call<Map<String, Object>> getBookmarkByBook(@Path("bookId") String bookId);

    @PATCH("bookmark/{bookId}")
    Call<Map<String, Object>> saveOrUpdateBookmark(
            @Path("bookId") String bookId,
            @Body Map<String, Object> body
    );

    @DELETE("bookmark/{bookId}")
    Call<Map<String, Object>> deleteBookmark(@Path("bookId") String bookId);
}
