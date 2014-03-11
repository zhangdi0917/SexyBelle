package com.jesson.android.internet.core.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.jesson.android.Jess;
import com.jesson.android.internet.InternetUtils;
import com.jesson.android.internet.core.BeanRequestInterface;
import com.jesson.android.internet.core.HttpClientInterface;
import com.jesson.android.internet.core.HttpConnectHookListener;
import com.jesson.android.internet.core.InternetConfig;
import com.jesson.android.internet.core.InternetStringUtils;
import com.jesson.android.internet.core.JsonUtils;
import com.jesson.android.internet.core.MultipartHttpEntity;
import com.jesson.android.internet.core.NetWorkException;
import com.jesson.android.internet.core.RequestBase;
import com.jesson.android.internet.core.RequestEntity;
import com.jesson.android.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

class BeanRequestDefaultImplInternal implements BeanRequestInterface {

    private static final String TAG = "BeanRequestImpl";
    private static final boolean DEBUG = InternetConfig.DEBUG;
    private static final boolean DEBUG_SERVER_CODE = false;

    private static final String KEY_METHOD = "method";

    private static final String KEY_HTTP_METHOD = "httpMethod";

    private static final String KEY_METHOD_EXT = "methodExt";

    private static BeanRequestDefaultImplInternal mInstance;

    private HttpClientInterface mHttpClientInterface;

    private HttpConnectHookListener mHttpHookListener;

    private Context mContext;

    private static Object lockObject = new Object();

    public static BeanRequestDefaultImplInternal getInstance(Context context) {
        if (mInstance == null) {
            synchronized (lockObject) {
                if (mInstance == null) {
                    mInstance = new BeanRequestDefaultImplInternal(context);
                }
            }
        }
        return mInstance;
    }

    private BeanRequestDefaultImplInternal(Context context) {
        mHttpClientInterface = HttpClientFactory.createHttpClientInterface(context);
        mContext = context;
    }

    @Override
    public void setRequestAdditionalKVInfo(Map<String, String> kvInfo) {
    }

