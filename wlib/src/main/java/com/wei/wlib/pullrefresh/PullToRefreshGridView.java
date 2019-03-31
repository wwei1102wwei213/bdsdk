package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.GridView;


public class PullToRefreshGridView extends PullToRefreshBase<GridView> implements OnScrollListener {
    private LoadingLayout mFooterLayout;
    private GridView mGridView;
    private OnScrollListener mScrollListener;

    public PullToRefreshGridView(Context context) {
        this(context, null);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPullLoadEnabled(false);
    }

    protected GridView createRefreshableView(Context context, AttributeSet attrs) {
        GridView gridView = new GridView(context);
        this.mGridView = gridView;
        gridView.setOnScrollListener(this);
        return gridView;
    }

    public void setHasMoreData(boolean hasMoreData) {
        if (this.mFooterLayout != null && !hasMoreData) {
            this.mFooterLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
        }
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mScrollListener = l;
    }

    protected boolean isReadyForPullUp() {
        return isLastItemVisible();
    }

    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    protected void startLoading() {
        super.startLoading();
        if (this.mFooterLayout != null) {
            this.mFooterLayout.setState(ILoadingLayout.State.REFRESHING);
        }
    }

    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();
        if (this.mFooterLayout != null) {
            this.mFooterLayout.setState(ILoadingLayout.State.RESET);
        }
    }

    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        super.setScrollLoadEnabled(scrollLoadEnabled);
        if (scrollLoadEnabled) {
            if (this.mFooterLayout == null) {
                this.mFooterLayout = new FooterLoadingLayout(getContext());
            }
        } else if (this.mFooterLayout == null) {
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isScrollLoadEnabled() && hasMoreData() && ((scrollState == 0 || scrollState == 2) && isReadyForPullUp())) {
            startLoading();
        }
        if (this.mScrollListener != null) {
            this.mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mScrollListener != null) {
            this.mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private boolean hasMoreData() {
        if (this.mFooterLayout == null || this.mFooterLayout.getState() != ILoadingLayout.State.NO_MORE_DATA) {
            return true;
        }
        return false;
    }

    private boolean isFirstItemVisible() {
        Adapter adapter = this.mGridView.getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return true;
        }
        int mostTop;
        if (this.mGridView.getChildCount() > 0) {
            mostTop = this.mGridView.getChildAt(0).getTop();
        } else {
            mostTop = 0;
        }
        if (mostTop < 0) {
            return false;
        }
        return true;
    }

    private boolean isLastItemVisible() {
        Adapter adapter = this.mGridView.getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return true;
        }
        int lastItemPosition = adapter.getCount() - 1;
        int lastVisiblePosition = this.mGridView.getLastVisiblePosition();
        if (lastVisiblePosition >= lastItemPosition - 1) {
            View lastVisibleChild = this.mGridView.getChildAt(Math.min(lastVisiblePosition - this.mGridView.getFirstVisiblePosition(), this.mGridView.getChildCount() - 1));
            if (lastVisibleChild != null) {
                if (lastVisibleChild.getBottom() > this.mGridView.getBottom()) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
