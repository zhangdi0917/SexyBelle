package com.jesson.belle.api.series;

import com.jesson.android.internet.core.json.JsonProperty;

import java.io.Serializable;

/**
 * Created by zhangdi on 14-3-11.
 */
public class Series implements Serializable {

    @JsonProperty("type")
    public int type;

    @JsonProperty("title")
    public String title;

    public Series(int type, String title) {
        this.type = type;
        this.title = title;
    }

}
