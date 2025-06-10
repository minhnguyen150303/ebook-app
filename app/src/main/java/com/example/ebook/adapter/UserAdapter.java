package com.example.ebook.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.model.User;
import com.example.ebook.repository.UserRepository;
import com.example.ebook.utils.SessionManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final UserRepository repository = new UserRepository();

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.userList = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());

        // Gán trạng thái nút
        holder.btnToggleStatus.setText(user.isActive() ? "Khóa" : "Mở");

        holder.btnDelete.setOnClickListener(v -> {
            Toast.makeText(context, "Xoá: " + user.getName(), Toast.LENGTH_SHORT).show();
            // TODO: gọi API xoá user
        });

        holder.btnToggleStatus.setOnClickListener(v -> {
            String token = new SessionManager(context).getToken();
            String userId = user.getId();

            if (userId == null) {
                Log.e("TOGGLE_FAIL", "User ID is null for: " + user.getEmail());
                Toast.makeText(context, "Không có ID người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.toggleUserStatus(userId, token).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Đã đổi trạng thái tài khoản", Toast.LENGTH_SHORT).show();
                        user.setActive(!user.isActive());
                        notifyItemChanged(holder.getAdapterPosition());
                    } else {
                        Toast.makeText(context, "Không thể thay đổi trạng thái", Toast.LENGTH_SHORT).show();
                        Log.e("TOGGLE_FAIL", "Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("TOGGLE_FAIL", "Error: ", t);
                    Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserEmail;
        Button  btnDelete, btnToggleStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnToggleStatus = itemView.findViewById(R.id.btnToggleStatus);
        }
    }
}
