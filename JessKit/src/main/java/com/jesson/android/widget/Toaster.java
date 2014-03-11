package com.jesson.android.widget;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zhangdi on 14-2-12.
 * 管理系统Toast，避免Toast压栈
 */
public class Toaster {

    private static Toast toast;

    public static void show(Context context, String text) {
        if (toast == null) {
            synchronized (Toaster.class) {
                if (toast == null) {
                    toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                }
            }
        } else {
            toast.setText(text);
        }
        toast.show();
    }

    public static void show(Context context, int resId) {
        show(context, context.getString(resId));
    }

}
