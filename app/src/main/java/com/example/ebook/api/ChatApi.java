package com.example.ebook.api;

import com.example.ebook.model.GetMessagesResponse;
import com.example.ebook.model.SendMessageResponse;
import com.example.ebook.view.ChatActivity;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/** Retrofit endpoints cho chat (khớp BE: /chat) */
public interface ChatApi {
    @GET("chat")
    Call<GetMessagesResponse> getMessages();

    @POST("chat")
    Call<SendMessageResponse> sendMessage(@Body SendBody body);

    // Bên trong interface ChatApi, thêm class nhỏ cho body:
    class SendBody {
        public String content;
        public SendBody(String c) { this.content = c; }
    }

}
