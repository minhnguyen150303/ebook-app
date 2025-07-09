package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.FavoriteApi;
import com.example.ebook.model.Book;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {

    private LinearLayout layoutFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBarFavorite);
        topAppBar.setTitle("Sách yêu thích");
        topAppBar.setTitleTextColor(getResources().getColor(android.R.color.white));
        topAppBar.setNavigationOnClickListener(v -> finish());

        layoutFavorite = findViewById(R.id.layoutFavorite);

        loadFavoriteBooks();
    }

    private void loadFavoriteBooks() {
        FavoriteApi api = ApiClient.getClient(this).create(FavoriteApi.class);
        api.getFavorites().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> favorites = (List<Map<String, Object>>) response.body().get("favorites");

                    for (Map<String, Object> item : favorites) {
                        Map<String, Object> bookData = (Map<String, Object>) item.get("book");
                        Book book = new Book();
                        book.setId((String) bookData.get("_id"));
                        book.setTitle((String) bookData.get("title"));
                        book.setAuthor((String) bookData.get("author"));
                        book.setCoverUrl((String) bookData.get("cover_url"));

                        addBookView(book);
                    }

                } else {
                    Toast.makeText(FavoriteActivity.this, "Bạn chưa yêu thích cuốn sách nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(FavoriteActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addBookView(Book book) {
        ViewGroup item = (ViewGroup) getLayoutInflater().inflate(R.layout.item_favorite_book, layoutFavorite, false);

        TextView tvTitle = item.findViewById(R.id.tvTitle);
        TextView tvAuthor = item.findViewById(R.id.tvAuthor);
        ImageView imgCover = item.findViewById(R.id.imgCover);

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());

        Glide.with(this)
                .load(book.getCoverUrl().replace("localhost", "10.0.2.2"))
                .into(imgCover);

        item.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            startActivity(intent);
        });

        layoutFavorite.addView(item);
    }
}
