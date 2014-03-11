package com.jesson.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class DeviceInfo {

    public static int DENSITY_DPI;

    public static float DENSITY;

    public static int SCREEN_WIDTH;

    public static int SCREEN_HEIGHT;

    public static int MEM_SIZE;

    public static String IMEI;

    public static String IMSI;

    public static String MAC_ADDRESS;

    public static String PHONE_NUMBER;

    public static void init(Context context) {
        DENSITY_DPI = context.getResources().getDisplayMetrics().densityDpi;
        DENSITY = context.getResources().getDisplayMetrics().density;
        SCREEN_WIDTH = context.getResources().getDisplayMetrics().widthPixels;
        SCREEN_HEIGHT = context.getResources().getDisplayMetrics().heightPixels;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MEM_SIZE = am.getMemoryClass();

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
        IMSI = telephonyManager.getSubscriberId();
        PHONE_NUMBER = telephonyManager.getLine1Number();

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            MAC_ADDRESS = info.getMacAddress();
        }
    }

}
