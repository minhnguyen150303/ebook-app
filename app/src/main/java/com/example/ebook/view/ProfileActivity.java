package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.model.User;
import com.example.ebook.model.UserResponse;
import com.example.ebook.repository.UserRepository;
import com.example.ebook.utils.SessionManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvPhone, tvGender, tvDob;
    private ImageView avatarImage;
    private Button btnBack, btnEdit;
    private UserRepository repository;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.etPhone);
        tvGender = findViewById(R.id.etGender);
        tvDob = findViewById(R.id.etDob);
        avatarImage = findViewById(R.id.avatarImage);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        repository = new UserRepository();

        String token = new SessionManager(this).getToken();
        userId = extractUserIdFromToken(token);
        if (userId != null) {
            loadUserInfo(userId);
        } else {
            tvName.setText("Không tìm thấy ID từ token");
        }

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            loadUserInfo(userId);
        }
    }

    private void loadUserInfo(String userId) {
        repository.getUserById(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getData();

                    tvName.setText(user.getName());
                    tvEmail.setText(user.getEmail());
//                    tvPhone.setText(user.getPhone());
//                    tvGender.setText(formatGender(user.getGender()));
//                    tvDob.setText(formatDate(user.getDateOfBirth()));

                    tvPhone.setText(user.getPhone() != null && !user.getPhone().isEmpty()
                            ? user.getPhone() : "Chưa cập nhật!");

                    tvGender.setText(user.getGender() != null && !user.getGender().isEmpty()
                            ? formatGender(user.getGender()) : "Chưa cập nhật!");

                    tvDob.setText(user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()
                            ? formatDate(user.getDateOfBirth()) : "Chưa cập nhật!");

                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load("http://10.0.2.2:5000" + user.getAvatar())
                                .circleCrop()
                                .placeholder(R.drawable.ic_avatar_placeholder)
                                .into(avatarImage);
                    }
                } else {
                    Log.e("LOAD_USER_ERROR", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("API_FAIL", "Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private String formatDate(String rawDate) {
        try {
            java.text.SimpleDateFormat input = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            input.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.text.SimpleDateFormat output = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return output.format(input.parse(rawDate));
        } catch (Exception e) {
            return rawDate;
        }
    }

    private String formatGender(String gender) {
        if ("nam".equalsIgnoreCase(gender)) return "Nam";
        if ("nu".equalsIgnoreCase(gender)) return "Nữ";
        return gender;
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
            e.printStackTrace();
        }
        return null;
    }
}
