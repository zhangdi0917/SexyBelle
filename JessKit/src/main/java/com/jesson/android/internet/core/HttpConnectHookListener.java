/**
 * HttpHookListener.java
 */
package com.jesson.android.internet.core;

import android.os.Bundle;

/**
 * @author Guoqing Sun Sep 14, 20126:12:07 PM
 */
public interface HttpConnectHookListener {

	void onPreHttpConnect(String baseUrl, String method, Bundle requestParams);

	void onPostHttpConnect(String result, int httpStatus);
	
	void onHttpConnectError(int code, String data, Object obj);
}
