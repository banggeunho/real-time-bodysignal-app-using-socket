package com.asanwatch.measure_sk;
import java.io.IOException;
import java.net.Socket;


public class SharedObjects {
    public static String deviceId = DeviceInfoUtil.getDeviceBrand()+" "+DeviceInfoUtil.getDeviceId(MainActivity.ApplicationContext());
    public static boolean isWake = true;
}
