package com.wei.wlib.elasticity.adapters;

import android.view.View;
import android.widget.ScrollView;

public class ScrollViewElasticityAdapter implements IElasticityAdapter {
    protected final ScrollView mView;

    public ScrollViewElasticityAdapter(ScrollView view) {
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
