package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook.R;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.BookApi;
import com.example.ebook.api.BookmarkApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterReaderActivity extends AppCompatActivity {

    private TextView tvReading;
    private Button btnPrev, btnNext, btnBack, btnMenu;
    private String bookId;
    private String chapterId;
    private int chapterNumber = 1;
    private int totalChapters = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_reader);

        tvReading = findViewById(R.id.tvReading);
        btnPrev = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);

        bookId = getIntent().getStringExtra("book_id");
        chapterNumber = getIntent().getIntExtra("chapter_number", 1);

        if (bookId == null) {
            Toast.makeText(this, "Thiếu ID sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("CHAPTER_READER", "Book ID: " + bookId + ", Chapter: " + chapterNumber);

        loadChapterContent();
        fetchTotalChapters();

        btnPrev.setOnClickListener(v -> {
            if (chapterNumber > 1) {
                chapterNumber--;
                loadChapterContent();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (chapterNumber < totalChapters) {
                chapterNumber++;
                loadChapterContent();
            }
        });

        btnBack.setOnClickListener(v -> {
            finish(); // trở lại BookDetailActivity
        });

        btnMenu.setOnClickListener(v -> {
            showChapterListDialog();
        });
    }

    private void fetchTotalChapters() {
        BookApi api = ApiClient.getClient(this).create(BookApi.class);
        api.getChaptersByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> chapters = (List<Map<String, Object>>) response.body().get("chapters");
                    totalChapters = chapters.size();
                    updateNavButtons();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("CHAPTER_READER", "Lỗi lấy số chương", t);
            }
        });
    }

    private void updateNavButtons() {
        btnPrev.setEnabled(chapterNumber > 1);
        btnNext.setEnabled(chapterNumber < totalChapters);
        btnPrev.setAlpha(chapterNumber > 1 ? 1.0f : 0.5f);
        btnNext.setAlpha(chapterNumber < totalChapters ? 1.0f : 0.5f);
    }

    private void loadChapterContent() {
        BookApi api = ApiClient.getClient(this).create(BookApi.class);
        api.getChapterByNumber(bookId, chapterNumber).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> chapter = (Map<String, Object>) response.body().get("chapter");
                    if (chapter != null) {
                        chapterId = (String) chapter.get("_id");
                        String title = (String) chapter.get("title");
                        String content = (String) chapter.get("content");

                        tvReading.setText(title + "\n\n" + content);
                        saveBookmark(bookId, chapterId);
                        updateNavButtons();
                    }
                } else {
                    tvReading.setText("Không thể tải nội dung chương.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                tvReading.setText("Lỗi kết nối khi tải chương.");
                Log.e("CHAPTER_READER", "Lỗi tải chương", t);
            }
        });
    }

    private void saveBookmark(String bookId, String chapterId) {
        BookmarkApi api = ApiClient.getClient(this).create(BookmarkApi.class);

        Map<String, Object> body = new HashMap<>();
        body.put("chapterId", chapterId);
        body.put("progress", 0);

        api.saveOrUpdateBookmark(bookId, body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Log.d("BOOKMARK", "Lưu bookmark thành công");
                } else {
                    Log.e("BOOKMARK", "Lỗi khi lưu bookmark: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("BOOKMARK", "Lỗi kết nối khi lưu bookmark", t);
            }
        });
    }

    private void showChapterListDialog() {
        BookApi api = ApiClient.getClient(this).create(BookApi.class);
        api.getChaptersByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> chapters = (List<Map<String, Object>>) response.body().get("chapters");
                    String[] chapterTitles = new String[chapters.size()];

                    for (int i = 0; i < chapters.size(); i++) {
                        Map<String, Object> chap = chapters.get(i);
                        chapterTitles[i] = (String) chap.get("title");
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(ChapterReaderActivity.this);
                    builder.setTitle("Danh sách chương");

                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                            ChapterReaderActivity.this,
                            android.R.layout.simple_list_item_1,
                            chapterTitles
                    ) {
                        @Override
                        public View getView(int position, View convertView, android.view.ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView textView = (TextView) view.findViewById(android.R.id.text1);
                            textView.setPadding(40, 40, 40, 40); // padding tăng khoảng cách giữa các dòng
                            textView.setTextSize(16); // chỉnh cỡ chữ dễ đọc
                            return view;
                        }
                    };

                    builder.setAdapter(adapter, (dialog, which) -> {
                        chapterNumber = which + 1;
                        loadChapterContent();
                    });

                    builder.show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(ChapterReaderActivity.this, "Không thể tải danh sách chương", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
