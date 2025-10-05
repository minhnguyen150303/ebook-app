package com.example.ebook.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.adapter.ChatAdapter;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.ChatApi;
import com.example.ebook.model.Chat;
import com.example.ebook.model.GetMessagesResponse;
import com.example.ebook.model.SendMessageResponse;
import com.example.ebook.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rv;
    private EditText edtMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private ChatApi chatApi;

    private String currentUserId;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ===== Toolbar back =====
        MaterialToolbar toolbar = findViewById(R.id.topAppBarChat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // BẬT nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // (tuỳ chọn) đảm bảo dùng đúng icon
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        // Handle click
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        rv = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        // === Lấy currentUserId đúng nguồn (SessionManager) + fallback ===
        session = new SessionManager(this);
        currentUserId = session.getUserId();
        if (currentUserId == null) {
            SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            currentUserId = sp.getString("user_id", null);
        }
        if (currentUserId == null) {
            currentUserId = getIntent().getStringExtra("user_id");
        }
        Log.d("CHAT_ID", "currentUserId=" + currentUserId);

        // === RecyclerView: cuộn từ cuối (giống app chat) ===
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);

        // === Adapter: truyền currentUserId để phân loại sent/received ===
        adapter = new ChatAdapter(this, currentUserId);
        rv.setAdapter(adapter);

        // === API (dùng bản có context để chèn token nếu có) ===
        chatApi = ApiClient.getClient(this).create(ChatApi.class);

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (content.isEmpty()) return;
            sendMessage(content);
        });
    }

    private void loadMessages() {
        chatApi.getMessages().enqueue(new Callback<GetMessagesResponse>() {
            @Override public void onResponse(Call<GetMessagesResponse> call, Response<GetMessagesResponse> res) {
                if (res.isSuccessful() && res.body() != null && res.body().isSuccess()) {
                    List<Chat> list = res.body().getMessages();
                    adapter.setItems(list);
                    rv.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
                } else {
                    Toast.makeText(ChatActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<GetMessagesResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String content) {
        chatApi.sendMessage(new ChatApi.SendBody(content)).enqueue(new Callback<SendMessageResponse>() {
            @Override public void onResponse(Call<SendMessageResponse> call, Response<SendMessageResponse> res) {
                if (res.isSuccessful() && res.body() != null && res.body().isSuccess()) {
                    Chat newMsg = res.body().getMessage();

                    // Fallback học ID nếu lúc mở màn chưa có
                    if (currentUserId == null && newMsg.getUser() != null && newMsg.getUser().getId() != null) {
                        currentUserId = newMsg.getUser().getId();
                        session.saveUserId(currentUserId);
                        adapter.setCurrentUserId(currentUserId); // ensure adapter có setter này
                        adapter.notifyDataSetChanged();
                        Log.d("CHAT_ID", "Learned currentUserId from message: " + currentUserId);
                    }

                    adapter.addItem(newMsg);
                    rv.scrollToPosition(adapter.getItemCount() - 1);
                    edtMessage.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, "Gửi thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<SendMessageResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
