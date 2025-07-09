// HomeActivity.java
package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;

import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.example.ebook.adapter.GridBookAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.ebook.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.adapter.BookHorizontalAdapter;
import com.example.ebook.api.ApiClient;
import com.example.ebook.api.BookApi;
import com.example.ebook.api.CategoryApi;
import com.example.ebook.model.Book;
import com.example.ebook.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout layoutHome;
    private LinearLayout categoryTabs;
    private NestedScrollView nestedScrollView;
    private Map<String, View> sectionViewsMap = new HashMap<>();

    private Map<View, String> viewToTitleMap = new HashMap<>();
    private TextView currentSelectedTab = null;
    private RecyclerView recyclerSearchResults;
    private GridBookAdapter searchAdapter;
    private List<Book> allBooks = new ArrayList<>();
    private int pendingCategoryCount = 0;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        String username = new SessionManager(this).getUsername();
        getSupportActionBar().setTitle("EBook");

        layoutHome = findViewById(R.id.layoutCategories);

        recyclerSearchResults = findViewById(R.id.recyclerSearchResults);
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(this, 3));
        searchAdapter = new GridBookAdapter(this, new ArrayList<>(), book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            startActivity(intent);
        });
        recyclerSearchResults.setAdapter(searchAdapter);

        fetchAllBooksForSearch();


        categoryTabs = findViewById(R.id.categoryTabs);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            for (Map.Entry<View, String> entry : viewToTitleMap.entrySet()) {
                View section = entry.getKey();
                String title = entry.getValue();
                int[] location = new int[2];
                section.getLocationOnScreen(location);
                int y = location[1];

                int screenHeight = nestedScrollView.getHeight();
                if (y >= 0 && y <= screenHeight * 2 / 3) {
                    highlightTab(title);
                    break;
                }
            }
        });


        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home); // chọn mặc định là Home

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // TODO: code chuyển trang Home
                return true;
            } else if (id == R.id.nav_library) {
                startActivity(new Intent(this, LibraryActivity.class));
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            } else if (id == R.id.nav_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                return true;
            }
            return false;
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        // Clear layout và reload lại dữ liệu
        layoutHome.removeAllViews();
        sectionViewsMap.clear();
        viewToTitleMap.clear();
        categoryTabs.removeAllViews();
        currentSelectedTab = null;

        loadTopBooks();
        loadAllCategoriesWithBooks();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Tìm tên sách...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBooks(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                recyclerSearchResults.setVisibility(View.VISIBLE);
                nestedScrollView.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                recyclerSearchResults.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
                return true;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_more) {
            Toolbar toolbar = findViewById(R.id.topAppBar);
            showPopupMenu(toolbar);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterBooks(String query) {
        List<Book> filtered = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(book);
            }
        }

        if (filtered.size() > 15) {
            filtered = filtered.subList(0, 15);
        }

        searchAdapter.setBooks(filtered);
    }



    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        String username = new SessionManager(this).getUsername();
        popupMenu.findItem(R.id.menu_username).setTitle("👤 " + username);

        String role = new SessionManager(this).getRole();

        if (!"admin".equalsIgnoreCase(role)) {
            popupMenu.findItem(R.id.menu_user_management).setVisible(false);
            popupMenu.findItem(R.id.menu_category_management).setVisible(false);
            popupMenu.findItem(R.id.menu_report).setVisible(false);
        }

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_info) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (menuItem.getItemId() == R.id.menu_logout) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Xác nhận đăng xuất!")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            new SessionManager(this).saveToken(null);
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            } else if (menuItem.getItemId() == R.id.menu_change_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
                return true;
            } else if (menuItem.getItemId() == R.id.menu_user_management) {
                startActivity(new Intent(this, UserManagementActivity.class));
                return true;
            } else if (menuItem.getItemId() == R.id.menu_category_management) {
                startActivity(new Intent(this, CategoryManagementActivity.class));
                return true;
            }else if (menuItem.getItemId() == R.id.menu_report) {
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            }
            return false;
        });

        try {
            java.lang.reflect.Field[] fields = popup.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    java.lang.reflect.Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.show();
    }

    private void fetchBookDetails(Book book, List<Book> targetList, Runnable onComplete) {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        Log.d("DEBUG", "Lấy chi tiết sách: " + book.getId());
        bookApi.getBookByIdNoView(book.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.body() == null) {
                    Log.e("DEBUG", "response body null");
                    return;
                }
                if (response.isSuccessful()) {
                    Map<String, Object> data = (Map<String, Object>) response.body().get("book");
                    book.setTitle((String) data.get("title"));
                    book.setAuthor((String) data.get("author"));
                    book.setCoverUrl((String) data.get("cover_url"));
                    targetList.add(book);
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("DEBUG", "Lỗi khi lấy chi tiết sách", t);
            }
        });
    }

    private void loadTopBooks() {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.getTopViewedBooks(5).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("data");
                    List<Book> topBooks = new ArrayList<>();

                    for (Map<String, Object> item : data) {
                        Book book = new Book();
                        book.setId((String) item.get("_id"));
                        book.setTitle((String) item.get("title"));
                        book.setAuthor((String) item.get("author"));
                        book.setCoverUrl((String) item.get("cover_url"));
                        book.setViews(((Number) item.get("views")).intValue());

                        topBooks.add(book);
                    }

                    addSection("Sách nổi bật trong tuần", topBooks);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi mạng khi tải sách nổi bật", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAllCategoriesWithBooks() {
        CategoryApi categoryApi = ApiClient.getClient(this).create(CategoryApi.class);
        categoryApi.getAllCategories().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> categories = (List<Map<String, Object>>) response.body().get("categories");
                    pendingCategoryCount = categories.size();

                    for (Map<String, Object> map : categories) {
                        String id = (String) map.get("_id");
                        String name = (String) map.get("name");
                        loadBooksByCategory(id, name);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi tải thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBooksByCategory(String categoryId, String categoryName) {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.getAllBooksByCategory(categoryId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("books");
                    List<Book> books = new ArrayList<>();
                    final int total = data.size();
                    final int[] count = {0};

                    for (Map<String, Object> item : data) {
                        Book book = new Book();
                        book.setId((String) item.get("_id"));

                        fetchBookDetails(book, books, () -> {
                            count[0]++;
                            if (count[0] == total) {
                                addSection(categoryName, books);

                                pendingCategoryCount--;
                                if (pendingCategoryCount == 0) {
                                    ensureScrollableToBottom();
                                }
                            }
                        });
                    }

                    if (data.isEmpty()) {
                        pendingCategoryCount--;
                        if (pendingCategoryCount == 0) {
                            ensureScrollableToBottom();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi tải sách theo thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addSection(String title, List<Book> books) {
        View sectionView = getLayoutInflater().inflate(R.layout.section_horizontal_books, layoutHome, false);
        TextView tvSection = sectionView.findViewById(R.id.tvSectionTitle);
        RecyclerView rvBooks = sectionView.findViewById(R.id.recyclerSectionBooks);

        tvSection.setText(title);
        rvBooks.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        BookHorizontalAdapter adapter = new BookHorizontalAdapter(this, books, book -> {
            Intent intent = new Intent(this, BookDetailActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            startActivity(intent);
        });
        rvBooks.setAdapter(adapter);

        new androidx.recyclerview.widget.LinearSnapHelper().attachToRecyclerView(rvBooks);

        // Auto scroll books
        final Handler handler = new Handler(Looper.getMainLooper());
        final int scrollDelay = 2000;
        final Runnable[] runnable = new Runnable[1];
        runnable[0] = new Runnable() {
            int position = 0;

            @Override
            public void run() {
                position++;
                if (position >= books.size()) {
                    position = 0;
                    rvBooks.scrollToPosition(0);
                } else {
                    rvBooks.smoothScrollToPosition(position);
                }
                handler.postDelayed(runnable[0], scrollDelay);
            }
        };
        handler.postDelayed(runnable[0], scrollDelay);

        // Thêm view vào màn hình và lưu vào map
        layoutHome.addView(sectionView);
        sectionViewsMap.put(title, sectionView);
        viewToTitleMap.put(sectionView, title);

        // Thêm tab thể loại phía trên
        addCategoryTab(title, v -> scrollToSection(title));
    }

    private void ensureScrollableToBottom() {
        layoutHome.post(() -> {
            int contentHeight = layoutHome.getHeight();
            int scrollViewHeight = nestedScrollView.getHeight();

            int maxScroll = contentHeight - scrollViewHeight;

            // Nếu maxScroll < vị trí section cuối, thêm view trắng
            int highestSectionBottom = 0;
            for (View v : sectionViewsMap.values()) {
                int bottom = v.getBottom();
                if (bottom > highestSectionBottom) {
                    highestSectionBottom = bottom;
                }
            }

            if (highestSectionBottom > maxScroll + scrollViewHeight) {
                return;
            }

            int requiredSpace = (highestSectionBottom + 200) - (maxScroll + scrollViewHeight);
            if (requiredSpace > 0) {
                Log.d("SCROLL_DEBUG", " Thêm khoảng trắng cuối: " + requiredSpace + "px");
                View spacer = new View(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        requiredSpace
                ));
                layoutHome.addView(spacer);
            }
        });
    }

    private void addCategoryTab(String title, View.OnClickListener onClick) {
        TextView tab = new TextView(this);
        tab.setText(title);
        tab.setPadding(32, 16, 32, 16);
        tab.setTextSize(16);
        tab.setBackgroundResource(R.drawable.tab_background);
        tab.setTextColor(getResources().getColor(R.color.black));
        tab.setOnClickListener(onClick);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(16, 8, 16, 8);
        tab.setLayoutParams(lp);

        categoryTabs.addView(tab);
    }


    private void scrollToSection(String title) {
        View section = sectionViewsMap.get(title);
        if (section != null) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                int targetY = section.getTop() - 100;

                int contentHeight = layoutHome.getHeight();
                int scrollViewHeight = nestedScrollView.getHeight();
                int maxScroll = contentHeight - scrollViewHeight;

                int scrollToY = Math.min(targetY, maxScroll);
                if (scrollToY < 0) scrollToY = 0;

                nestedScrollView.smoothScrollTo(0, scrollToY);
            }, 200); // delay 200ms
        }
    }





    private void highlightTab(String title) {
        for (int i = 0; i < categoryTabs.getChildCount(); i++) {
            View child = categoryTabs.getChildAt(i);

            if (!(child instanceof TextView)) continue;

            TextView tab = (TextView) child;
            if (tab.getText().toString().equals(title)) {
                if (currentSelectedTab != tab) {
                    if (currentSelectedTab != null) {
                        currentSelectedTab.setBackgroundResource(R.drawable.tab_background);
                    }
                    tab.setBackgroundResource(R.drawable.tab_selected_background);
                    currentSelectedTab = tab;

                    // Đảm bảo tab được focus nếu nằm ngoài tầm nhìn
                    tab.getParent().requestChildFocus(tab, tab);
                }
                break;
            }
        }
    }


    private void fetchAllBooksForSearch() {
        BookApi bookApi = ApiClient.getClient(this).create(BookApi.class);
        bookApi.getAllBooks().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.body().get("books");
                    for (Map<String, Object> item : data) {
                        Book book = new Book();
                        book.setId((String) item.get("_id"));
                        book.setTitle((String) item.get("title"));
                        book.setAuthor((String) item.get("author"));
                        book.setCoverUrl((String) item.get("cover_url"));
                        allBooks.add(book);
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Không tải được sách tìm kiếm", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
