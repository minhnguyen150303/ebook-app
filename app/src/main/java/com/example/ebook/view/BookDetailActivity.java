package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.ebook.api.ApiService;
import com.google.gson.Gson;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.socket.client.Socket;
import com.example.ebook.socket.SocketManager;

import com.bumptech.glide.Glide;
import com.example.ebook.R;
import com.example.ebook.adapter.CommentAdapter;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.BookApi;
import com.example.ebook.api.BookmarkApi;
import com.example.ebook.api.CommentApi;
import com.example.ebook.api.FavoriteApi;
import com.example.ebook.model.ApiResponse;
import com.example.ebook.model.Book;
import com.example.ebook.model.Comment;
import com.example.ebook.model.CommentRequest;
import com.example.ebook.model.CommentResponse;
import com.example.ebook.model.SingleCommentResponse;
import com.example.ebook.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetailActivity extends AppCompatActivity {

    private boolean isCommentsExpanded = false;
    private TextView tvExpandComments;
    private ImageView imgCover;
    private TextView tvTitle, tvAuthor, tvViews, tvDescription, tvExpandDescription, tvExpandChapters;
    private LinearLayout layoutChapters;
    private Button btnRead, btnFavorite;
    private Button btnBackToHome;
    private boolean isFavorite = false;
    private String bookId;
    private Book book;

    private RecyclerView rvComments;
    private EditText edtComment;
    private Button btnSubmitComment;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private CommentApi commentApi;
    private SessionManager sessionManager;
    private String token, role, userId;

    private Button btnToggleChapters;
    private boolean isChapterListVisible = false;
    private List<Map<String, Object>> allChapters = new ArrayList<>();
    private RatingBar ratingBarInput;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        imgCover = findViewById(R.id.imgCover);
        tvTitle = findViewById(R.id.tvTitle);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvViews = findViewById(R.id.tvViews);
        tvDescription = findViewById(R.id.tvDescription);
        tvExpandDescription = findViewById(R.id.tvExpandDescription);
        //tvExpandChapters = findViewById(R.id.tvExpandChapters);
        layoutChapters = findViewById(R.id.layoutChapters);
        btnRead = findViewById(R.id.btnRead);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        ratingBarInput = findViewById(R.id.ratingBarInput);


        bookId = getIntent().getStringExtra("BOOK_ID");
        if (bookId == null) {
            Toast.makeText(this, "Không có ID sách", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Socket socket = SocketManager.getSocket();
        socket.connect(); // Kết nối socket
        socket.emit("join-book", bookId); // Tham gia room theo bookId

        socket.on("new-comment", args -> runOnUiThread(() -> {
            Log.d("SOCKET", "new-comment nhận được");
            loadComments();
        }));

        socket.on("comment-updated", args -> runOnUiThread(() -> {
            Log.d("SOCKET", "comment-updated nhận được");
            loadComments();
        }));

        socket.on("comment-deleted", args -> runOnUiThread(() -> {
            Log.d("SOCKET", "comment-deleted nhận được");
            loadComments();
        }));

        socket.on("user-status-changed", args -> runOnUiThread(() -> {
            Log.d("SOCKET", "user-status-changed nhận được");
            loadComments();
        }));



        rvComments = findViewById(R.id.rvComments);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitComment = findViewById(R.id.btnSubmitComment);

        sessionManager = new SessionManager(this);
        token = sessionManager.getToken();
        role = sessionManager.getRole();
        userId = sessionManager.getUserId();

        commentApi = ApiClient.getClient(this).create(CommentApi.class);
        commentAdapter = new CommentAdapter(this, commentList, new CommentAdapter.OnCommentActionListener() {
            public void onEdit(Comment comment) { showEditDialog(comment); }
            public void onDelete(Comment comment) { deleteComment(comment.get_id()); }
            public void onToggle(Comment comment) { toggleComment(comment.get_id()); }
            public void onToggleUser(String userId) {
                toggleUserStatus(userId);
            }
        });
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);

        btnSubmitComment.setOnClickListener(v -> submitComment());
        loadComments();  // Gọi khi khởi động

        tvExpandComments = findViewById(R.id.tvExpandComments);
        tvExpandComments.setOnClickListener(v -> toggleCommentList());
        Log.d("EDIT_COMMENT", "TEST LOG IN onCreate");

        btnBackToHome.setOnClickListener(v -> {
            finish();
        });

        btnToggleChapters = findViewById(R.id.btnToggleChapters);

        btnToggleChapters.setOnClickListener(v -> {
            if (isChapterListVisible) {
                layoutChapters.removeAllViews();
                btnToggleChapters.setText("☰ Hiện danh sách chương");
                isChapterListVisible = false;
            } else {
                showFullChapterList();
                btnToggleChapters.setText("☰ Ẩn danh sách chương");
                isChapterListVisible = true;
            }
        });


        loadBookDetails();
        loadBookmarkState();
        loadFavoriteState();
        loadChapters();
    }

    private void loadBookDetails() {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.getBookById(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("book") != null) {
                    Map<String, Object> data = (Map<String, Object>) response.body().get("book");

                    book = new Book();
                    book.setId((String) data.get("_id"));
                    book.setTitle((String) data.get("title"));
                    book.setAuthor((String) data.get("author"));
                    book.setDescription((String) data.get("description"));
                    book.setCoverUrl((String) data.get("cover_url"));
                    book.setActive((boolean) data.get("is_active"));

                    Object viewsObj = data.get("views");
                    book.setViews(viewsObj instanceof Number ? ((Number) viewsObj).intValue() : 0);

                    tvTitle.setText(book.getTitle());
                    tvAuthor.setText(book.getAuthor());
                    tvViews.setText("\uD83D\uDC41 " + book.getViews() + " lượt đọc");
                    tvDescription.setText(book.getDescription());

                    Glide.with(BookDetailActivity.this)
                            .load(book.getCoverUrl().replace("localhost", "10.0.2.2"))
                            .placeholder(R.drawable.placeholder)
                            .into(imgCover);

                    final boolean[] isDescriptionExpanded = {false};
                    tvExpandDescription.setOnClickListener(v -> {
                        if (!isDescriptionExpanded[0]) {
                            tvDescription.setMaxLines(Integer.MAX_VALUE);
                            tvExpandDescription.setText("Thu gọn");
                        } else {
                            tvDescription.setMaxLines(3);
                            tvExpandDescription.setText("Xem thêm");
                        }
                        isDescriptionExpanded[0] = !isDescriptionExpanded[0];
                    });
                } else {
                    Log.e("BOOK_DETAIL", "Lỗi phản hồi hoặc không có book: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("BOOK_DETAIL", "Lỗi kết nối khi lấy chi tiết sách", t);
            }
        });
    }

    private void loadBookmarkState() {
        BookmarkApi bookmarkApi = ApiClient.getClient(this).create(BookmarkApi.class);

        bookmarkApi.getBookmarkByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().get("bookmark") != null) {
                    btnRead.setText("Đọc tiếp");
                } else {
                    btnRead.setText("Đọc từ đầu");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnRead.setText("Đọc từ đầu");
            }
        });

        btnRead.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, ChapterReaderActivity.class);
            intent.putExtra("book_id", bookId);

            bookmarkApi.getBookmarkByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().get("bookmark") != null) {
                        Map<String, Object> bookmark = (Map<String, Object>) response.body().get("bookmark");
                        Map<String, Object> chapter = (Map<String, Object>) bookmark.get("chapter");
                        if (chapter != null && chapter.get("chapter_number") != null) {
                            int chapterNumber = ((Double) chapter.get("chapter_number")).intValue();
                            intent.putExtra("chapter_number", chapterNumber);
                        }
                    }
                    startActivity(intent);
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    startActivity(intent);
                }
            });
        });
    }


    private void loadFavoriteState() {
        FavoriteApi api = ApiClient.getClient(this).create(FavoriteApi.class);
        api.getFavorites().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> favorites = (List<Map<String, Object>>) response.body().get("favorites");
                    isFavorite = false;
                    for (Map<String, Object> fav : favorites) {
                        Map<String, Object> bookMap = (Map<String, Object>) fav.get("book");
                        if (bookMap != null && bookId.equals(bookMap.get("_id"))) {
                            isFavorite = true;
                            break;
                        }
                    }
                    btnFavorite.setText(isFavorite ? "💔 Bỏ thích" : "❤️ Thích");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                btnFavorite.setText("❤️ Thích");
            }
        });

        btnFavorite.setOnClickListener(v -> {
            FavoriteApi favoriteApi = ApiClient.getClient(this).create(FavoriteApi.class);
            favoriteApi.toggleFavorite(bookId).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean newState = (Boolean) response.body().get("is_favorite");
                        isFavorite = newState;
                        btnFavorite.setText(isFavorite ? "💔 Bỏ thích" : "❤️ Thích");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FAVORITE", "Lỗi toggle yêu thích", t);
                }
            });
        });
    }

