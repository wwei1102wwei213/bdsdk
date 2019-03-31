package com.wei.wlib.elasticity.adapters;

import android.view.View;
import android.widget.HorizontalScrollView;

public class HorizontalScrollViewElasticityAdapter implements IElasticityAdapter {
    protected final HorizontalScrollView mView;

    public HorizontalScrollViewElasticityAdapter(HorizontalScrollView view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return !this.mView.canScrollHorizontally(-1);
    }

    public boolean isInAbsoluteEnd() {
        return !this.mView.canScrollHorizontally(1);
    }
}
