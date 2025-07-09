// com.example.ebook.socket.SocketManager.java
package com.example.ebook.socket;

import android.util.Log;
import java.net.URISyntaxException;
import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {
    private static Socket socket;
    private static final String SERVER_URL = "http://10.0.2.2:5000"; // dùng trong emulator

    public static Socket getSocket() {
        if (socket == null) {
            try {
                IO.Options options = new IO.Options();
                options.reconnection = true;
                socket = IO.socket(SERVER_URL, options);
            } catch (URISyntaxException e) {
                Log.e("SOCKET", "Lỗi URI Socket", e);
            }
        }
        return socket;
    }
}
