package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public abstract class LoadingLayout extends FrameLayout implements ILoadingLayout {
    private View mContainer;
    private State mCurState;
    private State mPreState;

    protected abstract View createLoadingView(Context context, AttributeSet attributeSet);

    public abstract int getContentSize();

    public LoadingLayout(Context context) {
        this(context, null);
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurState = State.NONE;
        this.mPreState = State.NONE;
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        this.mContainer = createLoadingView(context, attrs);
        if (this.mContainer == null) {
            throw new NullPointerException("Loading view can not be null.");
        }
        addView(this.mContainer, new LayoutParams(-1, -2));
    }

    public void show(boolean show) {
        boolean z;
        int i = 0;
        if (getVisibility() == VISIBLE) {
            z = true;
        } else {
            z = false;
        }
        if (show != z) {
            ViewGroup.LayoutParams params = this.mContainer.getLayoutParams();
            if (params != null) {
                if (show) {
                    params.height = -2;
                } else {
                    params.height = 0;
                }
                requestLayout();
                if (!show) {
                    i = 4;
                }
                setVisibility(i);
            }
        }
    }

    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    public void setLastUpdatedLabel(CharSequence label) {
    }

    public void setLoadingDrawable(Drawable drawable) {
    }

    public void setPullLabel(CharSequence pullLabel) {
    }

    public void setRefreshingLabel(CharSequence refreshingLabel) {
    }

    public void setReleaseLabel(CharSequence releaseLabel) {
    }

    public void setState(State state) {
        if (this.mCurState != state) {
            this.mPreState = this.mCurState;
            this.mCurState = state;
            onStateChanged(state, this.mPreState);
        }
    }

    public State getState() {
        return this.mCurState;
    }

    public void onPull(float scale) {
    }

    protected State getPreState() {
        return this.mPreState;
    }

    protected void onStateChanged(State curState, State oldState) {
        switch (curState) {
            case RESET:
                onReset();
                return;
            case RELEASE_TO_REFRESH:
                onReleaseToRefresh();
                return;
            case PULL_TO_REFRESH:
                onPullToRefresh();
                return;
            case REFRESHING:
                onRefreshing();
                return;
            case NO_MORE_DATA:
                onNoMoreData();
                return;
            case NO_NETWORK:
                onNetWrok();
                return;
            case NO_DATA:
                onNoData();
                return;
            default:
                return;
        }
    }

    protected void onReset() {
    }

    protected void onPullToRefresh() {
    }

    protected void onReleaseToRefresh() {
    }

    protected void onRefreshing() {
    }

    protected void onNoMoreData() {
    }

    protected void onNetWrok() {
    }

    protected void onNoData() {
    }
}
