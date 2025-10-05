package com.example.ebook.api;

import android.content.Context;

import com.example.ebook.BuildConfig;
import com.example.ebook.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://10.0.2.2:5000/";

    public static Retrofit getClient(Context context) {
        // dùng cho các API cần token
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                }).build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getClient() {
        // dùng cho API không cần token (login/register)
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Trả về base URL cho ảnh (ví dụ bạn có thể dùng chung BASE_URL luôn)
    public static String getBaseUrlForImages() {
        return BuildConfig.BASE_IMAGE_URL;
        // hoặc nếu chưa có BASE_IMAGE_URL thì return BuildConfig.BASE_URL;
    }

    // Ghép link ảnh đầy đủ từ BE
    public static String buildImageUrl(String maybeRelative) {
        if (maybeRelative == null || maybeRelative.isEmpty()) return null;
        if (maybeRelative.startsWith("http")) return maybeRelative;
        return getBaseUrlForImages() + maybeRelative;
    }

}

