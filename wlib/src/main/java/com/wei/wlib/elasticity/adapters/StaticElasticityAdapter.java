package com.wei.wlib.elasticity.adapters;

import android.view.View;

public class StaticElasticityAdapter implements IElasticityAdapter {
    protected final View mView;

    public StaticElasticityAdapter(View view) {
        this.mView = view;
    }

    public View getView() {
        return this.mView;
    }

    public boolean isInAbsoluteStart() {
        return true;
    }

    public boolean isInAbsoluteEnd() {
        return true;
    }
}
