package com.asanwatch.measure_sk;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.asanwatch.measure_sk.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static TextView measureText;
    private static TextView networkText;
    private Button startButton;
    private ActivityMainBinding binding;
    private Intent backgroundService;
    public static final int PERMISSIONS_REQUEST = 1; // 권한 요청
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        doCheckPermission();
        MainActivity.context = getApplicationContext();


        backgroundService = new Intent(getApplicationContext(), MeasureClass.class);

        measureText = binding.measureStatusText;
        networkText = binding.networkStatusText;
        startButton = binding.startButton;


        startButton.setOnClickListener(v -> {
            if(!isServiceRunning(MeasureClass.class)){
                Log.d(TAG, "Service started.");
                startButton.setText("측정종료");
                startForegroundService(backgroundService);

            } else {
//                MessageSend request = new MessageSend(SharedObjects.socket, "device=" + SharedObjects.deviceId + "&tag=stop");
//                request.execute();
                Log.d(TAG, "Service stopped.");
                startButton.setText("측정시작");
                stopService(backgroundService);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedObjects.isWake = true;
        boolean isNetworkOK = pingTest();
        setNetworkTextWithPing(isNetworkOK);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedObjects.isWake = false;
    }

    private boolean pingTest(){
        String ping = "ping -c 1 -W 10 142.250.204.68";
        Process proc = null;
        Runtime runTime = Runtime.getRuntime();

        try {
            proc = runTime.exec(ping);
            proc.waitFor();
        } catch(Exception ie){
            Log.d("runtime.exec()", ie.getMessage());
        }

        int result = proc.exitValue();

        Log.d(TAG, String.valueOf(result));

        if(result==0){
            return true;
        }else{
            return false;
        }
    }

    public boolean isServiceRunning(Class<?> myClass) {
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo rsi : am.getRunningServices(Integer.MAX_VALUE)) {
            if (myClass.getName().equals(rsi.service.getClassName()))
            return true;
        }
        return false;
    }

    private void setNetworkTextWithPing(boolean result){
        if(result == true){
            SpannableString s = new SpannableString("네트워크 상태 : OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.GREEN), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }else{
            SpannableString s = new SpannableString("네트워크 상태 : NOT OK");
            Spannable span = (Spannable) s;
            span.setSpan(new ForegroundColorSpan(Color.RED), 9, s.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            networkText.setText(span);
        }
    }

    public static void setMeasureText(String s){
        //Log.d(TAG, "Text changed.");
        if(measureText!=null) {
            measureText.setText(s);
        }
    }

    public static void setNetworkText(String s){
        if(networkText!=null) {
            networkText.setText(s);
        }
    }

    public void doCheckPermission() {
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, PERMISSIONS_REQUEST);
        }
    }

    public static Context ApplicationContext(){
        return MainActivity.context;
    }
}