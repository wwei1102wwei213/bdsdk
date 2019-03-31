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
        HttpFlag.changeBaseUrl();
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
            case HttpFlag.FLAG_ATTENDANCE:
                result = HttpFlag.URL_ATTENDANCE;
                break;
            case HttpFlag.FLAG_LOGIN:
                result = HttpFlag.URL_LOGIN;
                break;
            case HttpFlag.FLAG_SEARCH_NAME_FOR_SAFE:
                result = HttpFlag.URL_SEARCH_NAME_FOR_SAFE;
                break;
            case HttpFlag.FLAG_SEARCH_NAME_FOR_UNSAFE:
                result = HttpFlag.URL_SEARCH_NAME_FOR_UNSAFE;
                break;
            case HttpFlag.FLAG_SEARCH_FACE_FOR_SAFE:
                result = HttpFlag.URL_SEARCH_FACE_FOR_SAFE;
                break;
            case HttpFlag.FLAG_SEARCH_FACE_FOR_UNSAFE:
                result = HttpFlag.URL_SEARCH_FACE_FOR_UNSAFE;
                break;
            case HttpFlag.FLAG_INSERT_VIOLATION_INFO:
                result = HttpFlag.URL_INSERT_VIOLATION_INFO;
                break;
            case HttpFlag.FLAG_SEARCH_VIOLATION_BY_PAGE:
                result = HttpFlag.URL_SEARCH_VIOLATION_BY_PAGE;
                break;

        }
        return result;
    }

}
