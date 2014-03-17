package com.jesson.belle.api.series;

import com.jesson.android.internet.core.ResponseBase;
import com.jesson.android.internet.core.json.JsonProperty;

import java.util.List;

/**
 * Created by zhangdi on 14-3-11.
 */
public class GetSeriesListResponse extends ResponseBase {

    @JsonProperty("seriesList")
    public List<Series> seriesList;
    
}
