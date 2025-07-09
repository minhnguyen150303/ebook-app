package com.example.ebook.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ebook.R;
import com.example.ebook.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatActivity extends AppCompatActivity {
    private Socket mSocket;
    private LinearLayout messageContainer;
    private EditText editMessage;
    private ScrollView scrollView;
    private String username;

    {
        try {
            // ⚠️ Thay đổi IP này bằng địa chỉ thực tế của server (nếu chạy local thì dùng 10.0.2.2 cho Android Emulator)
            mSocket = IO.socket("http://10.0.2.2:5000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = new SessionManager(this).getUsername();

        scrollView = findViewById(R.id.scrollView);
        messageContainer = findViewById(R.id.messageContainer);
        editMessage = findViewById(R.id.editMessage);
        Button btnSend = findViewById(R.id.btnSend);

        mSocket.connect();

        btnSend.setOnClickListener(v -> {
            String msg = editMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("userId", new SessionManager(this).getUserId());
                    json.put("name", username);
                    json.put("message", msg);
                    json.put("timestamp", System.currentTimeMillis());
                    mSocket.emit("send-message", json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                editMessage.setText("");
            }
        });

        mSocket.on("receive-message", args -> runOnUiThread(() -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String name = data.getString("name");
                String message = data.getString("message");

                TextView tv = new TextView(this);
                tv.setText(name + ": " + message);
                tv.setPadding(16, 8, 16, 8);
                messageContainer.addView(tv);

                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("receive-message");
    }
}
