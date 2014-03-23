package com.jesson.belle.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.jesson.belle.AppConfig;
import com.jesson.belle.R;
import com.jesson.belle.dao.model.Series;
import com.jesson.belle.fragment.NavigationDrawerFragment;
import com.jesson.belle.fragment.PhotoStreamFragment;
import com.jesson.belle.helper.SeriesHelper;
import com.qq.e.ads.AdRequest;
import com.qq.e.ads.AdSize;
import com.qq.e.ads.AdView;
import com.qq.e.ads.InterstitialAd;
import com.qq.e.ads.InterstitialAdListener;
import com.qq.e.appwall.GdtAppwall;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int mRefreshTime = 0;

    private Series mSeries;

    private InterstitialAd iad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBannerAd();
        initInterstitialAd();
        initAppWall();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobclickAgent.flush(getApplication());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        List<Series> seriesList = SeriesHelper.getInstance().getSeriesList();
        if (position < 0 || position > seriesList.size() - 1) {
            return;
        }
        mSeries = seriesList.get(position);
        if (mSeries.getType() == -2) {
            GdtAppwall.showAppwall();
        } else {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getFragmentManager();
            String tag = String.valueOf(mSeries.getType());
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment == null) {
                fragment = PhotoStreamFragment.newInstance(mSeries);
            }
            fragmentManager.beginTransaction().replace(R.id.container, fragment, tag).commit();
        }
    }

    public void onSectionAttached(Series series) {
        mTitle = series.getTitle();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem item = menu.findItem(R.id.action_refresh);
            if (mSeries != null && mSeries.getType() < 0) {
                item.setVisible(false);
            } else {
                item.setVisible(true);
            }
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_feedback) {
            FeedbackAgent agent = new FeedbackAgent(this);
            agent.startFeedbackActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBannerAd() {
        RelativeLayout l = (RelativeLayout) findViewById(R.id.ad_content);
        AdView adv = new AdView(this, AdSize.BANNER, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_BANNER_POSID);
        l.addView(adv);
        AdRequest adr = new AdRequest();
        adr.setTestAd(AppConfig.DEBUG);
        adr.setRefresh(31);
        adv.fetchAd(adr);
    }

    private void initInterstitialAd() {
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
                iad.show();
            }
        });
    }

    private void initAppWall() {
        GdtAppwall.init(this, AppConfig.GDT_AD_APPID, AppConfig.GDT_AD_APPWALL_POSID, AppConfig.DEBUG);
    }

    public void onRefresh() {
        mRefreshTime += 1;
        if (mRefreshTime % 5 == 0) {
            if (iad != null) {
                iad.loadAd();
            }
        }

        if (mSeries != null) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("type", String.valueOf(mSeries.getType()));
            map.put("title", mSeries.getTitle());
            map.put("mode", String.valueOf(AppConfig.SERIES_MODE));
            MobclickAgent.onEvent(this, "Refresh", map);
        }
    }

}