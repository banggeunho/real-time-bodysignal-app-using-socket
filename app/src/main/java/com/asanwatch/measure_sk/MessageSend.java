package com.asanwatch.measure_sk;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.JSONObject;


public class MessageSend extends AsyncTask<Void, String, Boolean> {
    private BufferedWriter out;
    private Socket socket;
    private String value;
    private boolean interrupted = false;
    private String TAG = getClass().getName();

    public MessageSend(Socket socket, String value) {
        this.socket = socket;
        this.value = value;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            Log.d(TAG, "Opening socket connection.");
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            JSONObject json = new JSONObject();
            json.put("service", value);

            out.write(json.toString());
            out.flush();
            Log.d(TAG, json.toString());

        } catch (UnknownHostException ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            return false;
        } catch (IOException ex) {
            Log.d(TAG, "doInBackground(): " + ex.toString());
            return false;
        } catch (Exception ex) {
            Log.e(TAG, "doInBackground(): " + ex.toString());
            return false;
        }

        return true;
    }

    public void disconnect() {
        try {
            Log.d(TAG, "Closing the socket connection.");

            interrupted = true;
            if(socket != null) {
                socket.close();
            }
            if(out != null) {
                out.close();
            }
        } catch (IOException ex) {
            Log.e(TAG, "disconnect(): " + ex.toString());
        }
    }

}
