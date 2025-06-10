package com.example.ebook.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.AuthApi;
import com.example.ebook.model.User;
import com.example.ebook.model.UserResponse;
import com.example.ebook.utils.PathUtils;
import com.example.ebook.utils.SessionManager;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private EditText etName, etPhone, etDob;
    private TextView tvName;

    private RadioGroup rgGender;
    private Button btnSave;

    private Uri selectedImageUri;
    private SessionManager session;
    private AuthApi api;
    private String userId;

    private final int PICK_IMAGE_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        session = new SessionManager(this);
        api = ApiClient.getClient().create(AuthApi.class);

        String token = session.getToken();
        userId = extractUserIdFromToken(token);
        Log.d("USER_ID", "userId = " + userId);

        imgAvatar = findViewById(R.id.imgAvatar);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        rgGender = findViewById(R.id.rgGender);
        btnSave = findViewById(R.id.btnSave);

        loadUserInfo();

        etDob.setOnClickListener(v -> showDatePicker());
        imgAvatar.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> submitForm());

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }


    private void loadUserInfo() {
        api.getUserById(userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getData(); // 👈 chính xác

                    Log.d("DEBUG_USER", "Name: " + user.getName());

                    etName.setText(user.getName() != null ? user.getName() : "");
                    etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
                    etDob.setText(user.getDateOfBirth() != null ? formatDate(user.getDateOfBirth()) : "");

                    if ("nam".equalsIgnoreCase(user.getGender())) {
                        rgGender.check(R.id.rbMale);
                    } else if ("nu".equalsIgnoreCase(user.getGender())) {
                        rgGender.check(R.id.rbFemale);
                    }

                    if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                        Glide.with(EditProfileActivity.this)
                                .load("http://10.0.2.2:5000" + user.getAvatar())
                                .circleCrop()
                                .into(imgAvatar);
                    }
                } else {
                    Log.e("API_FAIL", "Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String formatDate(String rawDate) {
        try {
            // input: 2003-03-15T00:00:00.000Z
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(inputFormat.parse(rawDate));
        } catch (Exception e) {
            e.printStackTrace();
            return rawDate; // fallback nếu lỗi
        }
    }




    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dob = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etDob.setText(dob);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                imgAvatar.setImageBitmap(BitmapFactory.decodeStream(inputStream));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void submitForm() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = rgGender.getCheckedRadioButtonId() == R.id.rbMale ? "nam" : "nu";

        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody phonePart = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody genderPart = RequestBody.create(MediaType.parse("text/plain"), gender);
        RequestBody dobPart = RequestBody.create(MediaType.parse("text/plain"), dob);

        MultipartBody.Part avatarPart = null;
        if (selectedImageUri != null) {
            File file = createTempFileFromUri(this, selectedImageUri);
            if (file != null) {
                RequestBody filePart = RequestBody.create(MediaType.parse("image/*"), file);
                avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), filePart);
            }
        }


        api.updateProfile(
                "Bearer " + session.getToken(),
                avatarPart,
                namePart,
                phonePart,
                genderPart,
                dobPart
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new SessionManager(EditProfileActivity.this).saveUsername(name);
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("UPDATE_ERROR", errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createTempFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            String fileName = "temp_avatar.jpg";
            File tempFile = new File(context.getCacheDir(), fileName);
            java.io.OutputStream outputStream = new java.io.FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
                org.json.JSONObject json = new org.json.JSONObject(payload);
                return json.getString("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