    @Override
    public <T> T request(RequestBase<T> request) throws NetWorkException {
        long entryTime = System.currentTimeMillis();
        if (DEBUG) {
            Jess.LOGD("Entery Internet request, current time = " + entryTime + "ms from 1970");
        }

        if (request == null) {
            if (mHttpHookListener != null) {
                mHttpHookListener.onHttpConnectError(NetWorkException.REQUEST_NULL, "Request can't be NUll", request);
            } else {
                sendLocalNetworkError(NetWorkException.REQUEST_NULL, "Request can't be NUll", null);
            }

            throw new NetWorkException(NetWorkException.REQUEST_NULL, "Request can't be NUll", null);
        }

        boolean ignore = request.canIgnoreResult();
        if (!mHttpClientInterface.isNetworkAvailable()) {
            if (!ignore) {
                if (mHttpHookListener != null) {
                    mHttpHookListener.onHttpConnectError(NetWorkException.NETWORK_NOT_AVILABLE, "网络连接错误，请检查您的网络", request);
                } else {
                    sendLocalNetworkError(NetWorkException.NETWORK_NOT_AVILABLE, "网络连接错误，请检查您的网络", null);
                }
            }

            throw new NetWorkException(NetWorkException.NETWORK_NOT_AVILABLE, "网络连接错误，请检查您的网络", null);
        }

        RequestEntity requestEntity = request.getRequestEntity();
        Bundle baseParams = requestEntity.getBasicParams();

        if (baseParams == null) {
            if (!ignore) {
                if (mHttpHookListener != null) {
                    mHttpHookListener.onHttpConnectError(NetWorkException.PARAM_EMPTY, "网络请求参数列表不能为空", request);
                } else {
                    sendLocalNetworkError(NetWorkException.PARAM_EMPTY, "网络请求参数列表不能为空", null);
                }
            }

            throw new NetWorkException(NetWorkException.PARAM_EMPTY, "网络请求参数列表不能为空", null);
        }

        String api_url = baseParams.getString(KEY_METHOD);
        baseParams.remove(KEY_METHOD);
        String httpMethod = baseParams.getString(KEY_HTTP_METHOD);
        baseParams.remove(KEY_HTTP_METHOD);
        if (baseParams.containsKey(KEY_METHOD_EXT)) {
            String ext = baseParams.getString(KEY_METHOD_EXT);
            api_url = api_url + ext;
            baseParams.remove(KEY_METHOD_EXT);
        }
        if (mHttpHookListener != null) {
            mHttpHookListener.onPreHttpConnect(api_url, api_url, baseParams);
        }

        String contentType = requestEntity.getContentType();
        if (contentType == null) {
            if (!ignore) {
                if (mHttpHookListener != null) {
                    mHttpHookListener.onHttpConnectError(NetWorkException.MISS_CONTENT, "Content Type MUST be specified",
                            request);
                } else {
                    sendLocalNetworkError(NetWorkException.MISS_CONTENT, "Content Type MUST be specified", api_url);
                }
            }

            throw new NetWorkException(NetWorkException.MISS_CONTENT, "Content Type MUST be specified", null);
        }

        if (DEBUG) {
            StringBuilder param = new StringBuilder();
            if (baseParams != null) {
                for (String key : baseParams.keySet()) {
                    param.append("|    ").append(key).append(" : ").append(baseParams.get(key)).append("\n");
                }
            }

            Jess.LOGD("\n\n//***\n| [[request::" + request + "]] \n" + "| RestAPI URL = " + api_url
                    + "\n| after getSig bundle params is = \n" + param + " \n\\\\***\n");
        }

        HttpEntity entity = null;
        if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_TEXT_PLAIN)) {
            if (httpMethod.equals("POST")) {
                List<NameValuePair> paramList = convertBundleToNVPair(baseParams);
                if (paramList != null) {
                    try {
                        entity = new UrlEncodedFormEntity(paramList, HTTP.UTF_8);
                    } catch (UnsupportedEncodingException e) {
                        if (!ignore) {
                            if (mHttpHookListener != null) {
                                mHttpHookListener.onHttpConnectError(NetWorkException.ENCODE_HTTP_PARAMS_ERROR,
                                        "Unable to encode http parameters", request);
                            } else {
                                sendLocalNetworkError(NetWorkException.ENCODE_HTTP_PARAMS_ERROR, "Unable to encode http parameters", api_url);
                            }
                        }

                        throw new NetWorkException(NetWorkException.ENCODE_HTTP_PARAMS_ERROR,
                                "Unable to encode http parameters", null);
                    }
                }
            } else if (httpMethod.equals("GET")) {
                StringBuilder sb = new StringBuilder(api_url);
                sb.append("?");
                for (String key : baseParams.keySet()) {
                    sb.append(key).append("=").append(baseParams.getString(key)).append("&");
                }
                api_url = sb.substring(0, sb.length() - 1);
                if (DEBUG) {
                    Jess.LOGD("\n\n//***\n| GET url : " + api_url + "\n\\\\***\n");
                }
            }
        } else if (contentType.equals(RequestEntity.REQUEST_CONTENT_TYPE_MUTIPART)) {
            requestEntity.setBasicParams(baseParams);
            entity = new MultipartHttpEntity(requestEntity);
        }

        if (DEBUG) {
            Jess.LOGD("before get internet data from server, time cost from entry = "
                    + (System.currentTimeMillis() - entryTime) + "ms");
        }

        String response = mHttpClientInterface.getResource(String.class, api_url, httpMethod, entity);
        if (DEBUG_SERVER_CODE) {
            response = "{code:7,data:\"测试code7\"}";
        }

        if (DEBUG) {
            Jess.LOGD(response);
            long endTime = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder(1024);
            sb.append("\n\n")
                    .append("//***\n")
                    .append("| ------------- begin response ------------\n")
                    .append("|\n")
                    .append("| [[request::" + request + "]] " + " cost time from entry : " + (endTime - entryTime)
                            + "ms. " + "raw response String = \n");
            Jess.LOGD(sb.toString());
            sb.setLength(0);
            if (response != null) {
                try {
                    sb.append("| " + StringUtils.jsonFormatter(response) + " \n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sb.append("| " + response + "\n");
            }
            int step = 1024;
            int index = 0;
            do {
                if (index >= sb.length()) {
                    break;
                } else {
                    if ((index + step) < sb.length()) {
                        Jess.LOGD(sb.substring(index, index + step));
                    } else {
                        Jess.LOGD(sb.substring(index, sb.length()));
                    }
                }
                index = index + step;
            } while (index < sb.length());
            sb.setLength(0);
            sb.append("|\n|\n").append("| ------------- end response ------------\n").append("\\\\***");
            Jess.LOGD(sb.toString());

            // Config.LOGD("\n\n");
            // Config.LOGD("//***");
            // Config.LOGD("| ------------- begin response ------------");
            // Config.LOGD("|");
            // Config.LOGD("| [[RRConnect::request::" + request + "]] " +
            // " cost time from entry : " + (endTime - entryTime)
            // + "ms. " + "raw response String = ");
            // Config.LOGD("| " + response);
            // Config.LOGD("|");
            // Config.LOGD("| ------------- end response ------------\n|\n|");
            // Config.LOGD("\\\\***");
        }

        if (mHttpHookListener != null) {
            mHttpHookListener.onPostHttpConnect(response, 200);
        }

        if (response == null) {
            if (!ignore) {
                if (mHttpHookListener != null) {
                    mHttpHookListener.onHttpConnectError(NetWorkException.SERVER_ERROR, "服务器错误，请稍后重试", request);
                } else {
                    sendLocalNetworkError(NetWorkException.SERVER_ERROR, "服务器错误，请稍后重试", api_url);
                }
            }

            throw new NetWorkException(NetWorkException.SERVER_ERROR, "服务器错误，请稍后重试", null);
        } else {
            // 调试网络流量
            // if (Config.DEBUG_NETWORK_ST) {
            // String value =
            // DataBaseOperator.getInstance().queryCacheValue(Config.NETWORK_STATISTICS_TYPE,
            // method,
            // Config.NETWORK_STATISTICS_DOWN);
            // int oldSize = 0;
            // if (!TextUtils.isEmpty(value)) {
            // oldSize = Integer.valueOf(value);
            // }
            // oldSize += response.getBytes().length;
            // DataBaseOperator.getInstance().addCacheValue(Config.NETWORK_STATISTICS_TYPE,
            // method,
            // Config.NETWORK_STATISTICS_DOWN, String.valueOf(oldSize));
            // }
        }

        // JsonErrorResponse failureResponse = JsonUtils.parseError(response);
        // if (failureResponse == null) {
        // if (!TextUtils.isEmpty(method) && method.equals("batch.batchRun")) {
        // // 特殊处理batch.batchRun
        // BatchRunResponse responeObj = new BatchRunResponse();
        // BatchRunRequest reqeustObj = (BatchRunRequest) request;
        //
        // if (reqeustObj != null && reqeustObj.requestList != null) {
        // responeObj.responseList = new
        // ResponseBase[reqeustObj.requestList.length];
        // }
        //
        // try {
        // JSONObject jsonObj = new JSONObject(response);
        // if (reqeustObj.requestList != null) {
        // for (int index = 0; index < reqeustObj.requestList.length; ++index) {
        // String api_name = reqeustObj.requestList[index].getMethodName();
        // if (!TextUtils.isEmpty(api_name)) {
        // String apiData = jsonObj.optString(api_name);
        // if (!TextUtils.isEmpty(apiData)) {
        // JsonErrorResponse fResponse = JsonUtils.parseError(apiData);
        // if (fResponse == null) {
        // ResponseBase oneResponse = (ResponseBase) JsonUtils.parse(apiData,
        // reqeustObj.requestList[index].getGenericType());
        // responeObj.responseList[index] = oneResponse;
        // } else {
        // responeObj.responseList[index] = null;
        // }
        // } else {
        // responeObj.responseList[index] = null;
        // }
        // }
        // }
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // return null;
        // }
        //
        // return (T) responeObj;
        // } else {
        T ret = null;
        try {
            //先检查是否是错误的数据结构
            if (!request.getHandleErrorSelf()) {
                JsonErrorResponse errorResponse = JsonUtils.parseError(response);
                if (errorResponse != null) {
                    if (!ignore) {
                        if (mHttpHookListener != null) {
                            if (errorResponse.errorCode != 0
                                    && !TextUtils.isEmpty(errorResponse.errorMsg)) {
                                //the response is a server error response
                                mHttpHookListener.onHttpConnectError(errorResponse.errorCode, errorResponse.errorMsg, request);
                            }
                        } else {
                            sendAPIErrorLocal(errorResponse, api_url);
                        }
                    }
                    return null;
                }
            }

            ret = JsonUtils.parse(response, request.getGenericType());
            if (DEBUG) {
                Jess.LOGD("Before return, after success get the data from server, parse cost time from entry = "
                        + (System.currentTimeMillis() - entryTime) + "ms" + " response parse result = " + ret);
            }

            if (ret == null) {
                try {
                    JsonErrorResponse response2 = JsonUtils.parseError(response);
                    if (response2 != null) {
                        if (!ignore) {
                            if (mHttpHookListener != null) {
                                mHttpHookListener.onHttpConnectError(response2.errorCode, response2.errorMsg, request);
                            } else {
                                sendAPIErrorLocal(response2, api_url);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                JsonErrorResponse response2 = JsonUtils.parseError(response);
                if (response2 != null) {
                    if (!ignore) {
                        if (mHttpHookListener != null) {
                            mHttpHookListener.onHttpConnectError(response2.errorCode, response2.errorMsg, request);
                        } else {
                            sendAPIErrorLocal(response2, api_url);
                        }
                    }
                }
                ret = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return ret;
    }

    private void sendAPIErrorLocal(JsonErrorResponse response, String apiName) {
        Intent i = new Intent();
        i.putExtra("code", response.errorCode);
        i.putExtra("msg", response.errorMsg);
        i.putExtra("apiName", apiName);
        i.setAction(InternetUtils.ACTION_INTERNET_ERROR);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void sendLocalNetworkError(int code, String message, String apiName) {
        Intent i = new Intent();
        i.putExtra("code", code);
        i.putExtra("msg", message);
        i.putExtra("apiName", apiName);
        i.setAction(InternetUtils.ACTION_INTERNET_ERROR_LOCAL);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    @Override
    public String getSig(Bundle params, String secret_key) {
        if (params == null || params.size() == 0) {
            return null;
        }

        TreeMap<String, String> sortParams = new TreeMap<String, String>();
        for (String key : params.keySet()) {
            sortParams.put(key, params.getString(key));
        }

        Vector<String> vecSig = new Vector<String>();
        for (String key : sortParams.keySet()) {
            String value = sortParams.get(key);
            if (value.length() > InternetConfig.SIG_PARAM_MAX_LENGTH) {
                value = value.substring(0, InternetConfig.SIG_PARAM_MAX_LENGTH);
            }
            vecSig.add(key + "=" + value);
        }
        // LOGD("[[getSig]] after operate, the params is : " + vecSig);

        String[] nameValuePairs = new String[vecSig.size()];
        vecSig.toArray(nameValuePairs);

        for (int i = 0; i < nameValuePairs.length; i++) {
            for (int j = nameValuePairs.length - 1; j > i; j--) {
                if (nameValuePairs[j].compareTo(nameValuePairs[j - 1]) < 0) {
                    String temp = nameValuePairs[j];
                    nameValuePairs[j] = nameValuePairs[j - 1];
                    nameValuePairs[j - 1] = temp;
                }
            }
        }
        StringBuffer nameValueStringBuffer = new StringBuffer();
        for (int i = 0; i < nameValuePairs.length; i++) {
            nameValueStringBuffer.append(nameValuePairs[i]);
        }

        nameValueStringBuffer.append(secret_key);
        String sig = InternetStringUtils.MD5Encode(nameValueStringBuffer.toString());
        return sig;
    }

    private void checkException(int exceptionCode) {
        // switch (exceptionCode) {
        // case RRException.API_EC_INVALID_SESSION_KEY:
        // case RRException.API_EC_USER_AUDIT:
        // case RRException.API_EC_USER_BAND:
        // case RRException.API_EC_USER_SUICIDE:
        // LOGD("[[checkException]] should clean the user info in local");
        // //
        // mAccessTokenManager.clearUserLoginInfoByUid(mAccessTokenManager.getUID());
        // break;
        // default:
        // return;
        // }
    }

    private List<NameValuePair> convertBundleToNVPair(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            list.add(new BasicNameValuePair(key, bundle.getString(key)));
        }

        return list;
    }

    @Override
    public void setHttpHookListener(HttpConnectHookListener l) {
        mHttpHookListener = l;
    }

}
