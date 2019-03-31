package com.wei.wlib.elasticity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ListView;


public class ElasticityListView extends ListView{

    private boolean overScrollAnim = true;

    public void setOverScrollAnim(boolean overScrollAnim) {
        this.overScrollAnim = overScrollAnim;
    }

    public ElasticityListView(Context context) {
        super(context);
    }

    public ElasticityListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElasticityListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                   int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        final int overScrollMode = getOverScrollMode();
        final boolean canScrollHorizontal =
                computeHorizontalScrollRange() > computeHorizontalScrollExtent();
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent();
        final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == OVER_SCROLL_ALWAYS ||
                (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX + deltaX;
        if (!overScrollHorizontal) {
            maxOverScrollX = 0;
        }

        int newScrollY = scrollY + deltaY;
        if (!overScrollVertical) {
            maxOverScrollY = 0;
        }

        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

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
            if (overScrollAnim&&!isTouchEvent&&getOverBottomAnim()!=null) {
                startAnimation(getOverBottomAnim());
            }
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
            if (overScrollAnim&&!isTouchEvent&&getOverTopAnim()!=null) {
                startAnimation(getOverTopAnim());
            }
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return clampedX || clampedY;
    }

    private ScaleAnimation bottomAnim;
    private Animation getOverBottomAnim() {
        if (bottomAnim==null) {
            bottomAnim = new ScaleAnimation(1, 1, 1, ElasticityConfig.ELASTICITY_PIVOT_DITANCE,
                    Animation.RELATIVE_TO_SELF,1f,Animation.RELATIVE_TO_SELF,1f);
            bottomAnim.setDuration(ElasticityConfig.ELASTICITY_DURATION_BACK);
            bottomAnim.setInterpolator(new DecelerateInterpolator());
            bottomAnim.setRepeatCount(1);
            bottomAnim.setRepeatMode(Animation.REVERSE);
        }
        return bottomAnim;
    }

    private ScaleAnimation topAnim;
    private Animation getOverTopAnim() {
        if (topAnim==null) {
            topAnim = new ScaleAnimation(1, 1, 1, ElasticityConfig.ELASTICITY_PIVOT_DITANCE,
                    Animation.RELATIVE_TO_SELF,1f,Animation.RELATIVE_TO_SELF,0f);
            topAnim.setDuration(ElasticityConfig.ELASTICITY_DURATION_BACK);
            topAnim.setInterpolator(new DecelerateInterpolator());
            topAnim.setRepeatCount(1);
            topAnim.setRepeatMode(Animation.REVERSE);
        }
        return topAnim;
    }
}
