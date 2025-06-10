package com.example.ebook.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.R;
import com.example.ebook.api.AuthApi;
import com.example.ebook.model.ChangePasswordRequest;
import com.example.ebook.api.ApiClient;
import com.example.ebook.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etOldPassword, etNewPassword, etNewPassword2;
    private Button btnChange;
    private AuthApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // layout phải tạo ở bước 1

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etNewPassword2 = findViewById(R.id.etNewPassword2);
        btnChange = findViewById(R.id.btnChangePassword);

        api = ApiClient.getClient().create(AuthApi.class);

        btnChange.setOnClickListener(v -> handleChangePassword());

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

    }

    private void handleChangePassword() {
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etNewPassword2.getText().toString().trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = new SessionManager(this).getToken();
        ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass);

        api.changePassword(request, "Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    finish(); // quay lại màn trước
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
