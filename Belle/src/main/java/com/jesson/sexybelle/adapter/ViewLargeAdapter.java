package com.jesson.sexybelle.adapter;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jesson.sexybelle.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zhangdi on 14-3-8.
 */
public class ViewLargeAdapter extends PagerAdapter {

    private Activity mActivity;
    private List<String> mUrlList;

    private DisplayImageOptions mOptions;

    public ViewLargeAdapter(Activity activity, List<String> urls) {
        this.mActivity = activity;
        this.mUrlList = urls;

        mOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public int getCount() {
        return mUrlList != null ? mUrlList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View retView = LayoutInflater.from(mActivity).inflate(R.layout.item_view_large, null);
        PhotoView photoView = (PhotoView) retView.findViewById(R.id.photo_view);
        final ProgressBar progressBar = (ProgressBar) retView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        String uri = mUrlList.get(position);
        ImageLoader.getInstance().displayImage(uri, photoView, mOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });

        photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                ActionBar actionBar = mActivity.getActionBar();
                if (actionBar.isShowing()) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
            }
        });

        container.addView(retView);
        return retView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object != null) {
            View view = (View) object;
            container.removeView(view);
        }
    }

}
