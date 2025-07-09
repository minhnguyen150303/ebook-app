package com.example.ebook.api;

import com.example.ebook.model.CommentRequest;
import com.example.ebook.model.CommentResponse;
import com.example.ebook.model.SingleCommentResponse;
import com.example.ebook.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CommentApi {
    @GET("comment/book/{bookId}")
    Call<CommentResponse> getCommentsByBook(@Path("bookId") String bookId);

    @POST("comment")
    Call<SingleCommentResponse> createComment(@Body CommentRequest request);

    @PATCH("comment/update/{id}")
    Call<SingleCommentResponse> updateComment(@Path("id") String id, @Body CommentRequest request);

    @DELETE("comment/delete/{id}")
    Call<ApiResponse> deleteComment(@Path("id") String id);

    @PATCH("comment/toggle/{id}")
    Call<ApiResponse> toggleComment(@Path("id") String id);

    @GET("comment/all-book/{bookId}")
    Call<CommentResponse> getAllCommentsByBook(@Path("bookId") String bookId);
}
