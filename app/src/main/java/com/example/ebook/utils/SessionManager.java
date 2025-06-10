package com.example.ebook.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString("ACCESS_TOKEN", token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString("ACCESS_TOKEN", null);
    }


    public void saveUsername(String username) {
        editor.putString("USERNAME", username);
        editor.apply();
    }

    public void saveUserId(String userId) {
        editor.putString("USER_ID", userId);
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString("USER_ID", null);
    }

    public void saveUserInfo(String name, String email, String phone, String gender, String dob, String avatar) {
        editor.putString("USERNAME", name);
        editor.putString("EMAIL", email);
        editor.putString("PHONE", phone);
        editor.putString("GENDER", gender);
        editor.putString("DOB", dob);
        editor.putString("AVATAR", avatar);
        editor.apply();
    }
    public String getUsername() {
        return prefs.getString("USERNAME", "");
    }
    public String getEmail() {
        return prefs.getString("EMAIL", "");
    }
    public String getPhone() {
        return prefs.getString("PHONE", "");
    }
    public String getGender() {
        return prefs.getString("GENDER", "");
    }
    public String getDob() {
        return prefs.getString("DOB", "");
    }
    public String getAvatar() {
        return prefs.getString("AVATAR", "");
    }

    public void saveRole(String role) {
        editor.putString("ROLE", role);
        editor.apply();
    }

    public void saveEmail(String email) {
        editor.putString("EMAIL", email);
        editor.apply();
    }

    public String getRole() {
        return prefs.getString("ROLE", "");
    }


}
