package com.example.ebook.view;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.adapter.UserAdapter;
import com.example.ebook.model.User;
import com.example.ebook.model.UserResponseList;
import com.example.ebook.repository.UserRepository;
import com.example.ebook.utils.SessionManager;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private String currentUserId;
    private UserRepository repository;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        repository = new UserRepository();

        String token = new SessionManager(this).getToken();
        currentUserId = extractUserIdFromToken(token);

        try {
            // Kết nối socket (10.0.2.2 nếu là máy ảo)
            mSocket = IO.socket("http://10.0.2.2:5000");
            mSocket.connect();
            mSocket.on("user-status-changed", onUserStatusChanged);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadUsers(token);
    }

    private final Emitter.Listener onUserStatusChanged = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(() -> {
                try {
                    JSONObject data = (JSONObject) args[0];
                    String userId = data.getString("userId");
                    boolean isActive = data.getBoolean("is_active");

                    if (userList != null) {
                        for (User user : userList) {
                            if (user.getId().equals(userId)) {
                                user.setActive(isActive);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    };

    private void loadUsers(String token) {
        repository.getAllUsers(token).enqueue(new Callback<UserResponseList>() {
            @Override
            public void onResponse(Call<UserResponseList> call, Response<UserResponseList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body().getData();

                    String currentEmail = new SessionManager(UserManagementActivity.this).getEmail();
                    userList.removeIf(user -> user.getEmail() != null && user.getEmail().equalsIgnoreCase(currentEmail));

                    adapter = new UserAdapter(UserManagementActivity.this, userList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(UserManagementActivity.this, "Không thể tải danh sách người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponseList> call, Throwable t) {
                Toast.makeText(UserManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(Base64.decode(parts[1], Base64.DEFAULT));
                JSONObject json = new JSONObject(payload);
                return json.getString("id");
            }
        } catch (Exception e) {
            Log.e("TOKEN_PARSE_ERROR", "Lỗi lấy userId từ token", e);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("user-status-changed", onUserStatusChanged);
        }
    }
}
