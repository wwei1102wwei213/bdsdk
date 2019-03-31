package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.wei.wlib.elasticity.ElasticityScrollView;

public class PullElasticityScrollView extends PullToRefreshBase<ElasticityScrollView> {
    public PullElasticityScrollView(Context context) {
        this(context, null);
    }

    public PullElasticityScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullElasticityScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected ElasticityScrollView createRefreshableView(Context context, AttributeSet attrs) {
        return new ElasticityScrollView(context);
    }

    protected boolean isReadyForPullDown() {
        return ((ElasticityScrollView) this.mRefreshableView).getScrollY() == 0;
    }

    protected boolean isReadyForPullUp() {
        View scrollViewChild = ((ElasticityScrollView) this.mRefreshableView).getChildAt(0);
        if (scrollViewChild == null) {
            return false;
        }
        if (((ElasticityScrollView) this.mRefreshableView).getScrollY() >= scrollViewChild.getHeight() - getHeight()) {
            return true;
        }
        return false;
    }
}
