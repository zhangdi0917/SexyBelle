package com.jesson.sexybelle;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.jesson.android.Jess;
import com.jesson.android.internet.InternetUtils;
import com.jesson.android.utils.DeviceInfo;
import com.jesson.android.widget.Toaster;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by zhangdi on 14-3-4.
 */
public class BelleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Jess.init(this);
        Jess.DEBUG = AppConfig.DEBUG;

        initSeriesModel();

        initImageLoader();

        registerInternetError();
    }

    private void initSeriesModel() {
        String channel = getMetaData("UMENG_CHANNEL");
        if (channel != null && "google".equals(channel)) {
            AppConfig.SERIES_MODE = 1;
        } else {
            AppConfig.SERIES_MODE = 2;
        }
    }

    private String getMetaData(String key) {
        try {
            Bundle metaData = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
            return metaData.getString(key);
        } catch (Exception e) {

        }
        return null;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(8)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(DeviceInfo.MEM_SIZE / 16 * 1024 * 1024)
                .discCacheSize(50 * 1024 * 1024)
                .discCacheFileCount(100)
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .cacheInMemory(true)
                        .cacheOnDisc(true)
                        .considerExifParams(true)
                        .build());
        if (AppConfig.DEBUG) {
            builder.writeDebugLogs();
        }
        ImageLoader.getInstance().init(builder.build());
    }

    private void registerInternetError() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(InternetUtils.ACTION_INTERNET_ERROR);
        filter.addAction(InternetUtils.ACTION_INTERNET_ERROR_LOCAL);
        lbm.registerReceiver(mInternetBRC, filter);
    }

    private BroadcastReceiver mInternetBRC = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            if (InternetUtils.ACTION_INTERNET_ERROR.equals(intent.getAction())) {
                Toaster.show(context, R.string.api_server_error);
            } else if (InternetUtils.ACTION_INTERNET_ERROR_LOCAL.equals(intent.getAction())) {
                String msg = intent.getStringExtra("msg");
                Toaster.show(context, msg);
            }
        }
    };

}
