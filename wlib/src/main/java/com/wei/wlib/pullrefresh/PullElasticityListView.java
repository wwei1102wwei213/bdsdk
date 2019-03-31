package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;

import com.wei.wlib.elasticity.ElasticityListView;


public class PullElasticityListView extends PullToRefreshBase<ElasticityListView> implements AbsListView.OnScrollListener{

    /**
     * ListView
     */
    private ElasticityListView mListView;
    /**
     * 用于滑到底部自动加载的Footer
     */
    private LoadingLayout mLoadMoreFooterLayout;

    private AbsListView.OnScrollListener mScrollListener;

    private OnLoadingListener mLoadingListener;

    public interface OnLoadingListener {
        void onPullUpLoading();
    }

    public void setLoadingListener(OnLoadingListener mLoadingListener) {
        this.mLoadingListener = mLoadingListener;
    }

    public PullElasticityListView(Context context) {
        this(context, null);
    }

    public PullElasticityListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullElasticityListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPullLoadEnabled(false);
    }

    @Override
    protected ElasticityListView createRefreshableView(Context context, AttributeSet attrs) {
        ElasticityListView listView = new ElasticityListView(context);
        mListView = listView;
        listView.setOnScrollListener(this);
        return listView;
    }

    /**
     * 设置是否有更多数据的标志
     *
     * @param hasMoreData true表示还有更多的数据，false表示没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        if (!hasMoreData) {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
            }

            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.NO_MORE_DATA);
            }
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
            }
            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.RESET);
            }
        }
    }

    /**
     * 设置没有数据
     *
     * @param noData true 没有数据 ，false 有数据
     */
    public void setNoData(boolean noData) {
        if (noData) {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_DATA);
            }

            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.NO_DATA);
            }
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
            }
            LoadingLayout footerLoadingLayout = getFooterLoadingLayout();
            if (null != footerLoadingLayout) {
                footerLoadingLayout.setState(ILoadingLayout.State.RESET);
            }
        }
    }

    /**
     * 设置滑动的监听器
     *
     * @param l 监听器
     */
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    public boolean isReadyForPullUp() {
        return isLastItemVisible1();
    }

    @Override
    protected boolean isReadyForPullDown() {
        return isFirstItemVisible();
    }

    @Override
    protected void startLoading() {
        super.startLoading();

        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(ILoadingLayout.State.REFRESHING);
        }
    }

    @Override
    public void onPullUpRefreshComplete() {
        super.onPullUpRefreshComplete();

        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(ILoadingLayout.State.RESET);
        }
    }

    @Override
    public void setScrollLoadEnabled(boolean scrollLoadEnabled) {
        if (isScrollLoadEnabled() == scrollLoadEnabled) {
            return;
        }

        super.setScrollLoadEnabled(scrollLoadEnabled);

        if (scrollLoadEnabled) {
            // 设置Footer
            if (null == mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
                mListView.addFooterView(mLoadMoreFooterLayout, null, false);
            }
            mLoadMoreFooterLayout.show(true);
        } else {
            if (null != mLoadMoreFooterLayout) {
                mLoadMoreFooterLayout.show(false);
            }
        }
    }

    @Override
    public LoadingLayout getFooterLoadingLayout() {
        if (isScrollLoadEnabled()) {
            return mLoadMoreFooterLayout;
        }

        return super.getFooterLoadingLayout();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (isScrollLoadEnabled() && hasMoreData()) {
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                    || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                if (isReadyForPullUp()) {
                    startLoading();
                }
            }
        }
        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
        if (this.mLoadingListener != null && isReadyForPullUp() && hasMoreData()) {
            this.mLoadingListener.onPullUpLoading();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public boolean isListViewReachBottomEdge(final AbsListView listView) {
        boolean result = false;
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(
                    listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            result = (listView.getHeight() >= bottomChildView.getBottom());
        }
        return result;
    }

    public void scrollLoadError() {
        super.onPullUpRefreshComplete();

        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(ILoadingLayout.State.NO_NETWORK);
        }
    }

    /**
     * 表示是否还有更多数据
     *
     * @return true表示还有更多数据
     */
    public boolean hasMoreData() {
        if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == ILoadingLayout.State.NO_MORE_DATA)) {
            return false;
        }

        return true;
    }

    /**
     * 判断第一个child是否完全显示出来
     *
     * @return true完全显示出来，否则false
     */
    private boolean isFirstItemVisible() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        int mostTop = (mListView.getChildCount() > 0) ? mListView.getChildAt(0).getTop() : 0;
        if (mostTop >= 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断最后一个child是否显示出来
     *
     * @return true显示出来，否则false
     */
    private boolean isLastItemVisible1() {
        final Adapter adapter = mListView.getAdapter();

        if (null == adapter || adapter.isEmpty()) {
            return true;
        }

        final int lastItemPosition = adapter.getCount() - 1;
        final int lastVisiblePosition = mListView.getLastVisiblePosition();

        return lastVisiblePosition == lastItemPosition;
    }

    public void setOnPullUpReloadListener(OnClickListener listener) {
        FooterLoadingLayout footer = (FooterLoadingLayout) mLoadMoreFooterLayout;
        if (footer.mReloadBtn != null) footer.mReloadBtn.setOnClickListener(listener);
    }
}
