package com.wei.wlib.elasticity;

import android.content.Context;
import android.util.AttributeSet;

public class SmartScrollView extends ElasticityScrollView {
    private int DEFAULT_SY = 400;
    private boolean isScrolledToBottom = false;
    private boolean isScrolledToTop = true;
    private ISmartScrollChangedListener mSmartScrollChangedListener;
    private ISmartScrollOverListener mSmartScrollOverListener;

    public interface ISmartScrollChangedListener {
        void onScrolledToBottom();

        void onScrolledToTop();
    }

    public interface ISmartScrollOverListener {
        void overScrolled(boolean z, boolean z2);
    }

    public SmartScrollView(Context context) {
        super(context);
    }

    public SmartScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDefaultScrollY(int y) {
        this.DEFAULT_SY = y;
    }

    public void setScanScrollChangedListener(ISmartScrollChangedListener smartScrollChangedListener) {
        this.mSmartScrollChangedListener = smartScrollChangedListener;
    }

    public void setSmartScrollOverListener(ISmartScrollOverListener mSmartScrollOverListener) {
        this.mSmartScrollOverListener = mSmartScrollOverListener;
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (this.mSmartScrollChangedListener != null) {
            if (getScrollY() == 0 || getScrollY() <= this.DEFAULT_SY) {
                this.isScrolledToTop = true;
                this.isScrolledToBottom = false;
            } else {
                this.isScrolledToTop = false;
                this.isScrolledToBottom = true;
            }
            notifyScrollChangedListeners();
        }
    }

    private void notifyScrollChangedListeners() {
        if (this.isScrolledToTop) {
            if (this.mSmartScrollChangedListener != null) {
                this.mSmartScrollChangedListener.onScrolledToTop();
            }
        } else if (this.isScrolledToBottom && this.mSmartScrollChangedListener != null) {
            this.mSmartScrollChangedListener.onScrolledToBottom();
        }
    }

    public boolean isScrolledToTop() {
        return this.isScrolledToTop;
    }

    public boolean isScrolledToBottom() {
        return this.isScrolledToBottom;
    }
}
