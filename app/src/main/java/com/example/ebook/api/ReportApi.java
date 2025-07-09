package com.example.ebook.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ReportApi {
    @GET("/report/overview")
    Call<Map<String, Object>> getOverview(@Header("Authorization") String token);

    @GET("/report/books-by-category")
    Call<Map<String, Object>> getBooksByCategory(@Header("Authorization") String token);

    @GET("/report/comments-wmy")
    Call<Map<String, Object>> getCommentStats(
            @Header("Authorization") String token,
            @Query("range") String range
    );

    @GET("/report/new-users-wmy")
    Call<Map<String, Object>> getNewUsersStats(
            @Header("Authorization") String token,
            @Query("range") String range
    );


}