//    private void loadChapters() {
//        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
//        bookApi.getChaptersByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
//            @Override
//            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Map<String, Object>> chapters = (List<Map<String, Object>>) response.body().get("chapters");
//                    layoutChapters.removeAllViews();
//
//                    int limit = Math.min(9, chapters.size());
//                    for (int i = 0; i < limit; i++) {
//                        Map<String, Object> chap = chapters.get(i);
//                        TextView tv = new TextView(BookDetailActivity.this);
//                        tv.setText((String) chap.get("title"));
//                        int chapterNumber = ((Double) chap.get("chapter_number")).intValue();
//                        tv.setPadding(0, 16, 0, 16);
//                        tv.setOnClickListener(v -> openChapter(chapterNumber));
//                        layoutChapters.addView(tv);
//                    }
//
//                    if (chapters.size() > 9) {
//                        tvExpandChapters.setVisibility(View.VISIBLE);
//                        final boolean[] isChaptersExpanded = {false};
//
//                        tvExpandChapters.setOnClickListener(v -> {
//                            layoutChapters.removeAllViews();
//                            if (!isChaptersExpanded[0]) {
//                                for (Map<String, Object> chap : chapters) {
//                                    TextView tv = new TextView(BookDetailActivity.this);
//                                    tv.setText((String) chap.get("title"));
//                                    int chapterNumber = ((Double) chap.get("chapter_number")).intValue();
//                                    tv.setPadding(0, 16, 0, 16);
//                                    tv.setOnClickListener(view -> openChapter(chapterNumber));
//                                    layoutChapters.addView(tv);
//                                }
//                                tvExpandChapters.setText("Thu gọn");
//                            } else {
//                                for (int i = 0; i < 9; i++) {
//                                    Map<String, Object> chap = chapters.get(i);
//                                    TextView tv = new TextView(BookDetailActivity.this);
//                                    tv.setText((String) chap.get("title"));
//                                    int chapterNumber = ((Double) chap.get("chapter_number")).intValue();
//                                    tv.setPadding(0, 16, 0, 16);
//                                    tv.setOnClickListener(view -> openChapter(chapterNumber));
//                                    layoutChapters.addView(tv);
//                                }
//                                tvExpandChapters.setText("Xem tất cả chương");
//                            }
//                            isChaptersExpanded[0] = !isChaptersExpanded[0];
//                        });
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
//                Log.e("CHAPTERS", "Lỗi lấy chương", t);
//            }
//        });
//    }

    private void loadChapters() {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.getChaptersByBook(bookId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allChapters = (List<Map<String, Object>>) response.body().get("chapters");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("CHAPTERS", "Lỗi lấy chương", t);
            }
        });
    }

    private void showFullChapterList() {
        layoutChapters.removeAllViews();

        for (Map<String, Object> chap : allChapters) {
            TextView tv = new TextView(BookDetailActivity.this);
            tv.setText((String) chap.get("title")); // Hoặc dùng custom format nếu muốn
            tv.setTextSize(16);
            tv.setPadding(0, 16, 0, 16);
            int chapterNumber = ((Double) chap.get("chapter_number")).intValue();

            tv.setOnClickListener(view -> openChapter(chapterNumber));
            layoutChapters.addView(tv);
        }
    }



    private void openChapter(int chapterNumber) {
        Intent intent = new Intent(BookDetailActivity.this, ChapterReaderActivity.class);
        intent.putExtra("book_id", bookId);
        intent.putExtra("chapter_number", chapterNumber);
        startActivity(intent);
    }

    private void loadComments() {
        Call<CommentResponse> call;

        if ("admin".equals(role)) {
            // 👑 Admin thấy tất cả bình luận, kể cả bị ẩn
            call = commentApi.getAllCommentsByBook(bookId);
        } else {
            // 👤 User thường chỉ thấy bình luận đang hiện
            call = commentApi.getCommentsByBook(bookId);
        }

        call.enqueue(new Callback<CommentResponse>() {
            @Override
            public void onResponse(Call<CommentResponse> call, Response<CommentResponse> res) {
                if (res.isSuccessful() && res.body().isSuccess()) {
                    commentList = res.body().getComments();
                    updateCommentList(); // cập nhật danh sách comment
                }
            }

            @Override
            public void onFailure(Call<CommentResponse> call, Throwable t) {
                Log.e("COMMENT", "Lỗi khi load comment", t);
            }
        });
    }


    private void updateCommentList() {
        if (commentList == null) return;
        Log.d("COMMENTS", "commentList.size = " + commentList.size());

        if (commentList.size() <= 10) {
            commentAdapter.setData(commentList);
            tvExpandComments.setVisibility(View.GONE);
        } else {
            if (isCommentsExpanded) {
                commentAdapter.setData(commentList);
                tvExpandComments.setText("Thu gọn");
            } else {
                commentAdapter.setData(commentList.subList(0, 10));
                tvExpandComments.setText("Xem thêm");
            }
            tvExpandComments.setVisibility(View.VISIBLE);
        }
    }

    private void toggleCommentList() {
        isCommentsExpanded = !isCommentsExpanded;
        updateCommentList();
    }

