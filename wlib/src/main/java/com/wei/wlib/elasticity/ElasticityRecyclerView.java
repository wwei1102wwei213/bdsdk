package com.wei.wlib.elasticity;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

public class ElasticityRecyclerView extends RecyclerView {
    private final String TAG = getClass().getSimpleName();
    private ScaleAnimation bottomAnim;
    private boolean isInfoTouch = false;
    private boolean isTouchEvent = false;
    private boolean overScrollAnim = true;
    private ScaleAnimation topAnim;

    public void setTouchEvent(boolean touchEvent) {
        this.isTouchEvent = touchEvent;
    }

    public void setOverScrollAnim(boolean overScrollAnim) {
        this.overScrollAnim = overScrollAnim;
    }

    public ElasticityRecyclerView(Context context) {
        super(context);
        init();
    }

    public ElasticityRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElasticityRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
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
