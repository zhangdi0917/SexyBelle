package com.jesson.belle.api.belle;

import com.jesson.android.internet.core.ResponseBase;
import com.jesson.android.internet.core.json.JsonProperty;

import java.util.List;

/**
 * Created by zhangdi on 14-3-8.
 */
public class RandomGetBelleListResponse extends ResponseBase {

    @JsonProperty("belles")
    public List<Belle> belles;
    
}
