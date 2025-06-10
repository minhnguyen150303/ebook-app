package com.example.ebook.api;


import com.example.ebook.model.ChangePasswordRequest;
import com.example.ebook.model.LoginRequest;
import com.example.ebook.model.RegisterRequest;
import com.example.ebook.model.LoginResponse;
import com.example.ebook.model.User;
import com.example.ebook.model.UserResponse;
import com.example.ebook.model.UserResponseList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @PATCH("auth/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request, @Header("Authorization") String token);

    @GET("auth/get-detail-user/{id}")
    Call<UserResponse> getUserById(@Path("id") String userId);

    @Multipart
    @PATCH("auth/update-profile")
    Call<ResponseBody> updateProfile(
            @Header("Authorization") String token,
            @Part MultipartBody.Part avatar,
            @Part("name") RequestBody name,
            @Part("phone") RequestBody phone,
            @Part("gender") RequestBody gender,
            @Part("dateOfBirth") RequestBody dob
    );
}
