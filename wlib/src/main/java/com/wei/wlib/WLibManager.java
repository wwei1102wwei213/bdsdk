package com.wei.wlib;

import android.content.Context;

import com.wei.wlib.http.persistentcookiejar.PersistentCookieJar;
import com.wei.wlib.http.persistentcookiejar.cache.SetCookieCache;
import com.wei.wlib.http.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.wei.wlib.thread.WLibThreadPoolManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.https.HttpsUtils;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * WLib管理器设置
 *      init(...) 方法必须调用
 *      initOkHttp(...) 如果使用http模块则必须调用
 *
 *
 *
 */
public class WLibManager {

    private WLibManager(){}

    private static WLibManager instance;
    private PersistentCookieJar cookieJar;//序列化cookie

    public static WLibManager getInstance() {
        if (instance==null) {
            synchronized (WLibManager.class) {
                if (instance==null) {
                    instance = new WLibManager();
                }
            }
        }
        return instance;
    }

    public void init() {

    }

    public void initOkHttp() {
        initOkHttp(null, false, null);
    }

    public void initOkHttp(Interceptor interceptor) {
        initOkHttp(null, false, interceptor);
    }

    public void initOkHttp(Context context, boolean openCookies, Interceptor interceptor) {
        //cookie保存到本地
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //拦截请求加上UA
        if (interceptor!=null) builder.addInterceptor(interceptor);
        //序列化cookie
        if (openCookies) {
            if (cookieJar==null) {
                cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            }
            builder.cookieJar(cookieJar);
        }
        //接收所有https
        SSLSocketFactory sslSocketFactory = HttpsUtils.getSslSocketFactory(
                null, null, null).sSLSocketFactory;
        builder.sslSocketFactory(sslSocketFactory);
        OkHttpUtils.initClient(builder.build());
    }

    public static void destory () {
        WLibThreadPoolManager.shutdown();
    }

    public void clearCookieJar() {
        if (cookieJar!=null) cookieJar.clear();
    }

}
