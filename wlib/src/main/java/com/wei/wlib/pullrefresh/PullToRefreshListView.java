package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListView;


public class PullToRefreshListView extends PullToRefreshBase<ListView> implements OnScrollListener {
    private ListView mListView;
    private FooterLoadingLayout mLoadMoreFooterLayout;
    private OnScrollListener mScrollListener;

    public PullToRefreshListView(Context context) {
        this(context, null);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPullLoadEnabled(false);
    }

    protected ListView createRefreshableView(Context context, AttributeSet attrs) {
        ListView listView = new ListView(context);
        this.mListView = listView;
        listView.setOnScrollListener(this);
        return listView;
    }

    public void setHasMoreData(boolean hasMoreData) {
        LoadingLayout footerLoadingLayout;
        if (hasMoreData) {
            if (this.mLoadMoreFooterLayout != null) {
                this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
            }
            footerLoadingLayout = getFooterLoadingLayout();
            if (footerLoadingLayout != null) {
                footerLoadingLayout.setState(ILoadingLayout.State.RESET);
                return;
            }
            return;
        }
        if (this.mLoadMoreFooterLayout != null) {
            this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
        }
        footerLoadingLayout = getFooterLoadingLayout();
        if (footerLoadingLayout != null) {
            footerLoadingLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
        }
    }

    public void setNoData(boolean noData) {
        LoadingLayout footerLoadingLayout;
        if (noData) {
            if (this.mLoadMoreFooterLayout != null) {
                this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_DATA);
            }
            footerLoadingLayout = getFooterLoadingLayout();
            if (footerLoadingLayout != null) {
                footerLoadingLayout.setState(ILoadingLayout.State.NO_DATA);
                return;
            }
            return;
        }
        if (this.mLoadMoreFooterLayout != null) {
            this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
        }
        footerLoadingLayout = getFooterLoadingLayout();
        if (footerLoadingLayout != null) {
            footerLoadingLayout.setState(ILoadingLayout.State.RESET);
        }
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mScrollListener = l;
    }

    public boolean isReadyForPullUp() {
        return isLastItemVisible1();
    }

    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    protected void startLoading() {
        super.startLoading();
        if (this.mLoadMoreFooterLayout != null) {
            this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.REFRESHING);
        }
    }

    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();
        if (this.mLoadMoreFooterLayout != null) {
            this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
        }
    }

    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        if (isScrollLoadEnabled() != scrollLoadEnabled) {
            super.setScrollLoadEnabled(scrollLoadEnabled);
            if (scrollLoadEnabled) {
                if (this.mLoadMoreFooterLayout == null) {
                    this.mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
                    this.mListView.addFooterView(this.mLoadMoreFooterLayout, null, false);
                }
                this.mLoadMoreFooterLayout.show(true);
            } else if (this.mLoadMoreFooterLayout != null) {
                this.mLoadMoreFooterLayout.show(false);
            }
        }
    }

    public LoadingLayout getFooterLoadingLayout() {
        if (isScrollLoadEnabled()) {
            return this.mLoadMoreFooterLayout;
        }
        return super.getFooterLoadingLayout();
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

    public boolean isListViewReachBottomEdge(AbsListView listView) {
        if (listView.getLastVisiblePosition() != listView.getCount() - 1) {
            return false;
        }
        return listView.getHeight() >= listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition()).getBottom();
    }

    public void scrollLoadError() {
        super.onPullUpRefreshComplete();
        if (this.mLoadMoreFooterLayout != null) {
            this.mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_NETWORK);
        }
    }

    public boolean hasMoreData() {
        if (this.mLoadMoreFooterLayout == null || this.mLoadMoreFooterLayout.getState() != ILoadingLayout.State.NO_MORE_DATA) {
            return true;
        }
        return false;
    }

    private boolean isFirstItemVisible() {
        Adapter adapter = this.mListView.getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return true;
        }
        int mostTop;
        if (this.mListView.getChildCount() > 0) {
            mostTop = this.mListView.getChildAt(0).getTop();
        } else {
            mostTop = 0;
        }
        if (mostTop < 0) {
            return false;
        }
        return true;
    }

    private boolean isLastItemVisible() {
        Adapter adapter = this.mListView.getAdapter();
        if (adapter == null || adapter.isEmpty()) {
            return true;
        }
        int lastItemPosition = adapter.getCount() - 1;
        int lastVisiblePosition = this.mListView.getLastVisiblePosition();
        if (lastVisiblePosition >= lastItemPosition - 1) {
            View lastVisibleChild = this.mListView.getChildAt(Math.min(lastVisiblePosition - this.mListView.getFirstVisiblePosition(), this.mListView.getChildCount() - 1));
            if (lastVisibleChild != null) {
                if (lastVisibleChild.getBottom() > this.mListView.getBottom()) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean isLastItemVisible1() {
        Adapter adapter = this.mListView.getAdapter();
        if (adapter == null || adapter.isEmpty() || this.mListView.getLastVisiblePosition() == adapter.getCount() - 1) {
            return true;
        }
        return false;
    }

    public void setOnPullUpReloadListener(OnClickListener listener) {
        FooterLoadingLayout footer = this.mLoadMoreFooterLayout;
        if (footer.mReloadBtn != null) {
            footer.mReloadBtn.setOnClickListener(listener);
        }
    }
}
