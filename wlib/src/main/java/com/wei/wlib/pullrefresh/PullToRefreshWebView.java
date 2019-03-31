package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class PullToRefreshWebView extends PullToRefreshBase<WebView> {
    public PullToRefreshWebView(Context context) {
        this(context, null);
    }

    public PullToRefreshWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected WebView createRefreshableView(Context context, AttributeSet attrs) {
        return new WebView(context);
    }

    protected boolean isReadyForPullDown() {
        return ((WebView) this.mRefreshableView).getScrollY() == 0;
    }

    protected boolean isReadyForPullUp() {
        return ((float) ((WebView) this.mRefreshableView).getScrollY()) >= ((float) Math.floor((double) (((WebView) this.mRefreshableView).getScale() * ((float) ((WebView) this.mRefreshableView).getContentHeight())))) - ((float) ((WebView) this.mRefreshableView).getHeight());
    }
}
