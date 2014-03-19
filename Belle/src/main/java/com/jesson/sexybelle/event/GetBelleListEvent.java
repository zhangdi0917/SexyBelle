package com.jesson.sexybelle.event;

import com.jesson.sexybelle.api.belle.Belle;

import java.util.List;

/**
 * Created by zhangdi on 14-3-7.
 */
public class GetBelleListEvent {

    public static final int TYPE_LOCAL = 1;
    public static final int TYPE_SERVER = 2;
    public static final int TYPE_SERVER_MORE = 3;
    public static final int TYPE_SERVER_RANDOM = 4;

    public int type = TYPE_LOCAL;

    public boolean hasMore;

    public List<Belle> belles;

    @Override
    public String toString() {
        return "GetBelleListEvent{" +
                "type=" + type +
                ", hasMore=" + hasMore +
                ", belles=" + belles +
                '}';
    }

}
