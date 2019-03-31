package com.wei.wlib.http;

/**
 * Created by Administrator on 2018/10/10.
 *
 * Http请求的接口
 *
 * @author wwei
 */
public interface WLibHttpListener {

    /**
     *
     *  参数说明
     *
     *  flag  接口标识
     *
     *  tag   请求标识
     *
     *  data       请求数据（格式化）
     *
     *             格式化数据需要和 Class<?> mClass 参数配合使用
     *             如果mClass为null,则response为String类型且不为空
     *
     *  response   请求数据（源数据）
     *
     *  errorType  错误标识
     *
     *  hint  提示信息
     *
     *  isShow  显示或关闭loading
     *
     */

    //处理返回数据
    void handleResp(Object formatData, int flag, Object tag, String response, String hint);

    /**
     * 处理请求中loading操作
     * @param flag 接口标识
     * @param tag  请求标识
     * @param isShow  显示或关闭loading
     *                 true 发起请求之前的操作
     *                 false 请求之后的操作 此操作在handleResp和handleAfter之前
     *                 开发人员根据本身的需求选择loading的时机
     */
    void handleLoading(int flag, Object tag, boolean isShow);

    //请求错误
    void handleError(int flag, Object tag, int errorType, String response, String hint);

    //请求结束后操作
    void handleAfter(int flag, Object tag);
}
