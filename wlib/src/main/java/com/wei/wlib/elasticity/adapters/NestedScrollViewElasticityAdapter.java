package com.wei.wlib.elasticity.adapters;

import android.support.v4.widget.NestedScrollView;
import android.view.View;

public class NestedScrollViewElasticityAdapter implements IElasticityAdapter {
    protected final NestedScrollView mView;

    public NestedScrollViewElasticityAdapter(NestedScrollView view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return !this.mView.canScrollVertically(-1);
    }

    public boolean isInAbsoluteEnd() {
        return !this.mView.canScrollVertically(1);
    }
}
