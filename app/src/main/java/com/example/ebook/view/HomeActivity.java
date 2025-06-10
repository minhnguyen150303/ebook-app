package com.example.ebook.view;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import android.widget.PopupMenu;
import com.example.ebook.R;
import com.example.ebook.utils.SessionManager;

public class HomeActivity extends AppCompatActivity {
    private LinearLayout layoutCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        String username = new SessionManager(this).getUsername();
        getSupportActionBar().setTitle("EBook");

        layoutCategories = findViewById(R.id.layoutCategories);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        Menu popupMenu = popup.getMenu();
        String username = new SessionManager(this).getUsername();
        popupMenu.findItem(R.id.menu_username).setTitle("👤 " + username);

        String role = new SessionManager(this).getRole();

        Menu menu = popup.getMenu();
        if (!"admin".equalsIgnoreCase(role)) {
            menu.findItem(R.id.menu_user_management).setVisible(false);
        }

        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_info) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
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
            }else if (menuItem.getItemId() == R.id.menu_change_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
                return true;
            }else if (menuItem.getItemId() == R.id.menu_user_management) {
                startActivity(new Intent(this, UserManagementActivity.class));
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

}
