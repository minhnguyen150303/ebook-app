package com.example.ebook.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ebook.R;
import com.example.ebook.model.Comment;
import com.example.ebook.utils.SessionManager;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private Context context;
    private SessionManager sessionManager;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onEdit(Comment comment);
        void onDelete(Comment comment);
        void onToggle(Comment comment);
        void onToggleUser(String userId);
    }

    public CommentAdapter(Context context, List<Comment> comments, OnCommentActionListener listener) {
        this.context = context;
        this.comments = comments;
        this.listener = listener;
        this.sessionManager = new SessionManager(context);
    }

    public void setData(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String name = comment.getUser().getName();
        if (!comment.getUser().isActive()) {
            name += " (Đã bị khóa)";
            holder.tvName.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvName.setTextColor(context.getResources().getColor(android.R.color.black));
        }
        holder.tvName.setText(name);

        holder.tvContent.setText(comment.getComment());

        holder.ratingBar.setRating(comment.getRating());


        String role = sessionManager.getRole();

        // Nếu là admin và bình luận bị ẩn thì hiển thị mờ
        if ("admin".equals(role) && comment.isHidden()) {
            holder.tvContent.setAlpha(0.5f);
            holder.tvName.setAlpha(0.5f);
            holder.tvTime.setAlpha(0.5f);

            holder.tvContent.setText("(Bình luận đã bị ẩn) " + comment.getComment());
            holder.tvContent.setTextColor(android.graphics.Color.parseColor("#0077CC")); // 🔵 xanh nước biển
        } else {
            holder.tvContent.setAlpha(1f);
            holder.tvName.setAlpha(1f);
            holder.tvTime.setAlpha(1f);
            holder.tvContent.setText(comment.getComment());
            holder.tvContent.setTextColor(context.getResources().getColor(android.R.color.black)); // ⚫ mặc định
        }

        // Format thời gian
        String formattedTime = formatTime(comment.getCreated_at());
        holder.tvTime.setText(formattedTime);

        // Popup menu
        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            String userId = sessionManager.getUserId();

            if ("admin".equals(role)) {
                popup.getMenu().add(comment.isHidden() ? "Hiện bình luận" : "Ẩn bình luận");

                if (comment.getUser() != null && !"admin".equalsIgnoreCase(comment.getUser().getRole())) {
                    popup.getMenu().add(comment.getUser().isActive() ? "Khóa người dùng" : "Mở khóa người dùng");
                }
            }

            // Chỉ khi ID user hiện tại = ID người viết comment
            if (comment.getUser() != null && userId != null && userId.equals(comment.getUser().getId())) {
                popup.getMenu().add("Sửa bình luận");
                popup.getMenu().add("Xóa bình luận");
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Sửa")) {
                    listener.onEdit(comment);
                } else if (title.contains("Xóa")) {
                    listener.onDelete(comment);
                } else if (title.contains("Khóa") || title.contains("Mở")) {
                    if (comment.getUser() != null) {
                        listener.onToggleUser(comment.getUser().getId()); // Gọi về Activity
                    }
                } else {
                    listener.onToggle(comment); // Ẩn/hiện bình luận
                }
                return true;
            });

            popup.show();
        });
    }


    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvContent, tvTime;
        ImageView btnMore;
        RatingBar ratingBar;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCommentUser);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            btnMore = itemView.findViewById(R.id.btnMore);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }

    private String formatTime(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = isoFormat.parse(isoTime);
            if (date != null) {
                SimpleDateFormat desiredFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                desiredFormat.setTimeZone(TimeZone.getDefault());
                return desiredFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isoTime; // fallback nếu lỗi
    }

    public void updateComment(Comment updatedComment) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).get_id().equals(updatedComment.get_id())) {
                comments.set(i, updatedComment);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeComment(String commentId) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).get_id().equals(commentId)) {
                comments.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

}
