package com.wei.wlib;

import com.wei.wlib.http.IWLibHttpBiz;
import com.wei.wlib.http.WLibDefaultHttpBiz;
import com.wei.wlib.http.WLibHttpListener;

public class WLibFactory {

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag, Class<?> mClass){
        return new WLibDefaultHttpBiz(flag, tag, mResp, mClass);
    }

    public static IWLibHttpBiz resp(WLibHttpListener mResp, int flag, Object tag, Class<?> mClass, int codeType){
        return new WLibDefaultHttpBiz(flag, tag, mResp, mClass, codeType);
    }

}
