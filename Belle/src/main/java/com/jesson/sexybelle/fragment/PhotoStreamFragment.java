package com.jesson.sexybelle.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jesson.sexybelle.R;
import com.jesson.sexybelle.activity.GalleryActivity;
import com.jesson.sexybelle.activity.MainActivity;
import com.jesson.sexybelle.adapter.PhotoStreamAdapter;
import com.jesson.sexybelle.api.belle.Belle;
import com.jesson.sexybelle.dao.model.CollectedBelle;
import com.jesson.sexybelle.dao.model.Series;
import com.jesson.sexybelle.event.GetBelleListEvent;
import com.jesson.sexybelle.event.NetworkErrorEvent;
import com.jesson.sexybelle.event.ServerErrorEvent;
import com.jesson.sexybelle.helper.BelleHelper;
import com.jesson.sexybelle.helper.CollectHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by zhangdi on 14-3-4.
 */
public class PhotoStreamFragment extends Fragment implements OnRefreshListener {

    private static final String ARG_SERIES = "series";

    private PullToRefreshLayout mPullToRefreshLayout;

    private GridView mGridView;

    private PhotoStreamAdapter mPhotoStreamAdapter;

    private List<Belle> mBelles = new ArrayList<Belle>();

    private Series mSeries;

    private BelleHelper mBelleHelper;

    private static final int PAGE_COUNT = 50;

    public static PhotoStreamFragment newInstance(Series series) {
        PhotoStreamFragment fragment = new PhotoStreamFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SERIES, series);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoStreamFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSeries = (Series) getArguments().getSerializable(ARG_SERIES);
        ((MainActivity) activity).onSectionAttached(mSeries);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mBelleHelper = new BelleHelper(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo_stream, container, false);

        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.SetupWizard setupWizard = ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this);

        if (mSeries.getType() >= 0) {
            setupWizard.allChildrenArePullable();
        } else if (mSeries.getType() == -1) {
            setupWizard.theseChildrenArePullable(0);
            List<Belle> list = loadCollectedBelles();
            if (list != null) {
                mBelles.addAll(list);
            }
        }

        setupWizard.setup(mPullToRefreshLayout);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view);
        mPhotoStreamAdapter = new PhotoStreamAdapter(getActivity(), mBelles);
        mGridView.setAdapter(mPhotoStreamAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<String> uriList = new ArrayList<String>();
                for (Belle belle : mBelles) {
                    uriList.add(belle.url);
                }
                GalleryActivity.startViewLarge(getActivity(), mSeries.getTitle(), uriList, i);
            }
        });

        if (mSeries.getType() >= 0) {
            mBelleHelper.getBelleListFromLocal(mSeries.getType());
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ImageLoader.getInstance().stop();
    }

    @Override
    public void onRefreshStarted(View view) {
        mBelleHelper.randomGetBelleListFromServer(mSeries.getType(), PAGE_COUNT);
        ((MainActivity) getActivity()).onRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            if (!mPullToRefreshLayout.isRefreshing()) {
                mPullToRefreshLayout.setRefreshing(true);
                mBelleHelper.randomGetBelleListFromServer(mSeries.getType(), PAGE_COUNT);
                ((MainActivity) getActivity()).onRefresh();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEventMainThread(GetBelleListEvent event) {
        if (event.type == GetBelleListEvent.TYPE_SERVER_RANDOM) {
            mBelles.clear();
            if (event.belles != null) {
                mBelles.addAll(event.belles);
            }
            mPhotoStreamAdapter = new PhotoStreamAdapter(getActivity(), mBelles);
            mGridView.setAdapter(mPhotoStreamAdapter);
            mPullToRefreshLayout.setRefreshComplete();
        } else if (event.type == GetBelleListEvent.TYPE_LOCAL) {
            mBelles.clear();
            if (event.belles != null) {
                mBelles.addAll(event.belles);
            }
            mPhotoStreamAdapter.notifyDataSetChanged();
            // load from server
            if (mBelles.size() == 0) {
                if (!mPullToRefreshLayout.isRefreshing()) {
                    mPullToRefreshLayout.setRefreshing(true);
                    mBelleHelper.randomGetBelleListFromServer(mSeries.getType(), PAGE_COUNT);
                    ((MainActivity) getActivity()).onRefresh();
                }
            }
        }
    }

    public void onEventMainThread(NetworkErrorEvent event) {
        if (mPullToRefreshLayout.isRefreshing()) {
            mPullToRefreshLayout.setRefreshComplete();
        }
    }

    public void onEventMainThread(ServerErrorEvent event) {
        if (mPullToRefreshLayout.isRefreshing()) {
            mPullToRefreshLayout.setRefreshComplete();
        }
    }

    private List<Belle> loadCollectedBelles() {
        List<Belle> belles = new ArrayList<Belle>();
        CollectHelper helper = new CollectHelper(getActivity());
        List<CollectedBelle> collectedBelles = helper.loadAll();
        if (collectedBelles != null) {
            for (CollectedBelle collectedBelle : collectedBelles) {
                Belle belle = new Belle(0, collectedBelle.getTime(), -1, collectedBelle.getUrl());
                belles.add(belle);
            }
        }
        return belles;
    }

}
