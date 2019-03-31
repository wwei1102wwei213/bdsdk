package com.wei.wlib.http;


public class WLibCookieManger {

    private WLibCookieManger(){}

    private String cookies;

    private static WLibCookieManger instance;

    public static WLibCookieManger getInstance() {
        if (instance==null) {
            synchronized (WLibCookieManger.class) {
                if (instance==null) {
                    instance = new WLibCookieManger();
                }
            }
        }
        return instance;
    }

    /*public String getCookies() {
        if (cookies==null) {
            cookies = cookieHeader(W.cookieJar.getCookies());
        }
        return cookies;
    }

    public void updateCookies() {
        cookies = cookieHeader(BaseApplication.getInstance().cookieJar.getCookies());
    }

    private String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        return cookieHeader.toString();
    }*/
}