//    private void submitComment() {
//        String content = edtComment.getText().toString().trim();
//        float rating = ratingBarInput.getRating();
//
//        if (content.isEmpty() || rating == 0f) {
//            Toast.makeText(this, "Vui lòng nhập bình luận và chọn số sao", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        CommentRequest req = new CommentRequest(bookId, content, rating);
//        req.setRating(rating); // ✅ Gửi số sao vào request
//        Log.d("COMMENT_REQUEST", "Request: bookId = " + bookId + ", content = " + content + ", rating = " + rating);
//
//        commentApi.createComment(req).enqueue(new Callback<SingleCommentResponse>() {
//            @Override
//            public void onResponse(Call<SingleCommentResponse> call, Response<SingleCommentResponse> res) {
//                if (res.isSuccessful() && res.body().isSuccess()) {
//                    edtComment.setText("");
//                    ratingBarInput.setRating(0f);
//                    loadComments();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SingleCommentResponse> call, Throwable t) {
//                Toast.makeText(BookDetailActivity.this, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
private void submitComment() {
    String content = edtComment.getText().toString().trim();
    float rating = ratingBarInput.getRating(); // Lấy số sao từ RatingBar

    // Kiểm tra nếu content hoặc rating không hợp lệ
    if (content.isEmpty() || rating == 0f) {
        Toast.makeText(this, "Vui lòng nhập bình luận và chọn số sao hợp lệ", Toast.LENGTH_SHORT).show();
        return;
    }

    // Gửi yêu cầu tạo bình luận mới
    CommentRequest req = new CommentRequest(bookId, content, rating);
    Log.d("COMMENT_REQUEST", "Request: bookId = " + bookId + ", content = " + content + ", rating = " + rating);

    commentApi.createComment(req).enqueue(new Callback<SingleCommentResponse>() {
        @Override
        public void onResponse(Call<SingleCommentResponse> call, Response<SingleCommentResponse> res) {
            if (res.isSuccessful() && res.body().isSuccess()) {
                edtComment.setText("");  // Reset lại trường nhập liệu
                ratingBarInput.setRating(0f);  // Reset lại RatingBar
                loadComments();  // Cập nhật lại danh sách bình luận
            } else {
                Toast.makeText(BookDetailActivity.this, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<SingleCommentResponse> call, Throwable t) {
            Toast.makeText(BookDetailActivity.this, "Lỗi khi gửi bình luận", Toast.LENGTH_SHORT).show();
            Log.e("COMMENT_REQUEST", "Error: " + t.getMessage());
        }
    });
}


    private void deleteComment(String commentId) {
        commentApi.deleteComment(commentId).enqueue(new Callback<ApiResponse>() {
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> res) {
                if (res.isSuccessful() && res.body().isSuccess()) {
                    commentAdapter.removeComment(commentId); // ✅ xóa trên UI
                }
            }
            public void onFailure(Call<ApiResponse> call, Throwable t) { }
        });
    }


    private void toggleComment(String commentId) {
        commentApi.toggleComment(commentId).enqueue(new Callback<ApiResponse>() {
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> res) {
                if (res.isSuccessful() && res.body().isSuccess()) {
                    loadComments();
                }
            }
            public void onFailure(Call<ApiResponse> call, Throwable t) { }
        });
    }


