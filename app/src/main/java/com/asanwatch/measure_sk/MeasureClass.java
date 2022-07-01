package com.asanwatch.measure_sk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

public class MeasureClass extends Service implements SensorEventListener {
    private static final String TAG = "MeasureForegroundService";
    private SensorManager manager;
    private Sensor mHeartRate;
    private Socket mSocket;
    private String tag = "0";
    private String time = "";
    private float value;
    private int count = 0;

    @Override
    public void onStart(Intent intent, int startId){
        foregroundNotification();
        initSocket();
        initSensors();
    }

    @Override
    public void onDestroy(){
        try {
            unregister();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        JSONObject data = new JSONObject();
        Sensor sensor = event.sensor;
        long date = System.currentTimeMillis();
        if (sensor.getType() == Sensor.TYPE_HEART_RATE) {
            time = Long.toString(date);
            value = event.values[0];
            try {
                data.put("device", SharedObjects.deviceId);
                data.put("tag", tag);
                data.put("time", time);
                data.put("value", value);
                data.put("index", count);
                Log.d("123", data.toString());
                mSocket.emit("service", data);
            } catch (JSONException e){
                e.printStackTrace();
            }
            count += 1;
            if(SharedObjects.isWake){
                MainActivity.setMeasureText("Heart-Rate : " + event.values[0] + "\nCount :" + count);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void foregroundNotification() { // foreground 실행 후 신호 전달 (안하면 앱 강제종료 됨)
        NotificationCompat.Builder builder;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "measuring_service_channel";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Measuring Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        builder.setContentTitle("측정시작됨")
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }

    private void initSocket(){
        SocketConnect app = (SocketConnect) getApplicationContext();
        mSocket = app.getSocket();
        mSocket.connect();
    }

    private void initSensors(){
        manager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mHeartRate = manager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        manager.registerListener(this, mHeartRate, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister() throws JSONException { // unregister listener
        JSONObject data = new JSONObject();
        data.put("stop","");
        mSocket.emit("stop",data);
        mSocket.close();
        manager.unregisterListener(this);
    }

}
