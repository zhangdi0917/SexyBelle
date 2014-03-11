/**
 * Copyright 2011-2012 Renren Inc. All rights reserved.
 * － Powered by Team Pegasus. －
 */

package com.jesson.android.internet.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.jesson.android.internet.core.annotations.HttpMethod;
import com.jesson.android.internet.core.annotations.IgnoreValue;
import com.jesson.android.internet.core.annotations.NeedTicket;
import com.jesson.android.internet.core.annotations.NoNeedTicket;
import com.jesson.android.internet.core.annotations.OptionalParam;
import com.jesson.android.internet.core.annotations.OptionalTicket;
import com.jesson.android.internet.core.annotations.RequiredParam;
import com.jesson.android.internet.core.annotations.RestMethodExtUrlParam;
import com.jesson.android.internet.core.annotations.RestMethodUrl;

/**
 * 
 * All Requests inherit from this MUST add Annotation (either
 * {@link RequiredParam} or {@link OptionalParam}) to their declared fields that
 * should be send to the REST server.
 * 
 * Note : 1.Follow field should not be declared in Requests: api_key call_id sig
 * session_key format 2.REST version is set to "1.0" by default,
 * 
 * @see RequiredParam
 * @see OptionalParam
 * 
 * @param <T>
 */
public abstract class RequestBase<T> {

    // private static final String DEFAULT_REST_VERSION = "1.0";
    // private static final String DEFAULT_FORMAT = "JSON";
    private static final boolean DEBUG = InternetConfig.DEBUG;

    public static final int NO_TICKET = 0;
    public static final int OPTIONAL_TICKET = 1;
    public static final int NEED_TICKET = 2;

    private RequestEntity mRequestEntity;

    private boolean mIgnoreResult;

    private boolean mHandleErrorSelf;

    public boolean canIgnoreResult() {
        return mIgnoreResult;
    }

    public boolean getHandleErrorSelf() {
        return mHandleErrorSelf;
    }

    public void setHandleErrorSelf(boolean self) {
        mHandleErrorSelf = self;
    }

    public void setIgnoreResult(boolean ignore) {
        mIgnoreResult = ignore;
    }

    public int getSessinConfig() {
        Class<?> c = this.getClass();
        if (c.isAnnotationPresent(NoNeedTicket.class)) {
            return NO_TICKET;
        } else if (c.isAnnotationPresent(NeedTicket.class)) {
            return NEED_TICKET;
        } else if (c.isAnnotationPresent(OptionalTicket.class)) {
            return OPTIONAL_TICKET;
        }
        return NEED_TICKET;
    }

    public RequestEntity getRequestEntity() throws NetWorkException {
        if (mRequestEntity != null) {
            return mRequestEntity;
        }
        mRequestEntity = new RequestEntity();
        mRequestEntity.setBasicParams(getParams());
        mRequestEntity.setContentType(RequestEntity.REQUEST_CONTENT_TYPE_TEXT_PLAIN);
        return mRequestEntity;
    }

    public String getMethodUrl() {
        Class<?> c = this.getClass();

        // Method name
        if (c.isAnnotationPresent(RestMethodUrl.class)) {
            RestMethodUrl restMethodName = c.getAnnotation(RestMethodUrl.class);
            return restMethodName.value();
        }

        return null;
    }

    public Bundle getParams() throws NetWorkException {
        Class<?> c = this.getClass();
        Field[] fields = c.getDeclaredFields();
        Bundle params = new Bundle();

        // Method name
        if (c.isAnnotationPresent(RestMethodUrl.class)) {
            RestMethodUrl restMethodName = c.getAnnotation(RestMethodUrl.class);
            String methodName = restMethodName.value();
            params.putString("method", methodName);
        } else {
            throw new RuntimeException("Method Name MUST be annotated!! :" + c.getName());
        }
    
        // Htpp Method name
        String methodString = "POST";
        if (c.isAnnotationPresent(HttpMethod.class)) {
            HttpMethod method = c.getAnnotation(HttpMethod.class);
            methodString = method.value();
        }
        if (TextUtils.isEmpty(methodString)) {
            throw new RuntimeException("Http Method Name Can not be annotated Empty!!");
        } else {
            if (methodString.toUpperCase().equals("GET")) {
                params.putString("httpMethod", "GET");
            } else if (methodString.toUpperCase().equals("POST")) {
                params.putString("httpMethod", "POST");
            } else {
                throw new RuntimeException("Http Method Name Must be annotated POST or GET!!");
            }
        }

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                boolean hasDefault = false;
                long defaultValue = -1;
                if (field.isAnnotationPresent(IgnoreValue.class)) {
                    IgnoreValue value = field.getAnnotation(IgnoreValue.class);
                    if (value != null) {
                        hasDefault = true;
                        defaultValue = value.value();
                    }
                }
                
                if (field.isAnnotationPresent(RequiredParam.class)) {
                    RequiredParam requiredParam = field.getAnnotation(RequiredParam.class);
                    if (requiredParam != null) {
                        String name = requiredParam.value();
                        Object object = field.get(this);
                        if (object == null) {
                            throw new NetWorkException("Param " + name + " MUST NOT be null");
                        }
                        String value = String.valueOf(object);
                        if (TextUtils.isEmpty(value)) {
                            throw new NetWorkException("Param " + name + " MUST NOT be null");
                        }
                        params.putString(name, value);
                    }
                } else if (field.isAnnotationPresent(OptionalParam.class)) {
                    OptionalParam optionalParam = field.getAnnotation(OptionalParam.class);
                    if (optionalParam != null) {
                        String name = optionalParam.value();
                        Object object = field.get(this);
                        if (object != null) {
                            if (hasDefault) {
                                if (object instanceof Long) {
                                    long value = (Long)object;
                                    if (value != defaultValue) {
                                        params.putString(name, String.valueOf(value));
                                    }
                                } else if (object instanceof Integer) {
                                    int value = (Integer) object;
                                    if (value != defaultValue) {
                                        params.putString(name, String.valueOf(value));
                                    }
                                }
                            } else {
                                String value = String.valueOf(object);
                                params.putString(name, value);
                            }
                        }
                    }
                } else if (field.isAnnotationPresent(RestMethodExtUrlParam.class)) {
                    RestMethodExtUrlParam param = field.getAnnotation(RestMethodExtUrlParam.class);
                    if (param != null) {
                        Object object = field.get(this);
                        if (object != null) {
                            params.putString("methodExt", String.valueOf(object));
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    private void LOGD(String message) {
        if (DEBUG) {
            Log.d(this.getClass().getName(), message);
        }
    }

    /**
     * 获取T的类型
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> getGenericType() {
        Type genType = getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return null;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (params.length < 1) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[0] instanceof Class)) {
            return null;
        }
        return (Class<T>) params[0];
    }
}
