package com.jesson.sexybelle.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jesson.sexybelle.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by zhangdi on 14-3-9.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private TextView mClearTv;

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mClearTv = (TextView) findViewById(R.id.clear);
        mClearTv.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.clear) {
            dismissDialog();
            mAlertDialog = new AlertDialog.Builder(this).setMessage(R.string.clear_dialog_message)
                    .setPositiveButton(R.string.clear_dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ImageLoader.getInstance().clearDiscCache();
                            ImageLoader.getInstance().clearMemoryCache();
                        }
                    }).setNegativeButton(R.string.clear_dialog_negative, null).create();
            mAlertDialog.show();
        }
    }

    private void dismissDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
        mAlertDialog = null;
    }
}
