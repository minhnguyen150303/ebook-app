package com.example.ebook.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ebook.BuildConfig;
import com.example.ebook.R;
import com.example.ebook.model.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    public static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvContent, tvTime;

        public VH(@NonNull View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvName);
            tvContent = v.findViewById(R.id.tvContent);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }


    private final List<Chat> data = new ArrayList<>();
    private final Context ctx;

    // ✅ thêm để phân biệt gửi / nhận
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private String currentUserId; // set từ ChatActivity khi tạo adapter

    public ChatAdapter(Context ctx, String currentUserId) {
        this.ctx = ctx;
        this.currentUserId = currentUserId;
    }

    public void setItems(List<Chat> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(Chat m) {
        data.add(m);
        notifyItemInserted(data.size() - 1);
    }

    // ✅ thêm hàm xác định loại item
    @Override
    public int getItemViewType(int position) {
        Chat msg = data.get(position);

        String senderId = null;
        if (msg.getUser() != null) senderId = msg.getUser().getId(); // User.getId() đã gộp id/_id

        String cur = currentUserId == null ? "" : currentUserId.trim();
        String snd = senderId == null ? "" : senderId.trim();

        boolean isMine = !cur.isEmpty() && cur.equalsIgnoreCase(snd);

        Log.d("CHAT_VIEWTYPE", "pos=" + position + " sender=" + snd + " current=" + cur + " -> " + (isMine ? "SENT" : "RECEIVED"));

        return isMine ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }





    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if (viewType == VIEW_TYPE_SENT) {
            v = inflater.inflate(R.layout.item_message_sent, parent, false);
        } else {
            v = inflater.inflate(R.layout.item_message_received, parent, false);
        }
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Chat m = data.get(pos);

        // --- NAME ---
        String displayName = "Unknown";
        if (m.getUser() != null) {
            String n = m.getUser().getFullName();
            if (!TextUtils.isEmpty(n)) displayName = n;
        }
        h.tvName.setText(displayName);

        // --- CONTENT ---
        h.tvContent.setText(m.getContent() != null ? m.getContent() : "");

        // --- AVATAR ---
        String raw = (m.getUser() != null) ? m.getUser().getProfileImage() : null;
        String url = buildImageUrlLocal(raw);

        // --- TIME ---
        String when = formatTime(m.getCreatedAt()); // ISO từ BE, ví dụ: 2025-06-02T13:54:50.329+00:00
        h.tvTime.setText(when);

        Glide.with(ctx)
                .load(url)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .circleCrop()
                .into(h.imgAvatar);

        boolean isMine = currentUserId != null
                && m.getUser() != null
                && currentUserId.trim().equalsIgnoreCase(m.getUser().getId());

        // ép căn phải/trái theo isMine (nếu lỡ inflate sai)
        if (h.itemView instanceof android.widget.LinearLayout) {
            ((android.widget.LinearLayout) h.itemView)
                    .setGravity(isMine ? android.view.Gravity.END : android.view.Gravity.START);
        }
        if (isMine) {
            h.tvName.setTextColor(android.graphics.Color.parseColor("#0288D1")); // tên xanh biển
        }

        Log.d("CHAT", "name=" + displayName + " avatarRaw=" + raw + " url=" + url);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // Giữ nguyên hàm build URL ảnh
    private String buildImageUrlLocal(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;

        String base = BuildConfig.BASE_IMAGE_URL;
        if (base.endsWith("/") && path.startsWith("/")) {
            return base.substring(0, base.length() - 1) + path;
        } else if (!base.endsWith("/") && !path.startsWith("/")) {
            return base + "/" + path;
        } else {
            return base + path;
        }
    }

    public void setCurrentUserId(String id) {
        this.currentUserId = id;
    }

    private String formatTime(String iso) {
        if (TextUtils.isEmpty(iso)) return "";
        try {
            // ISO-8601 có timezone dạng "+07:00"
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.getDefault());
            java.util.Date d = in.parse(iso);

            // Nếu khác ngày thì hiện dd/MM HH:mm, cùng ngày chỉ HH:mm
            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(d);

            boolean sameDay = now.get(java.util.Calendar.YEAR) == c.get(java.util.Calendar.YEAR)
                    && now.get(java.util.Calendar.DAY_OF_YEAR) == c.get(java.util.Calendar.DAY_OF_YEAR);

            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat(sameDay ? "HH:mm" : "dd/MM HH:mm", java.util.Locale.getDefault());
            return out.format(d);
        } catch (Exception e) {
            // fallback nếu parse lỗi
            try {
                java.text.SimpleDateFormat in2 = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault());
                java.util.Date d2 = in2.parse(iso);
                java.text.SimpleDateFormat out2 = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                return out2.format(d2);
            } catch (Exception ignore) {
                return "";
            }
        }
    }



}
