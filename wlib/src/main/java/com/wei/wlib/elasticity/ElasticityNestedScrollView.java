package com.wei.wlib.elasticity;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class ElasticityNestedScrollView extends NestedScrollView {
    private ScaleAnimation bottomAnim;
    private boolean isTouchEvent = true;
    private boolean overScrollAnim = true;
    private ScaleAnimation topAnim;

    public void setTouchEvent(boolean touchEvent) {
        this.isTouchEvent = touchEvent;
    }

    public void setOverScrollAnim(boolean overScrollAnim) {
        this.overScrollAnim = overScrollAnim;
    }

    public ElasticityNestedScrollView(Context context) {
        super(context);
        /*setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);*/
    }

    public ElasticityNestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /*setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);*/
    }

    private OnScrollListener listener;
    public interface OnScrollListener {
        void onElasticityScrollChanged(int i, int i2, int i3, int i4);
    }

    public void setListener(OnScrollListener listener) {
        this.listener = listener;
    }

    public ElasticityNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.listener != null) {
            this.listener.onElasticityScrollChanged(l, t, oldl, oldt);
        }
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (!clampedY) {
            return;
        }
        if (scrollY == 0) {
            if (this.overScrollAnim && !this.isTouchEvent && getOverTopAnim() != null) {
                startAnimation(getOverTopAnim());
            }
        } else if (this.overScrollAnim && !this.isTouchEvent && getOverBottomAnim() != null) {
            startAnimation(getOverBottomAnim());
        }
    }

    private Animation getOverBottomAnim() {
        if (this.bottomAnim == null) {
            this.bottomAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.075f, 1, 1.0f, 1, 1.0f);
            this.bottomAnim.setDuration(180);
            this.bottomAnim.setInterpolator(new DecelerateInterpolator());
            this.bottomAnim.setRepeatCount(1);
            this.bottomAnim.setRepeatMode(2);
        }
        return this.bottomAnim;
    }

    private Animation getOverTopAnim() {
        if (this.topAnim == null) {
            this.topAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 1.075f, 1, 1.0f, 1, 0.0f);
            this.topAnim.setDuration(180);
            this.topAnim.setInterpolator(new DecelerateInterpolator());
            this.topAnim.setRepeatCount(1);
            this.topAnim.setRepeatMode(2);
        }
        return this.topAnim;
    }
}
