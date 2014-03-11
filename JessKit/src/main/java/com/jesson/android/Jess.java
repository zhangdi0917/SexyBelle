package com.jesson.android;

import android.content.Context;

import com.jesson.android.utils.DeviceInfo;
import com.jesson.android.utils.Environment;
import com.jesson.android.utils.Logger;

/**
 * Created by zhangdi on 14-3-4.
 */
public class Jess {

    public static boolean DEBUG = true;

    public static void init(Context context) {
        DeviceInfo.init(context);
    }

    public static void LOGD(String msg) {
        String tag = Environment.getCurrentStackMethodName();
        Logger.d(tag, msg);
    }

}
