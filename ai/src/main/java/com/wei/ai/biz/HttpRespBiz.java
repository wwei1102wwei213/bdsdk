package com.wei.ai.biz;

import com.wei.wlib.http.WLibDefaultHttpBiz;
import com.wei.wlib.http.WLibHttpListener;

import java.util.List;

public class HttpRespBiz extends WLibDefaultHttpBiz {

    public HttpRespBiz(int flag, Object tag, WLibHttpListener callback) {
        super(flag, tag, callback);
    }

    public HttpRespBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass) {
        super(flag, tag, callback, mClass);
    }

    public HttpRespBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass, int codeType) {
        super(flag, tag, callback, mClass, codeType);
    }

    public HttpRespBiz(int flag, Object tag, WLibHttpListener callback, Class<?> mClass, boolean checkUrl) {
        super(flag, tag, callback, mClass, checkUrl);
    }

    @Override
    protected List<String> getBaseUrls() {
        return null;
    }

    @Override
    protected String getCurrentBaseUrl() {
        return HttpFlag.BASE_URL;
    }

    @Override
    protected void changeBaseUrl(String baseUrl) {
        HttpFlag.BASE_URL = baseUrl;
//        HttpFlag.changeBaseUrl();
    }

    @Override
    protected String getUrl() {
        String result = null;
        switch (flag) {

        }
        return result;
    }

    @Override
    protected String postUrl() {
        String result = null;
        switch (flag) {
            case HttpFlag.FLAG_INSERT_ATTENDANCE:
                result = HttpFlag.URL_INSERT_ATTENDANCE;
                break;
            case HttpFlag.FLAG_GET_ATTENDANCE_NUM:
                result = HttpFlag.URL_GET_ATTENDANCE_NUM;
                break;

        }
        return result;
    }

}
