// CategoryManagementActivity.java
package com.example.ebook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebook.R;
import com.example.ebook.adapter.CategoryAdapter;
import com.example.ebook.model.Category;
import com.example.ebook.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter adapter;
    private CategoryRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        recyclerView = findViewById(R.id.recyclerCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        repository = new CategoryRepository(this);

        adapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onDelete(Category category) {
                deleteCategory(category);
            }

            @Override
            public void onDetail(Category category) {
                Intent intent = new Intent(CategoryManagementActivity.this, BookListByCategoryActivity.class);
                intent.putExtra("categoryId", category.getId());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAddCategory).setOnClickListener(v -> showAddCategoryDialog());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadCategories();
    }

    private void loadCategories() {
        repository.getAllCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    List<Map<String, Object>> raw = (List<Map<String, Object>>) response.body().get("categories");
                    for (Map<String, Object> item : raw) {
                        Category c = new Category();
                        c._id = (String) item.get("_id");
                        c.name = (String) item.get("name");
                        categoryList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CategoryManagementActivity.this, "Không thể tải thể loại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CategoryManagementActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Thêm thể loại")
                .setView(input)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) addCategory(name);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addCategory(String name) {
        repository.addCategory(name).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoryManagementActivity.this, "Đã thêm", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(CategoryManagementActivity.this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CategoryManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCategory(Category category) {
        repository.deleteCategory(category.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoryManagementActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                    loadCategories();
                } else {
                    Toast.makeText(CategoryManagementActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(CategoryManagementActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
