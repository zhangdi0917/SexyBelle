package com.jesson.sexybelle.api.belle;

import com.jesson.android.internet.core.ResponseBase;
import com.jesson.android.internet.core.json.JsonProperty;

import java.util.List;

/**
 * Created by zhangdi on 14-3-7.
 */
public class GetBelleListResponse extends ResponseBase {

    @JsonProperty("belles")
    public List<Belle> belles;

    @JsonProperty("hasMore")
    public boolean hasMore;

}
