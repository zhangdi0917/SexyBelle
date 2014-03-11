package com.jesson.sexybelle.activity;

import android.app.Activity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by zhangdi on 14-3-5.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
