package com.jesson.belle.helper;

import android.content.Context;

import com.jesson.android.internet.InternetUtils;
import com.jesson.android.internet.core.NetWorkException;
import com.jesson.belle.AppConfig;
import com.jesson.belle.api.series.GetSeriesListRequest;
import com.jesson.belle.api.series.GetSeriesListResponse;
import com.jesson.belle.dao.model.DaoSession;
import com.jesson.belle.dao.model.Series;
import com.jesson.belle.dao.model.SeriesDao;
import com.jesson.belle.dao.utils.DaoUtils;
import com.jesson.belle.event.SeriesUpdatedEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by zhangdi on 14-3-11.
 */
public class SeriesHelper {

    private static SeriesHelper mInstance = new SeriesHelper();

    private List<Series> mSeriesList = new ArrayList<Series>();

    private SeriesHelper() {

    }

    public void syncSeries(final Context context) {
        DaoSession session = DaoUtils.getDaoSession(context);
        final SeriesDao dao = session.getSeriesDao();
        // load from local database
        mSeriesList = dao.loadAll();
        if (mSeriesList == null || mSeriesList.size() == 0) {
            // 加载默认
            mSeriesList = defaultSeries();
            dao.insertInTx(mSeriesList);
        }
        mSeriesList.addAll(localSeries());

        new Thread() {
            @Override
            public void run() {
                // load from server
                GetSeriesListRequest request = new GetSeriesListRequest();
                try {
                    GetSeriesListResponse response = InternetUtils.request(context, request);
                    if (response != null && response.seriesList != null && response.seriesList.size() > 0) {
                        List<Series> list = new ArrayList<Series>();
                        for (com.jesson.belle.api.series.Series s : response.seriesList) {
                            Series series = new Series(s.type, s.title);
                            list.add(series);
                        }
                        // delete old
                        dao.deleteAll();
                        mSeriesList.clear();
                        // insert new
                        dao.insertInTx(list);
                        mSeriesList.addAll(list);
                        mSeriesList.addAll(localSeries());
                        // post event
                        EventBus.getDefault().post(new SeriesUpdatedEvent());
                    }
                } catch (NetWorkException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private List<Series> defaultSeries() {
        List<Series> list = new ArrayList<Series>();
        if (AppConfig.SERIES_MODE == 1) {
            Series series1 = new Series(1, "性感美女");
            list.add(series1);
            Series series2 = new Series(2, "岛国女友");
            list.add(series2);
            Series series3 = new Series(3, "丝袜美腿");
            list.add(series3);
            Series series4 = new Series(4, "有沟必火");
            list.add(series4);
            Series series5 = new Series(5, "有沟必火");
            list.add(series5);
            Series series11 = new Series(11, "明星美女");
            list.add(series11);
            Series series12 = new Series(12, "甜素纯");
            list.add(series12);
            Series series13 = new Series(13, "校花");
            list.add(series13);
        } else {
            Series series11 = new Series(11, "明星美女");
            list.add(series11);
            Series series12 = new Series(12, "甜素纯");
            list.add(series12);
            Series series13 = new Series(13, "古典美女");
            list.add(series13);
            Series series14 = new Series(14, "校花");
            list.add(series14);
            Series series1 = new Series(1, "性感美女");
            list.add(series1);
        }
        return list;
    }

    private List<Series> localSeries() {
        List<Series> list = new ArrayList<Series>();
        Series collect = new Series(-1, "我的收藏");
        list.add(collect);
        if (AppConfig.SERIES_MODE == 2) {
            Series suggest = new Series(-2, "推荐应用");
            list.add(suggest);
        }
        return list;
    }

    public static SeriesHelper getInstance() {
        return mInstance;
    }

    public List<Series> getSeriesList() {
        return mSeriesList;
    }
}
