package com.wei.wlib.pullrefresh;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wei.wlib.R;


public class RotateLoadingLayout extends LoadingLayout {
    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();
    static final int ROTATION_ANIMATION_DURATION = 1200;
    private ImageView mArrowImageView;
    private RelativeLayout mHeaderContainer;
    private TextView mHeaderTimeView;
    private TextView mHeaderTimeViewTitle;
    private TextView mHintTextView;
    private Animation mRotateAnimation;

    public RotateLoadingLayout(Context context) {
        super(context);
        init(context);
    }

    public RotateLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mHeaderContainer = (RelativeLayout) findViewById(R.id.pull_to_refresh_header_content);
        this.mArrowImageView = (ImageView) findViewById(R.id.pull_to_refresh_header_arrow);
        this.mHintTextView = (TextView) findViewById(R.id.pull_to_refresh_header_hint_textview);
        this.mHeaderTimeView = (TextView) findViewById(R.id.pull_to_refresh_header_time);
        this.mHeaderTimeViewTitle = (TextView) findViewById(R.id.pull_to_refresh_last_update_time_text);
        this.mArrowImageView.setScaleType(ScaleType.CENTER);
        this.mArrowImageView.setImageResource(R.mipmap.wlib_default_ptr_rotate);
        this.mRotateAnimation = new RotateAnimation(0.0f, 720.0f, 1, 0.5f, 1, 0.5f);
        this.mRotateAnimation.setFillAfter(true);
        this.mRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        this.mRotateAnimation.setDuration(1200);
        this.mRotateAnimation.setRepeatCount(-1);
        this.mRotateAnimation.setRepeatMode(1);
    }

    protected View createLoadingView(Context context, AttributeSet attrs) {
        return LayoutInflater.from(context).inflate(R.layout.wlib_pull_to_refresh_header2, null);
    }

    public void setLastUpdatedLabel(CharSequence label) {
        this.mHeaderTimeViewTitle.setVisibility(TextUtils.isEmpty(label) ? INVISIBLE : VISIBLE);
        this.mHeaderTimeView.setText(label);
    }

    public int getContentSize() {
        if (this.mHeaderContainer != null) {
            return this.mHeaderContainer.getHeight();
        }
        return (int) (getResources().getDisplayMetrics().density * 60.0f);
    }

    protected void onStateChanged(State curState, State oldState) {
        super.onStateChanged(curState, oldState);
    }

    protected void onReset() {
        resetRotation();
        this.mHintTextView.setText(R.string.pull_to_refresh_header_hint_normal);
    }

    protected void onReleaseToRefresh() {
        this.mHintTextView.setText(R.string.pull_to_refresh_header_hint_ready);
    }

    protected void onPullToRefresh() {
        this.mHintTextView.setText(R.string.pull_to_refresh_header_hint_normal);
    }

    protected void onRefreshing() {
        resetRotation();
        this.mArrowImageView.startAnimation(this.mRotateAnimation);
        this.mHintTextView.setText(R.string.pull_to_refresh_header_hint_loading);
    }

    public void onPull(float scale) {
        this.mArrowImageView.setRotation(scale * 180.0f);
    }

    private void resetRotation() {
        this.mArrowImageView.clearAnimation();
        this.mArrowImageView.setRotation(0.0f);
    }
}
