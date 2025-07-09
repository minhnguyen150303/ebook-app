package com.example.ebook.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("_id")
    private String _id;

    public String getId() {
        return id != null ? id : _id;
    }
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String avatar;
    private boolean is_active;
    private String role;

    // Getters (hoặc thêm setters nếu cần)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() {
        return phone;
    }
    public String getGender() {
        return gender;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }
    public boolean isActive() {
        return is_active;
    }

    public void setActive(boolean active) {
        this.is_active = active;
    }
}
