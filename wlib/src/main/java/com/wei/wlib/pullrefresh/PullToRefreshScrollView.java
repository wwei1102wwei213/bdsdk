package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class PullToRefreshScrollView extends PullToRefreshBase<ScrollView> {
    public PullToRefreshScrollView(Context context) {
        this(context, null);
    }

    public PullToRefreshScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {
        return new ScrollView(context);
    }

    protected boolean isReadyForPullDown() {
        return ((ScrollView) this.mRefreshableView).getScrollY() == 0;
    }

    protected boolean isReadyForPullUp() {
        View scrollViewChild = ((ScrollView) this.mRefreshableView).getChildAt(0);
        if (scrollViewChild == null) {
            return false;
        }
        if (((ScrollView) this.mRefreshableView).getScrollY() >= scrollViewChild.getHeight() - getHeight()) {
            return true;
        }
        return false;
    }
}
