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

    // ====== Thêm 2 field dành cho API chat (KHÔNG dùng alternate để tránh trùng key) ======
    @SerializedName("fullName")
    private String fullName;          // BE chat trả "fullName"

    @SerializedName("profileImage")
    private String profileImage;      // BE chat trả "profileImage"
    // ===============================================================================

    // Các field cũ bạn đang dùng ở API khác
    private String name;              // API khác có thể trả "name"
    private String email;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String avatar;            // API khác có thể trả "avatar"
    private boolean is_active;
    private String role;

    // ====== Getters ======
    // Ưu tiên fullName nếu có, nếu không thì dùng name
    public String getFullName() {
        return (fullName != null && !fullName.isEmpty()) ? fullName : name;
    }

    public String getName() {
        return (fullName != null && !fullName.isEmpty()) ? fullName : name;
    }

    // Ưu tiên profileImage nếu có, nếu không thì dùng avatar
    public String getProfileImage() {
        return (profileImage != null && !profileImage.isEmpty()) ? profileImage : avatar;
    }

    public String getAvatar() {
        return (profileImage != null && !profileImage.isEmpty()) ? profileImage : avatar;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getRole() { return role; }
    public boolean isActive() { return is_active; }

    public void setActive(boolean active) { this.is_active = active; }
}
