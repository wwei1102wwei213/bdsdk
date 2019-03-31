package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.wei.wlib.elasticity.SmartScrollView;

public class PullToRefreshSmartScrollView extends PullToRefreshBase<SmartScrollView> {
    public PullToRefreshSmartScrollView(Context context) {
        this(context, null);
    }

    public PullToRefreshSmartScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshSmartScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected SmartScrollView createRefreshableView(Context context, AttributeSet attrs) {
        return new SmartScrollView(context);
    }

    protected boolean isReadyForPullDown() {
        return ((SmartScrollView) this.mRefreshableView).getScrollY() == 0;
    }

    protected boolean isReadyForPullUp() {
        View scrollViewChild = ((SmartScrollView) this.mRefreshableView).getChildAt(0);
        if (scrollViewChild == null) {
            return false;
        }
        if (((SmartScrollView) this.mRefreshableView).getScrollY() >= scrollViewChild.getHeight() - getHeight()) {
            return true;
        }
        return false;
    }
}
