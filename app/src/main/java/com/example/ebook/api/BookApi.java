package com.example.ebook.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookApi {
    @GET("category/get-books/{id}")
    Call<Map<String, Object>> getBooksByCategory(@Path("id") String id);

    @PATCH("book/status/{id}")
    Call<Map<String, Object>> toggleBookStatus(@Path("id") String id);

    @DELETE("book/delete/{id}")
    Call<Map<String, Object>> deleteBook(@Path("id") String id);

    @GET("book/top-view")
    Call<Map<String, Object>> getTopViewedBooks(@Query("limit") int limit);

    @GET("book/get-detail/{id}")
    Call<Map<String, Object>> getBookById(@Path("id") String id);

    @GET("book/menu/{id}")
    Call<Map<String, Object>> getChaptersByBook(@Path("id") String bookId);

    @GET("book/category/{categoryId}")
    Call<Map<String, Object>> getAllBooksByCategory(@Path("categoryId") String categoryId);

    @GET("book/chapter/{bookId}/{chapterNumber}")
    Call<Map<String, Object>> getChapterByNumber(@Path("bookId") String bookId, @Path("chapterNumber") int chapterNumber);

    @GET("bookmark")
    Call<Map<String, Object>> getAllBookmarks();

    @GET("book/get-all")
    Call<Map<String, Object>> getAllBooks();

    @GET("book/no-view/{id}")
    Call<Map<String, Object>> getBookByIdNoView(@Path("id") String id);
}

