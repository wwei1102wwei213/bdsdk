package com.wei.ai.biz;


import com.wei.wlib.http.IWLibHttpBiz;
import com.wei.wlib.http.WLibHttpListener;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class Factory {

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag){
        return new HttpRespBiz(flag, tag, mResp);
    }

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag, Class<?> mClass){
        return new HttpRespBiz(flag, tag, mResp, mClass);
    }

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag, Class<?> mClass, int codeType){
        return new HttpRespBiz(flag, tag, mResp, mClass, codeType);
    }

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag, Class<?> mClass, boolean checkUrl){
        return new HttpRespBiz(flag, tag, mResp, mClass, checkUrl);
    }

}
