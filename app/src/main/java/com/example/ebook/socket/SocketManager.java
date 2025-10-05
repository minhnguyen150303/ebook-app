package com.example.ebook.socket;

import android.util.Log;
import java.net.URISyntaxException;
import java.util.*;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static Socket socket;
    private static final String DEFAULT_URL = "http://10.0.2.2:5000"; // Emulator
    private static String baseUrl = DEFAULT_URL;
    private static String bearerToken = null;

    // ====== Giữ nguyên API cũ ======
    public static Socket getSocket() {
        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;

                // Nếu đã set token trước đó -> truyền header (phòng khi bạn bật auth cho socket)
                if (bearerToken != null && !bearerToken.isEmpty()) {
                    Map<String, List<String>> headers = new HashMap<>();
                    headers.put("Authorization",
                            Collections.singletonList("Bearer " + bearerToken));
                    options.extraHeaders = headers;
                }

                socket = IO.socket(baseUrl, options);
            } catch (URISyntaxException e) {
                Log.e("SOCKET", "Lỗi URI Socket", e);
            }
        }
        return socket;
    }

    // ====== Thêm tiện ích nhưng không bắt buộc dùng ======
    /** Gọi sớm để đổi URL (device thật dùng IP LAN) &/hoặc gắn token. */
    public static void init(String url, String token) {
        baseUrl = (url == null || url.isEmpty()) ? DEFAULT_URL : url;
        bearerToken = token;
        // nếu đã có socket cũ -> đóng để tạo lại đúng cấu hình
        if (socket != null) {
            try { socket.off(); socket.disconnect(); } catch (Exception ignored) {}
            socket = null;
        }
    }

    public static void connect() {
        Socket s = getSocket();
        if (!s.connected()) s.connect();
    }

    public static void disconnect() {
        if (socket != null) {
            try {
                socket.off("newMessage");
                socket.disconnect();
            } catch (Exception ignored) {}
            socket = null;
        }
    }

    public static boolean isConnected() {
        return socket != null && socket.connected();
    }

    public static void joinRoom(String roomId) {
        Socket s = getSocket();
        if (s != null) s.emit("join-book", roomId);
    }

    public static void onNewMessage(Emitter.Listener listener) {
        Socket s = getSocket();
        if (s != null) s.on("newMessage", listener);
    }

    public static void offNewMessage() {
        Socket s = getSocket();
        if (s != null) s.off("newMessage");
    }
}
