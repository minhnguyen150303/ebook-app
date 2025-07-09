package com.example.ebook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    // ✅ Định nghĩa interface đúng tên ở đây
    public interface OnCategoryClickListener {
        void onDelete(Category category);
        void onDetail(Category category);
    }

    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnDelete, btnDetail;

        public ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            btnDelete = view.findViewById(R.id.btnDelete);
            btnDetail = view.findViewById(R.id.btnDetail);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvName.setText(category.getName());
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(category));
        holder.btnDetail.setOnClickListener(v -> listener.onDetail(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
