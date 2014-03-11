package com.jesson.android.internet.core.impl;

import android.content.Context;

import com.jesson.android.internet.core.HttpClientInterface;

public class HttpClientFactory {

	public static HttpClientInterface createHttpClientInterface(Context context) {
		return HttpClientImpl.getInstance(context);
	}

}
