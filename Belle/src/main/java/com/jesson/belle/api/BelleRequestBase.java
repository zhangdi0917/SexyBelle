package com.jesson.belle.api;

import android.os.Bundle;
import android.text.TextUtils;

import com.jesson.android.internet.core.NetWorkException;
import com.jesson.android.internet.core.RequestBase;
import com.jesson.android.internet.core.annotations.UseHttps;

/**
 * Created by zhangdi on 14-3-7.
 */
public class BelleRequestBase<T> extends RequestBase<T> {

    public static final String BASE_API_URL = "http://sexybelle.duapp.com/";
    public static final String APP_ID = "29560";

    private static final String KEY_METHOD = "method";

    @Override
    public Bundle getParams() throws NetWorkException {
        Bundle params = super.getParams();

        String method = params.getString(KEY_METHOD);
        if (TextUtils.isEmpty(method)) {
            throw new RuntimeException("Method Name MUST NOT be NULL");
        }
        if (!method.startsWith("http://")) {    //method可填为 http://url/xxx?a=1&b=2 或  feed.gets
            method = BASE_API_URL + method.replace('.', '/');
        }
        Class<?> c = this.getClass();
        if (c.isAnnotationPresent(UseHttps.class)) {
            method = method.replace("http", "https");
            method = method.replaceAll(":(\\d+)/", "/");
        }
        params.putString(KEY_METHOD, method);

        params.putString("appid", APP_ID);

        return params;
    }
}
