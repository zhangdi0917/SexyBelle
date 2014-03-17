package com.jesson.belle.helper;

import android.content.Context;

import com.jesson.android.internet.InternetUtils;
import com.jesson.android.internet.core.NetWorkException;
import com.jesson.belle.api.belle.Belle;
import com.jesson.belle.api.belle.GetBelleListRequest;
import com.jesson.belle.api.belle.GetBelleListResponse;
import com.jesson.belle.api.belle.RandomGetBelleListRequest;
import com.jesson.belle.api.belle.RandomGetBelleListResponse;
import com.jesson.belle.dao.model.DaoSession;
import com.jesson.belle.dao.model.LocalBelle;
import com.jesson.belle.dao.model.LocalBelleDao;
import com.jesson.belle.dao.utils.DaoUtils;
import com.jesson.belle.event.GetBelleListEvent;
import com.jesson.belle.event.NetworkErrorEvent;
import com.jesson.belle.event.ServerErrorEvent;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by zhangdi on 14-3-7.
 */
public class BelleHelper {

    private Context mContext;

    private DaoSession mSession;

    public BelleHelper(Context context) {
        mContext = context.getApplicationContext();
        mSession = DaoUtils.getDaoSession(mContext);
    }

    public void getBelleListFromServer(final int type, final int id, final int count) {
        new Thread() {
            @Override
            public void run() {
                try {
                    GetBelleListRequest request = new GetBelleListRequest();
                    request.type = type;
                    request.id = id;
                    request.count = count;
                    GetBelleListResponse response = InternetUtils.request(mContext, request);
                    if (response != null) {
                        GetBelleListEvent event = new GetBelleListEvent();
                        event.belles = response.belles;
                        event.hasMore = response.hasMore;
                        if (id <= 0) {
                            event.type = GetBelleListEvent.TYPE_SERVER;
                            // update local database
                            LocalBelleDao dao = mSession.getLocalBelleDao();
                            // delete old
                            dao.queryBuilder().where(LocalBelleDao.Properties.Type.eq(type)).buildDelete().forCurrentThread().executeDeleteWithoutDetachingEntities();
                            // insert new
                            if (response.belles != null) {
                                List<LocalBelle> localBelles = new ArrayList<LocalBelle>();
                                for (Belle belle : response.belles) {
                                    LocalBelle localBelle = new LocalBelle(belle.id, belle.time, belle.type, belle.url);
                                    localBelles.add(localBelle);
                                }
                                dao.insertOrReplaceInTx(localBelles);
                            }
                        } else {
                            event.type = GetBelleListEvent.TYPE_SERVER_MORE;
                        }
                        EventBus.getDefault().post(event);
                    } else {
                        EventBus.getDefault().post(new ServerErrorEvent());
                    }
                } catch (NetWorkException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new NetworkErrorEvent(e));
                }
            }
        }.start();
    }

    /**
     * 随机从服务器获取
     *
     * @param type
     * @param count
     */
    public void randomGetBelleListFromServer(final int type, final int count) {
        new Thread() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();

                    RandomGetBelleListRequest request = new RandomGetBelleListRequest();
                    request.type = type;
                    request.count = count;
                    RandomGetBelleListResponse response = InternetUtils.request(mContext, request);
                    if (response != null) {
                        GetBelleListEvent event = new GetBelleListEvent();
                        event.belles = response.belles;
                        event.type = GetBelleListEvent.TYPE_SERVER_RANDOM;
                        // update local database
                        LocalBelleDao dao = mSession.getLocalBelleDao();
                        // delete old
                        dao.queryBuilder().where(LocalBelleDao.Properties.Type.eq(type)).buildDelete().forCurrentThread().executeDeleteWithoutDetachingEntities();
                        // insert new
                        if (response.belles != null) {
                            List<LocalBelle> localBelles = new ArrayList<LocalBelle>();
                            for (Belle belle : response.belles) {
                                LocalBelle localBelle = new LocalBelle(belle.id, belle.time, belle.type, belle.url);
                                localBelles.add(localBelle);
                            }
                            dao.insertOrReplaceInTx(localBelles);
                        }

                        long endTime = System.currentTimeMillis();
                        long delay = 1000 - (endTime - startTime);
                        if (delay > 0) {
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        EventBus.getDefault().post(event);
                    } else {
                        EventBus.getDefault().post(new ServerErrorEvent());
                    }
                } catch (NetWorkException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new NetworkErrorEvent(e));
                }
            }
        }.start();

    }

    public void getBelleListFromLocal(final int type) {
        new Thread() {
            @Override
            public void run() {
                LocalBelleDao dao = mSession.getLocalBelleDao();
                List<LocalBelle> localList = dao.queryBuilder().where(LocalBelleDao.Properties.Type.eq(type))
                        .build().forCurrentThread().list();
                List<Belle> belles = null;
                if (localList != null) {
                    belles = new ArrayList<Belle>();
                    for (LocalBelle localBelle : localList) {
                        Belle belle = new Belle(localBelle.getId(), localBelle.getTime(), localBelle.getType(), localBelle.getUrl());
                        belles.add(belle);
                    }
                }
                GetBelleListEvent event = new GetBelleListEvent();
                event.type = GetBelleListEvent.TYPE_LOCAL;
                event.belles = belles;
                event.hasMore = false;
                EventBus.getDefault().post(event);
            }
        }.start();
    }

}
