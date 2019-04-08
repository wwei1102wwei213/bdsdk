package com.wei.wlib.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wei.wlib.util.WLibLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.builder.PostStringBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;


public class WLibDefaultHttpBiz implements IWLibHttpBiz {

    protected int flag;
    protected Object tag;
    private String OkTag;
    private Gson mGson;
    private Class<?> mClass;
    private int codeType = WLibHttpFlag.RESULT_DATA_FORMAT;
    protected WLibHttpListener callback;
    private List<String> urls;
    private String currentUrl;
    private String currentBaseUrl;
    private boolean IsChangeBase = false;
    private boolean checkUrl = false;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public WLibDefaultHttpBiz(int flag, Object tag, WLibHttpListener callback) {
        this.flag = flag;
        this.tag = tag;
        this.callback = callback;
        mGson = new Gson();
        this.mClass = null;
        OkTag = flag + "-" + System.currentTimeMillis();
    }

    public WLibDefaultHttpBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass) {
        this.flag = flag;
        this.tag = tag;
        this.callback = callback;
        mGson = new Gson();
        this.mClass = mClass;
        OkTag = flag + "-" + System.currentTimeMillis();
    }

    public WLibDefaultHttpBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass, int codeType) {
        this.flag = flag;
        this.tag = tag;
        this.callback = callback;
        mGson = new Gson();
        this.mClass = mClass;
        OkTag = flag + "-" + System.currentTimeMillis();
        this.codeType = codeType;
    }

    public WLibDefaultHttpBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass, boolean checkUrl) {
        this.flag = flag;
        this.tag = tag;
        this.callback = callback;
        mGson = new Gson();
        this.mClass = mClass;
        OkTag = flag + "-" + System.currentTimeMillis();
        this.checkUrl = checkUrl;
    }


    @Override
    public void post(final Map<String, String> params) {
        if (callback!=null) callback.handleLoading(flag, tag, true);
        currentBaseUrl = getCurrentBaseUrl();
        currentUrl = postUrl();
        handlePost(params);
    }

    private void handlePost(final Map<String, String> params) {

        PostStringBuilder builder = OkHttpUtils.postString()
                .url(currentUrl)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(mGson.toJson(params));

        builder.tag(OkTag).build().execute(new WLibStringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                WLibLog.h("请求地址："+currentUrl+",FLAG:"+flag + ",错误信息:"+e.getMessage()+",请求参数："+mGson.toJson(params));
                if (checkUrl) {
                    if (urls==null) {
                        urls = getBaseUrls();
                    }
                    urls.remove(currentBaseUrl);
                    if (urls.size()>0) {
                        IsChangeBase = true;
                        currentUrl = currentUrl.replace(currentBaseUrl, urls.get(0));
                        currentBaseUrl = urls.get(0);
                        WLibLog.h("替换后："+currentUrl+",BASE:"+currentBaseUrl);
                        handlePost(params);
                    } else {
                        if (callback!=null) callback.handleLoading(flag, tag, false);
                        handleError(WLibHttpFlag.HTTP_ERROR_DISCONNECT, null, null);
                        cancel();
                    }
                } else {
                    if (callback!=null) callback.handleLoading(flag, tag, false);
                    handleError(WLibHttpFlag.HTTP_ERROR_DISCONNECT, null, null);
                    cancel();
                }
            }

            @Override
            public void onResponse(String response, int id) {
                WLibLog.h("请求地址："+currentUrl+"\n请求数据："+response+"(POST)\n请求参数："+mGson.toJson(params));
                if (checkUrl&&IsChangeBase) {
                    changeBaseUrl(currentBaseUrl);
                    if (callback!=null) callback.handleError(flag, tag, WLibHttpFlag.HTTP_ERROR_BASE_URL_CHANGED, currentBaseUrl, null);
                }
                handleResponse(response);
                cancel();
            }
        });
    }

    @Override
    public void get(final Map<String, String> params) {
        if (callback!=null) callback.handleLoading(flag, tag, true);
        currentBaseUrl = getCurrentBaseUrl();
        currentUrl = getUrl();
        handleGet(params);
    }

    private void handleGet(final Map<String, String> params) {
        GetBuilder builder = OkHttpUtils.get().url(getUrl());
        if (params!=null&&params.size()>0) {
            builder.params(params);
        }
        builder.tag(OkTag).build().execute(new WLibStringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
//                WLibLog.h("请求地址："+currentUrl+",FLAG:"+flag + ",错误信息:"+e.getMessage()+",请求参数："+mGson.toJson(params));
                if (checkUrl) {
                    if (urls==null) {
                        urls = getBaseUrls();
                    }
                    urls.remove(currentBaseUrl);
                    if (urls.size()>0) {
                        IsChangeBase = true;
                        currentUrl = currentUrl.replace(currentBaseUrl, urls.get(0));
                        currentBaseUrl = urls.get(0);
                        WLibLog.h("替换后："+currentUrl+",BASE:"+currentBaseUrl);
                        handleGet(params);
                    } else {
                        if (callback!=null) callback.handleLoading(flag, tag, false);
                        handleError(WLibHttpFlag.HTTP_ERROR_DISCONNECT, null, null);
                        cancel();
                    }
                } else {
                    if (callback!=null) callback.handleLoading(flag, tag, false);
                    handleError(WLibHttpFlag.HTTP_ERROR_DISCONNECT, null, null);
                    cancel();
                }
            }

            @Override
            public void onResponse(String response, int id) {
//                WLibLog.h("请求地址："+currentUrl+"(GET)\n请求参数："+mGson.toJson(params)+ "\n请求数据："+response);
                if (checkUrl&&IsChangeBase) {
                    changeBaseUrl(currentBaseUrl);
                    if (callback!=null) callback.handleError(flag, tag, WLibHttpFlag.HTTP_ERROR_BASE_URL_CHANGED, currentBaseUrl, null);
                }
                handleResponse(response);
                cancel();
            }
        });
    }

    @Override
    public void uploadPost() {

    }

    @Override
    public void multiUploadPost() {

    }

    protected String getUrl() {
        return null;
    }

    protected String postUrl() {
        return null;
    }

    protected String getCurrentBaseUrl() {
        return null;
    }

    protected List<String> getBaseUrls() {
        return null;
    }

    protected void changeBaseUrl(String baseUrl) {}

    private void handleResponse(String response) {
        if (callback!=null) {
            callback.handleLoading(flag, tag, false);
            try {
                if (response != null) {
                    if (codeType!=WLibHttpFlag.RESULT_DATA_FORMAT) {
                        callback.handleResp(response, flag, tag, response, null);
                    } else {
                        JSONObject resJson = new JSONObject(response);
                        int status = resJson.optInt("result");
                        String msg = resJson.optString("msg");
                        if (status == WLibHttpFlag.RESULT_OK) {
                            Object data = resJson.opt("obj");

                            if (mClass==null) {
                                callback.handleResp(data, flag, tag, response, msg);
                            } else {
                                Object o = null;
                                if (data!=null) {
                                    o = formatResultData(data+"");
                                }
                                if (o!=null) {
                                    callback.handleResp(o, flag, tag, response, msg);
                                } else {
                                    handleError(WLibHttpFlag.HTTP_ERROR_DATA_EMPTY, response, "数据异常");
                                }
                            }
                        } else {
                            handleError(WLibHttpFlag.HTTP_ERROR_CODE, response, msg);
                        }
                    }
                } else {
                    handleError(WLibHttpFlag.HTTP_ERROR_RESULT_EMPTY, null, "数据异常");
                }
            } catch (JSONException | JsonSyntaxException e) {
                WLibLog.e(e);
                handleError(WLibHttpFlag.HTTP_ERROR_FORMAT, response, "数据异常");
            } catch (Exception e) {
                WLibLog.e(e);
                handleError(WLibHttpFlag.HTTP_ERROR_OTHER, response, null);
            }
        } else {
            WLibLog.h("请求地址："+getUrl(), " handleResponse: callback is empty.");
        }
    }

    private void handleError(int errorType, String source, String msg) {
        WLibLog.e("请求地址："+postUrl(), "错误类型："+getErrorSummary(errorType));
        try {
            if (callback!=null) {
                callback.handleError(flag, tag, errorType, source, msg);
            }
        } catch (Exception e){
            WLibLog.e(e);
        }
    }

    private Object formatResultData(String response) {
        if (response == null) return null;
        if (mClass == null) return response;
        Object o = null;
        try {
            o = mGson.fromJson(response, mClass);
        } catch (JsonSyntaxException e) {
            WLibLog.e("请求成功,JSON异常:" + e.toString());
        } catch (Exception e){
            WLibLog.e("请求成功,其他异常:" + e.toString());
        }
        return o;
    }

    private String getErrorSummary(int errorType){
        String error  = "未知错误";
        switch (errorType) {
            case WLibHttpFlag.HTTP_ERROR_NETWORK:
                error = "网络异常";
                break;
            case WLibHttpFlag.HTTP_ERROR_DISCONNECT:
                error = "连接异常";
                break;
            case WLibHttpFlag.HTTP_ERROR_RESULT_EMPTY:
                error = "返回数据为空";
                break;
            case WLibHttpFlag.HTTP_ERROR_FORMAT:
                error = "数据解析异常";
                break;
            case WLibHttpFlag.HTTP_ERROR_CODE:
                error = "数据code异常";
                break;
            case WLibHttpFlag.HTTP_ERROR_DATA_EMPTY:
                error = "data数据为空";
                break;
            case WLibHttpFlag.HTTP_ERROR_OTHER:
                error = "其他异常";
                break;
        }
        return error;
    }

    private void cancel() {
        try {
            if (callback!=null) {
                callback.handleAfter(flag, tag);
            }
        } catch (Exception e){
            WLibLog.e(e);
        }
        try {
            OkHttpUtils.getInstance().cancelTag(OkTag);
        } catch (Exception e){
            WLibLog.e(e);
        }
    }

}
