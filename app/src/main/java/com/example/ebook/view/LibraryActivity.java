package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.BookApi;
import com.example.ebook.api.BookmarkApi;
import com.example.ebook.model.Book;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryActivity extends AppCompatActivity {

    private LinearLayout layoutLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBarLibrary);
        topAppBar.setTitle("Sách đang đọc");
        topAppBar.setTitleTextColor(getResources().getColor(android.R.color.white));
        topAppBar.setNavigationOnClickListener(v -> finish());


        layoutLibrary = findViewById(R.id.layoutLibrary);

        loadBookmarkedBooks();
    }

    private void loadBookmarkedBooks() {
        BookApi api = ApiClient.getClient(this).create(BookApi.class);
        api.getAllBookmarks().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> bookmarks = (List<Map<String, Object>>) response.body().get("bookmarks");

                    for (Map<String, Object> item : bookmarks) {
                        Map<String, Object> bookData = (Map<String, Object>) item.get("book");
                        Book book = new Book();
                        book.setId((String) bookData.get("_id"));
                        book.setTitle((String) bookData.get("title"));
                        book.setAuthor((String) bookData.get("author"));
                        book.setCoverUrl((String) bookData.get("cover_url"));

                        addBookView(book);
                    }

                } else {
                    Toast.makeText(LibraryActivity.this, "Không có sách đang đọc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LibraryActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookView(Book book) {
        // Inflate the item view from XML
        ViewGroup item = (ViewGroup) getLayoutInflater().inflate(R.layout.item_library_book, layoutLibrary, false);

        // Get references to the views inside the inflated layout
        TextView tvTitle = item.findViewById(R.id.tvTitle);
        TextView tvAuthor = item.findViewById(R.id.tvAuthor);
        ImageView imgCover = item.findViewById(R.id.imgCover);
        Button btnRemoveBookmark = item.findViewById(R.id.btnRemoveBookmark); // Reference to the "Remove Bookmark" button

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());

        Glide.with(this)
                .load(book.getCoverUrl().replace("localhost", "10.0.2.2"))
                .into(imgCover);

        // Set click listener for book item
        item.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            startActivity(intent);
        });

        // Handle "Remove Bookmark" button click
        btnRemoveBookmark.setOnClickListener(v -> removeBookmark(book.getId()));

        // Add the item view to the layout
        layoutLibrary.addView(item);
    }


    private void removeBookmark(String bookId) {
        BookmarkApi api = ApiClient.getClient(this).create(BookmarkApi.class);
        api.deleteBookmark(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().get("success").equals(true)) {
                        Toast.makeText(LibraryActivity.this, "Đã bỏ đánh dấu sách", Toast.LENGTH_SHORT).show();
                        layoutLibrary.removeAllViews();
                        loadBookmarkedBooks();
                    } else {
                        Toast.makeText(LibraryActivity.this, "Không thể bỏ đánh dấu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý khi API trả về lỗi không phải 2xx
                    Toast.makeText(LibraryActivity.this, "Không thể bỏ đánh dấu, thử lại sau", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                // Xử lý lỗi kết nối mạng
                Toast.makeText(LibraryActivity.this, "Lỗi mạng, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
