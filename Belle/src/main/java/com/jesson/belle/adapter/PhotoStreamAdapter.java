package com.jesson.belle.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jesson.belle.R;
import com.jesson.belle.api.belle.Belle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.List;

/**
 * Created by zhangdi on 14-3-4.
 */
public class PhotoStreamAdapter extends BaseAdapter {

    private Context mContext;

    private List<Belle> mBelles;

    public PhotoStreamAdapter(Context context, List<Belle> belles) {
        mContext = context;
        mBelles = belles;
    }

    @Override
    public int getCount() {
        return mBelles == null ? 0 : mBelles.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_photo_stream, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String photoUri = mBelles.get(i).url;

        holder.progressBar.setVisibility(View.GONE);

        ImageLoader.getInstance().displayImage(photoUri, holder.photo, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.progressBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private static final class ViewHolder {

        public ImageView photo;

        public ProgressBar progressBar;

        public ViewHolder(View rootView) {
            photo = (ImageView) rootView.findViewById(R.id.photo);
            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        }

    }
}