//    private void showEditDialog(Comment comment) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Sửa bình luận");
//
//        final EditText input = new EditText(this);
//        input.setText(comment.getComment());
//        builder.setView(input);
//
//        builder.setPositiveButton("Lưu", (dialog, which) -> {
//            String newContent = input.getText().toString().trim();
//            if (!newContent.isEmpty()) {
//                float newRating = ratingBarInput.getRating();
//                CommentRequest req = new CommentRequest(bookId, newContent, newRating);
//                Log.d("EDIT_COMMENT_REQUEST", "Request: bookId = " + bookId + ", content = " + newContent + ", rating = " + newRating);
//                commentApi.updateComment(comment.get_id(), req).enqueue(new Callback<SingleCommentResponse>() {
//                    public void onResponse(Call<SingleCommentResponse> call, Response<SingleCommentResponse> res) {
//                        Log.d("EDIT_COMMENT", "Status code = " + res.code());
//                        Log.d("EDIT_COMMENT", "Body = " + new Gson().toJson(res.body()));
//                        if (res.isSuccessful() && res.body() != null && res.body().getComment() != null) {
//                            Comment updated = res.body().getComment();
//                            commentAdapter.updateComment(updated);
//                            for (int i = 0; i < commentList.size(); i++) {
//                                if (commentList.get(i).get_id().equals(updated.get_id())) {
//                                    commentList.set(i, updated);
//                                    break;
//                                }
//                            }
//                            Toast.makeText(BookDetailActivity.this, "Đã sửa bình luận", Toast.LENGTH_SHORT).show();
//                        } else {
//                            String errorMsg = "Không thể sửa bình luận";
//                            try {
//                                if (res.errorBody() != null) {
//                                    String errorJson = res.errorBody().string();
//                                    Log.d("EDIT_COMMENT", "ErrorBody = " + (res.errorBody() != null ? res.errorBody().toString() : "null"));
//                                    Map<String, Object> errorMap = new com.google.gson.Gson().fromJson(errorJson, Map.class);
//                                    if (errorMap.get("message") != null) {
//                                        errorMsg = errorMap.get("message").toString();
//                                    }
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            Toast.makeText(BookDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    public void onFailure(Call<SingleCommentResponse> call, Throwable t) {
//                        Log.e("EDIT_COMMENT", "onFailure: " + t.getMessage(), t);
//                        Toast.makeText(BookDetailActivity.this, "Lỗi sửa bình luận", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//
//        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
//        builder.show();
//    }
private void showEditDialog(Comment comment) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Sửa bình luận");

    final EditText input = new EditText(this);
    input.setText(comment.getComment());
    builder.setView(input);

    ratingBarInput.setRating(comment.getRating());

    builder.setPositiveButton("Lưu", (dialog, which) -> {
        String newContent = input.getText().toString().trim();

        if (newContent.isEmpty()) {
            Toast.makeText(BookDetailActivity.this, "Vui lòng nhập nội dung bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        float currentRating = comment.getRating();

        CommentRequest req = new CommentRequest(bookId, newContent, currentRating);
        Log.d("EDIT_COMMENT_REQUEST", "Request: bookId = " + bookId + ", content = " + newContent + ", rating = " + currentRating);

        commentApi.updateComment(comment.get_id(), req).enqueue(new Callback<SingleCommentResponse>() {
            @Override
            public void onResponse(Call<SingleCommentResponse> call, Response<SingleCommentResponse> res) {
                if (res.isSuccessful() && res.body().isSuccess()) {
                    Comment updatedComment = res.body().getComment();
                    commentAdapter.updateComment(updatedComment);  
                    Toast.makeText(BookDetailActivity.this, "Bình luận đã được sửa", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookDetailActivity.this, "Không thể sửa bình luận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SingleCommentResponse> call, Throwable t) {
                Log.e("EDIT_COMMENT", "Error: " + t.getMessage());
                Toast.makeText(BookDetailActivity.this, "Lỗi sửa bình luận", Toast.LENGTH_SHORT).show();
            }
        });
    });

    builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
    builder.show();
}




    private void toggleUserStatus(String userId) {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.toggleUserStatus(userId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookDetailActivity.this, "Cập nhật trạng thái người dùng thành công", Toast.LENGTH_SHORT).show();
                    loadComments(); // ✅ Refresh lại comment
                } else {
                    Toast.makeText(BookDetailActivity.this, "Lỗi cập nhật trạng thái người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(BookDetailActivity.this, "Lỗi mạng khi cập nhật người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Socket socket = SocketManager.getSocket();
        socket.off("new-comment");
        socket.off("comment-updated");
        socket.off("comment-deleted");
        socket.off("user-status-changed");

        socket.disconnect();
    }

}
