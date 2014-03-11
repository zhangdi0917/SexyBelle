package com.jesson.android.internet.core;

import org.apache.http.HttpEntity;

public interface HttpClientInterface {

    public <T> T getResource(Class<T> resourceType, String url, String method, HttpEntity entity)
            throws NetWorkException;
    
    public <T, V> V getResource(Class<T> inputResourceType, Class<V> retResourceType, String url, String method, HttpEntity entity)
            throws NetWorkException;

    public boolean isNetworkAvailable();

    public void setHttpReturnListener(HttpRequestHookListener l);
}
