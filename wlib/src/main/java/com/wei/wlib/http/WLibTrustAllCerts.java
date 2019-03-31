package com.wei.wlib.http;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Created by Administrator on 2016/12/23.
 */
public class WLibTrustAllCerts implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
}