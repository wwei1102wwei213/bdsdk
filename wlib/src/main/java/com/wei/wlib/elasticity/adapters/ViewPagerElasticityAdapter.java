package com.wei.wlib.elasticity.adapters;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

public class ViewPagerElasticityAdapter implements IElasticityAdapter, OnPageChangeListener {
    protected int mLastPagerPosition = 0;
    protected float mLastPagerScrollOffset;
    protected final ViewPager mViewPager;

    public ViewPagerElasticityAdapter(ViewPager viewPager) {
        this.mViewPager = viewPager;
        this.mViewPager.addOnPageChangeListener(this);
        this.mLastPagerPosition = this.mViewPager.getCurrentItem();
        this.mLastPagerScrollOffset = 0.0f;
    }

    public View getView() {
        return this.mViewPager;
    }

    public boolean isInAbsoluteStart() {
        return this.mLastPagerPosition == 0 && this.mLastPagerScrollOffset == 0.0f;
    }

    public boolean isInAbsoluteEnd() {
        return this.mLastPagerPosition == this.mViewPager.getAdapter().getCount() + -1 && this.mLastPagerScrollOffset == 0.0f;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.mLastPagerPosition = position;
        this.mLastPagerScrollOffset = positionOffset;
    }

    public void onPageSelected(int position) {
    }

    public void onPageScrollStateChanged(int state) {
    }
}
