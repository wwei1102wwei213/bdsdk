package com.wei.wlib.elasticity;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.wei.wlib.elasticity.adapters.AbsListViewElasticityAdapter;
import com.wei.wlib.elasticity.adapters.HorizontalScrollViewElasticityAdapter;
import com.wei.wlib.elasticity.adapters.NestedScrollViewElasticityAdapter;
import com.wei.wlib.elasticity.adapters.RecyclerViewElasticityAdapter;
import com.wei.wlib.elasticity.adapters.ScrollViewElasticityAdapter;
import com.wei.wlib.elasticity.adapters.StaticElasticityAdapter;
import com.wei.wlib.elasticity.adapters.ViewPagerElasticityAdapter;


public class ElasticityHelper {
    public static IElasticity setUpOverScroll(RecyclerView recyclerView, ORIENTATION orientation) {
        return setUpOverScroll(recyclerView, orientation, 1.2f);
    }

    public static IElasticity setUpOverScroll(RecyclerView recyclerView, ORIENTATION orientation, float scaleFactor) {
        if (orientation == ORIENTATION.HORIZONTAL) {
            return new HorizontalElasticityBounceEffect(new RecyclerViewElasticityAdapter(recyclerView), scaleFactor);
        }
        if (orientation == ORIENTATION.VERTICAL) {
            return new VerticalElasticityBounceEffect(new RecyclerViewElasticityAdapter(recyclerView), scaleFactor);
        }
        throw new IllegalArgumentException("orientation");
    }

    public static IElasticity setUpOverScroll(ListView listView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new AbsListViewElasticityAdapter(listView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ListView listView) {
        return setUpOverScroll(listView, 1.2f);
    }

    public static IElasticity setUpOverScroll(GridView gridView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new AbsListViewElasticityAdapter(gridView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(GridView gridView) {
        return setUpOverScroll(gridView, 1.2f);
    }

    public static IElasticity setUpOverScroll(ScrollView scrollView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new ScrollViewElasticityAdapter(scrollView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ScrollView scrollView) {
        return null;
//        return setUpOverScroll(scrollView, 1.2f);
    }

    public static IElasticity setUpOverScroll(HorizontalScrollView scrollView, float scaleFactor) {
        return new HorizontalElasticityBounceEffect(new HorizontalScrollViewElasticityAdapter(scrollView), scaleFactor);
    }

    public static IElasticity setUpOverScroll(HorizontalScrollView scrollView) {
        return setUpOverScroll(scrollView, 1.2f);
    }

    public static IElasticity setUpOverScroll(NestedScrollView scrollView) {
        return setUpOverScroll(scrollView, 1.2f);
    }

    public static IElasticity setUpOverScroll(NestedScrollView scrollView, float scaleFactor) {
        return new VerticalElasticityBounceEffect(new NestedScrollViewElasticityAdapter(scrollView), scaleFactor);
    }

    public static IElasticity setUpStaticOverScroll(View view, ORIENTATION orientation) {
        return setUpStaticOverScroll(view, orientation, 1.2f);
    }

    private static IElasticity setUpStaticOverScroll(View view, ORIENTATION orientation, float scaleFactor) {
        if (orientation == ORIENTATION.HORIZONTAL) {
            return new HorizontalElasticityBounceEffect(new StaticElasticityAdapter(view), scaleFactor);
        }
        if (orientation == ORIENTATION.VERTICAL) {
            return new VerticalElasticityBounceEffect(new StaticElasticityAdapter(view), scaleFactor);
        }
        throw new IllegalArgumentException("orientation");
    }

    public static IElasticity setUpOverScroll(ViewPager viewPager, float scaleFactor) {
        return new HorizontalElasticityBounceEffect(new ViewPagerElasticityAdapter(viewPager), scaleFactor);
    }

    public static IElasticity setUpOverScroll(ViewPager viewPager) {
        return setUpOverScroll(viewPager, 1.2f);
    }
}
