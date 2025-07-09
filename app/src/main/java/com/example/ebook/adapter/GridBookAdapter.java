package com.example.ebook.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.model.Book;
import com.example.ebook.view.BookDetailActivity;

import java.util.List;

public class GridBookAdapter extends RecyclerView.Adapter<GridBookAdapter.ViewHolder> {

    public interface OnBookClickListener {
        void onClick(Book book);
    }

    private final Context context;
    private final List<Book> books;
    private final OnBookClickListener listener;

    public GridBookAdapter(Context context, List<Book> books, OnBookClickListener listener) {
        this.context = context;
        this.books = books;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_book_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Book book = books.get(position);
        holder.tvTitle.setText(book.getTitle());

        String coverUrl = book.getCoverUrl();
        if (coverUrl != null && coverUrl.contains("localhost")) {
            coverUrl = coverUrl.replace("localhost", "10.0.2.2");
        }

        Glide.with(context)
                .load(coverUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.imgCover);

        holder.itemView.setOnClickListener(v -> listener.onClick(book));
    }

    public void setBooks(List<Book> newBooks) {
        books.clear();
        books.addAll(newBooks);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
