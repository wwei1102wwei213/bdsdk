package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wei.wlib.R;


public class FooterLoadingLayout extends LoadingLayout {
    private TextView mHintView;
    private ProgressBar mProgressBar;
    public Button mReloadBtn;

    public FooterLoadingLayout(Context context) {
        super(context);
        init(context);
    }

    public FooterLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mProgressBar = (ProgressBar) findViewById(R.id.pull_to_load_footer_progressbar);
        this.mHintView = (TextView) findViewById(R.id.pull_to_load_footer_hint_textview);
        this.mReloadBtn = (Button) findViewById(R.id.pull_to_load_footer_btn_reload);
        setState(State.RESET);
    }

    protected View createLoadingView(Context context, AttributeSet attrs) {
        return LayoutInflater.from(context).inflate(R.layout.wlib_pull_to_load_footer, null);
    }

    public void setLastUpdatedLabel(CharSequence label) {
    }

    public int getContentSize() {
        View view = findViewById(R.id.pull_to_load_footer_content);
        if (view != null) {
            return view.getHeight();
        }
        return (int) (getResources().getDisplayMetrics().density * 40.0f);
    }

    protected void onStateChanged(State curState, State oldState) {
        this.mProgressBar.setVisibility(GONE);
        this.mReloadBtn.setVisibility(GONE);
        this.mHintView.setVisibility(GONE);
        super.onStateChanged(curState, oldState);
    }

    protected void onReset() {
        this.mHintView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    protected void onPullToRefresh() {
        this.mHintView.setVisibility(VISIBLE);
        this.mHintView.setText(R.string.pull_to_refresh_header_hint_normal2);
    }

    protected void onReleaseToRefresh() {
        this.mHintView.setVisibility(GONE);
        this.mHintView.setText(R.string.pull_to_refresh_header_hint_ready);
    }

    protected void onRefreshing() {
        this.mProgressBar.setVisibility(VISIBLE);
        this.mHintView.setVisibility(VISIBLE);
        this.mHintView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    protected void onNoMoreData() {
        this.mHintView.setVisibility(VISIBLE);
        this.mHintView.setText(R.string.pushmsg_center_no_more_msg);
    }

    protected void onNoData() {
        this.mHintView.setVisibility(GONE);
        this.mHintView.setText("");
    }

    protected void onNetWrok() {
        this.mReloadBtn.setVisibility(GONE);
    }
}
