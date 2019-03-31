package com.wei.wlib.elasticity.adapters;

import android.view.View;
import android.widget.AbsListView;

public class AbsListViewElasticityAdapter implements IElasticityAdapter {
    protected final AbsListView mView;

    public AbsListViewElasticityAdapter(AbsListView view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return this.mView.getChildCount() > 0 && !canScrollListUp();
    }

    public boolean isInAbsoluteEnd() {
        return this.mView.getChildCount() > 0 && !canScrollListDown();
    }

    private boolean canScrollListUp() {
        int firstTop = this.mView.getChildAt(0).getTop();
        if (this.mView.getFirstVisiblePosition() > 0 || firstTop < this.mView.getListPaddingTop()) {
            return true;
        }
        return false;
    }

    private boolean canScrollListDown() {
        int childCount = this.mView.getChildCount();
        return this.mView.getFirstVisiblePosition() + childCount < this.mView.getCount() || this.mView.getChildAt(childCount - 1).getBottom() > this.mView.getHeight() - this.mView.getListPaddingBottom();
    }
}
