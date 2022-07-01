package com.asanwatch.measure_sk;

import android.app.Application;

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;


public class SocketConnect extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://172.30.1.60:5001");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}