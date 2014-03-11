package com.jesson.sexybelle.api.series;

import com.jesson.android.internet.core.annotations.RestMethodUrl;
import com.jesson.sexybelle.api.BelleRequestBase;
import com.jesson.sexybelle.api.belle.GetBelleListResponse;

/**
 * Created by zhangdi on 14-3-11.
 */

@RestMethodUrl("series/list")
public class GetSeriesListRequest extends BelleRequestBase<GetSeriesListResponse> {

}
