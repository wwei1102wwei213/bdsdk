package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;

import com.wei.wlib.elasticity.ElasticityNestedScrollView;


public class PullElasticityNestedScrollView extends PullToRefreshBase<ElasticityNestedScrollView> {
    public PullElasticityNestedScrollView(Context context) {
        this(context, null);
    }

    public PullElasticityNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullElasticityNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected ElasticityNestedScrollView createRefreshableView(Context context, AttributeSet attrs) {
        return new ElasticityNestedScrollView(context);
    }



    protected boolean isReadyForPullDown() {
        return ((ElasticityNestedScrollView) this.mRefreshableView).getScrollY() == 0;
    }

    protected boolean isReadyForPullUp() {
        if (((ElasticityNestedScrollView) this.mRefreshableView).getChildAt(0) == null) {
            return false;
        }
        if (((ElasticityNestedScrollView) this.mRefreshableView).canScrollVertically(1)) {
            return false;
        }
        return true;
    }
}
