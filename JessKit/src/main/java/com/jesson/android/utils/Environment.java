package com.jesson.android.utils;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by zhangdi on 14-2-12.
 */
public class Environment {

    /**
     * 判断小时是否为24小时制
     */
    public static boolean isHourto24(Context context) {
        ContentResolver cr = context.getContentResolver();
        String strFormatTime = android.provider.Settings.System.getString(cr, android.provider.Settings.System.TIME_12_24);
        if (null != strFormatTime && strFormatTime.equals("24")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断网络连接
     *
     * @param context
     * @return
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = (cm != null) ? cm.getActiveNetworkInfo() : null;
        if (info != null && info.isAvailable() && info.isConnected()) {
            return true;
        }
        return false;
    }

    public static String getCurrentStackMethodName() {
        String method = "";
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        String invokeMethodName = ste.getMethodName();
        String fileName = ste.getFileName();
        long line = ste.getLineNumber();
        if (!TextUtils.isEmpty(invokeMethodName)) {
            method = fileName + "::" + invokeMethodName + "::" + line;
        }
        return method;
    }

    /**
     * 获取包名
     *
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * 获取版本
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0.0";
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前进程名
     *
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess == null) {
                continue;
            }
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
