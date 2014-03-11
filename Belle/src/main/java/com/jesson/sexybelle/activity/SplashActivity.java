package com.jesson.sexybelle.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.jesson.android.utils.Logger;
import com.jesson.sexybelle.AppConfig;
import com.jesson.sexybelle.R;
import com.qq.e.splash.SplashAd;
import com.qq.e.splash.SplashAdListener;

/**
 * Created by zhangdi on 14-3-8.
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private long mStartTime = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mStartTime = System.currentTimeMillis();

        FrameLayout container = (FrameLayout) findViewById(R.id.splash_container);
        new SplashAd(this, container, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_SPLASH_POSID,
                new SplashAdListener() {
                    @Override
                    public void onAdPresent() {
                        Logger.i(TAG, "splash ad present");
                    }

                    @Override
                    public void onAdFailed(int arg0) {
                        Logger.i(TAG, "splash ad fail " + arg0);
                        enterMain();
                    }

                    @Override
                    public void onAdDismissed() {
                        Logger.i(TAG, "splash ad dismiss");
                        enterMain();
                    }
                }
        );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void enterMain() {
        long endTime = System.currentTimeMillis();
        long delay = 500 - (endTime - mStartTime);
        if (delay <= 0) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, delay);
        }
    }
}
