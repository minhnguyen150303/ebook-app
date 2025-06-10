package com.example.ebook.api;

import com.example.ebook.model.User;
import com.example.ebook.model.UserResponse;
import com.example.ebook.model.UserResponseList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/auth/get-detail-user/{id}")
    Call<UserResponse> getUserById(@Path("id") String id);

    @GET("/auth/get-all")
    Call<UserResponseList> getAllUsers(@Header("Authorization") String token);

    @PATCH("/auth/status-user/{id}")
    Call<ResponseBody> toggleUserStatus(@Path("id") String id, @Header("Authorization") String token);


    @Multipart
    @PATCH("auth/update-profile")
    Call<ResponseBody> updateProfile(
            @Header("Authorization") String token,
            @Part("name") RequestBody name,
            @Part("phone") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("gender") RequestBody gender,
            @Part("dob") RequestBody dob,
            @Part("role") RequestBody role
    );


}
