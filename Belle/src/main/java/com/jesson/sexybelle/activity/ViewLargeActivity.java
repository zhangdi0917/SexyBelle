package com.jesson.sexybelle.activity;

import android.app.ActionBar;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jesson.android.widget.Toaster;
import com.jesson.sexybelle.AppConfig;
import com.jesson.sexybelle.R;
import com.jesson.sexybelle.adapter.ViewLargeAdapter;
import com.jesson.sexybelle.helper.CollectHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qq.e.ads.AdRequest;
import com.qq.e.ads.AdSize;
import com.qq.e.ads.AdView;
import com.qq.e.ads.InterstitialAd;
import com.qq.e.ads.InterstitialAdListener;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewLargeActivity extends BaseActivity {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_PHOTO_URI_LIST = "photo_uri_list";
    private static final String EXTRA_POSITION = "position";

    private TextView mPaginationTv;

    private ViewPager mViewPager;

    private ViewLargeAdapter mPagerAdapter;

    private ArrayList<String> mPhotoUriList;
    private String mTitle;
    private int mPosition;
    private int mSwitchCount = 0;

    private CollectHelper mCollectHelper;

    private InterstitialAd iad;

    private static final int WHAT_SAVE_SUCCESS = 1000;
    private static final int WHAT_SAVE_FAIL = 2000;
    private static final int WHAR_WALLPAPER_SUCCESS = 3000;
    private static final int WHAR_WALLPAPER_FAIL = 4000;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_SAVE_SUCCESS:
                    Toaster.show(ViewLargeActivity.this, R.string.save_gallery_success);
                    MobclickAgent.onEvent(ViewLargeActivity.this, "SaveGallery", "success");
                    break;
                case WHAT_SAVE_FAIL:
                    Toaster.show(ViewLargeActivity.this, R.string.save_gallery_fail);
                    MobclickAgent.onEvent(ViewLargeActivity.this, "SaveGallery", "fail");
                    break;
                case WHAR_WALLPAPER_SUCCESS:
                    Toaster.show(ViewLargeActivity.this, R.string.set_wallpaper_success);
                    MobclickAgent.onEvent(ViewLargeActivity.this, "Wallpaper", "success");
                    break;
                case WHAR_WALLPAPER_FAIL:
                    Toaster.show(ViewLargeActivity.this, R.string.set_wallpaper_success);
                    MobclickAgent.onEvent(ViewLargeActivity.this, "Wallpaper", "fail");
                    break;
            }
        }
    };

    public static void startViewLarge(Context context, String title, ArrayList<String> uriList, int position) {
        Intent intent = new Intent(context, ViewLargeActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putStringArrayListExtra(EXTRA_PHOTO_URI_LIST, uriList);
        intent.putExtra(EXTRA_POSITION, position);
        context.startActivity(intent);

        MobclickAgent.onEvent(context, "ViewLarge", title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        getActionBar().hide();
        setContentView(R.layout.activity_view_large);
        initBanner();

        if (savedInstanceState != null) {
            mPhotoUriList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_URI_LIST);
            mTitle = savedInstanceState.getString(EXTRA_TITLE);
            mPosition = savedInstanceState.getInt(EXTRA_POSITION);
        } else {
            mPhotoUriList = getIntent().getStringArrayListExtra(EXTRA_PHOTO_URI_LIST);
            mTitle = getIntent().getStringExtra(EXTRA_TITLE);
            mPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        }

        getActionBar().setTitle(mTitle);

        mPaginationTv = (TextView) findViewById(R.id.pagination);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new ViewLargeAdapter(this, mPhotoUriList);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPosition = position;
                mPaginationTv.setText((position + 1) + "/" + mPagerAdapter.getCount());

                if (++mSwitchCount % 10 == 0) {
                    iad.loadAd();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                ActionBar actionBar = getActionBar();
                if (actionBar.isShowing()) {
                    actionBar.hide();
                }
            }
        });

        mViewPager.setCurrentItem(mPosition);
        mPaginationTv.setText((mPosition + 1) + "/" + mPagerAdapter.getCount());

        mCollectHelper = new CollectHelper(this);

        iad = new InterstitialAd(this, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_INTERSTITIAL_POSID);
        iad.setAdListener(new InterstitialAdListener() {
            @Override
            public void onFail() {

            }

            @Override
            public void onBack() {

            }

            @Override
            public void onAdReceive() {
                iad.closePopupWindow();
                iad.show();
            }
        });

    }

    private void initBanner() {
        RelativeLayout l = (RelativeLayout) findViewById(R.id.ad_content);
        AdView adv = new AdView(this, AdSize.BANNER, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_BANNER_POSID);
        l.addView(adv);
        AdRequest adr = new AdRequest();
        adr.setTestAd(AppConfig.DEBUG);
        adr.setRefresh(31);
        adv.fetchAd(adr);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(EXTRA_PHOTO_URI_LIST, mPhotoUriList);
        outState.putString(EXTRA_TITLE, mTitle);
        outState.putInt(EXTRA_POSITION, mPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_large, menu);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_collect);
        String url = mPhotoUriList.get(mPosition);
        if (mCollectHelper.isCollected(url)) {
            item.setTitle(R.string.action_cancel_collect);
        } else {
            item.setTitle(R.string.action_collect);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                saveToGallery();
                return true;
            case R.id.action_wallpaper:
                setWallpaper();
                return true;
            case R.id.action_collect:
                String url = getCurrentUrl();
                HashMap<String, String> map = new HashMap<String, String>();
                if (mCollectHelper.isCollected(url)) {
                    MobclickAgent.onEvent(ViewLargeActivity.this, "Collect", "collect");
                    mCollectHelper.cancelCollectBelle(url);
                    Toaster.show(this, R.string.cancel_collect_success);
                } else {
                    MobclickAgent.onEvent(ViewLargeActivity.this, "Collect", "cancel collect");
                    mCollectHelper.collectBelle(url);
                    Toaster.show(this, R.string.collect_success);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getCurrentUrl() {
        if (mPosition >= 0 && mPosition < mPhotoUriList.size()) {
            return mPhotoUriList.get(mPosition);
        }
        return null;
    }

    private void saveToGallery() {
        new Thread() {
            @Override
            public void run() {
                String url = mPhotoUriList.get(mPosition);
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisc(true)
                        .build();
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(url, options);

                try {
                    File cacheDir = getExternalCacheDir();
                    File saveFile = new File(cacheDir, "tmp.jpg");
                    FileOutputStream fos = new FileOutputStream(saveFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (saveFile.exists()) {
                        ContentResolver cr = getContentResolver();
                        String uri = MediaStore.Images.Media.insertImage(cr, saveFile.getAbsolutePath(), "", "");

                        String data = null;
                        String[] projection = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(Uri.parse(uri), projection, null, null, null);
                        if (cursor != null) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            data = cursor.getString(column_index);
                            cursor.close();
                        }
                        if (data != null) {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(data))));
                            saveFile.delete();
                            mHandler.sendEmptyMessage(WHAT_SAVE_SUCCESS);
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(WHAT_SAVE_FAIL);
            }
        }.start();
    }

    private void setWallpaper() {
        new Thread() {
            @Override
            public void run() {
                String url = mPhotoUriList.get(mPosition);
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisc(true)
                        .build();
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(url, options);

                WallpaperManager wallpaperManager = WallpaperManager.getInstance(ViewLargeActivity.this);
                try {
                    wallpaperManager.setBitmap(bitmap);
                    mHandler.sendEmptyMessage(WHAR_WALLPAPER_SUCCESS);
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(WHAR_WALLPAPER_FAIL);
                }
            }
        }.start();

    }

}
