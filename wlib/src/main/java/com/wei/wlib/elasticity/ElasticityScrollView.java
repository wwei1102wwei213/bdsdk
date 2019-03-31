package com.wei.wlib.elasticity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.ScaleAnimation;
import android.widget.ScrollView;

public class ElasticityScrollView extends ScrollView {
    private ScaleAnimation bottomAnim;
    private OnScrollListener listener;
    private boolean overScrollAnim = true;
    private ScaleAnimation topAnim;

    public interface OnScrollListener {
        void onElasticityScrollChanged(int i, int i2, int i3, int i4);
    }

    public ElasticityScrollView(Context context) {
        super(context);
    }

    public ElasticityScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElasticityScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOverScrollAnim(boolean overScrollAnim) {
        this.overScrollAnim = overScrollAnim;
    }

    public void setElasticityScrollListener(OnScrollListener listener) {
        this.listener = listener;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.listener != null) {
            this.listener.onElasticityScrollChanged(l, t, oldl, oldt);
        }
    }

    /*protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int overScrollMode = getOverScrollMode();
        boolean canScrollHorizontal = computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        boolean canScrollVertical = computeVerticalScrollRange() > computeVerticalScrollExtent();
        boolean overScrollHorizontal = overScrollMode == 0 || (overScrollMode == 1 && canScrollHorizontal);
        boolean overScrollVertical = overScrollMode == 0 || (overScrollMode == 1 && canScrollVertical);
        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }
        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }
        int left = -maxOverScrollX;
        int right = maxOverScrollX + scrollRangeX;
        int top = -maxOverScrollY;
        int bottom = maxOverScrollY + scrollRangeY;
        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }
        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
            if (!(!this.overScrollAnim || isTouchEvent || getOverBottomAnim() == null)) {
                startAnimation(getOverBottomAnim());
            }
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
            if (!(!this.overScrollAnim || isTouchEvent || getOverTopAnim() == null)) {
                startAnimation(getOverTopAnim());
            }
        }
        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);
        if (clampedX || clampedY) {
            return true;
        }
        return false;
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
    }*/
}
