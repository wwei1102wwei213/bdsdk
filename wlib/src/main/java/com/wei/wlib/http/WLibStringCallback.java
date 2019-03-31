package com.wei.wlib.http;

import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;

import okhttp3.Response;

public abstract class WLibStringCallback extends StringCallback{

    /*@Override
    public String parseNetworkResponse(Response response) throws IOException {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return super.parseNetworkResponse(response);
    }*/

    @Override
    public String parseNetworkResponse(Response response, int id) throws IOException {
        return super.parseNetworkResponse(response, id);
    }

}
