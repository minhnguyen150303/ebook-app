// BookListByCategoryActivity.java
package com.example.ebook.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.adapter.BookAdapter;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.BookApi;
import com.example.ebook.model.Book;
import com.example.ebook.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookListByCategoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();
    private String categoryId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list_by_category);

        recyclerView = findViewById(R.id.recyclerBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BookAdapter(bookList, new BookAdapter.OnBookActionListener() {
            @Override
            public void onLock(Book book) {
                toggleBookStatus(book);
            }

            @Override
            public void onDelete(Book book) {
                confirmDeleteBook(book);
            }
        });

        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        categoryId = getIntent().getStringExtra("categoryId");
        if (categoryId != null) {
            loadBooksByCategory(categoryId);
        } else {
            Toast.makeText(this, "Không có ID thể loại", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBooksByCategory(String id) {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);

        bookApi.getBooksByCategory(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    bookList.clear();
                    List<Map<String, Object>> raw = (List<Map<String, Object>>) response.body().get("books");
                    for (Map<String, Object> item : raw) {
                        Book book = new Book();
                        book.setId((String) item.get("_id"));
                        book.setTitle((String) item.get("title"));
                        book.setAuthor((String) item.get("author"));
                        book.setActive((Boolean) item.get("is_active"));
                        bookList.add(book);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookListByCategoryActivity.this, "Lỗi tải sách", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(BookListByCategoryActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleBookStatus(Book book) {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.toggleBookStatus(book.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookListByCategoryActivity.this, "Đã đổi trạng thái sách", Toast.LENGTH_SHORT).show();
                    book.setActive(!book.isActive());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookListByCategoryActivity.this, "Không thể đổi trạng thái sách", Toast.LENGTH_SHORT).show();
                    Log.e("BOOK_STATUS_FAIL", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("BOOK_STATUS_FAIL", "Error: ", t);
                Toast.makeText(BookListByCategoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteBook(Book book) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sách: " + book.getTitle() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBook(book))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBook(Book book) {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.deleteBook(book.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookListByCategoryActivity.this, "Đã xóa sách", Toast.LENGTH_SHORT).show();
                    bookList.remove(book);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookListByCategoryActivity.this, "Không thể xóa sách", Toast.LENGTH_SHORT).show();
                    Log.e("DELETE_BOOK_FAIL", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("DELETE_BOOK_FAIL", "Error: ", t);
                Toast.makeText(BookListByCategoryActivity.this, "Lỗi kết nối khi xóa sách", Toast.LENGTH_SHORT).show();
            }
        });
    }
}