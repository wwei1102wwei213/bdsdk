package com.wei.wlib.elasticity.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.wei.wlib.pullrefresh.LoadingLayout;

import java.util.Collections;
import java.util.List;

public abstract class BaseRecyclerAdapter<VH extends ViewHolder> extends Adapter<VH> {
    protected View customHeaderView = null;
    protected View customLoadMoreView = null;
    private boolean isFooterEnable = true;
    private boolean removeFooter = false;

    protected class VIEW_TYPES {
        public static final int FOOTER = -1;
        public static final int HEADER = -3;
        public static final int NORMAL = -4;

        protected VIEW_TYPES() {
        }
    }

    public abstract int getAdapterItemCount();

    public abstract VH getViewHolder(View view);

    public abstract void onBindViewHolder(VH vh, int i, boolean z);

    public abstract VH onCreateViewHolder(ViewGroup viewGroup, int i, boolean z);

    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        showFooter(this.customLoadMoreView, false);
        if (viewType == -1) {
            removeViewFromParent(this.customLoadMoreView);
            return getViewHolder(this.customLoadMoreView);
        } else if (viewType != -3) {
            return onCreateViewHolder(parent, viewType, true);
        } else {
            removeViewFromParent(this.customHeaderView);
            return getViewHolder(this.customHeaderView);
        }
    }

    private void showFooter(View footerview, boolean show) {
        if (this.isFooterEnable && footerview != null && (footerview instanceof LoadingLayout)) {
            LoadingLayout footerCallBack = (LoadingLayout) footerview;
            if (show) {
                if (!footerCallBack.isShow()) {
                    footerCallBack.show(true);
                }
            } else if (getAdapterItemCount() == 0 && footerCallBack.isShow()) {
                footerCallBack.show(false);
            } else if (getAdapterItemCount() != 0 && !footerCallBack.isShow()) {
                footerCallBack.show(show);
            }
        }
    }

    public void addFooterView() {
        if (this.removeFooter) {
            notifyItemInserted(getItemCount());
            this.removeFooter = false;
            showFooter(this.customLoadMoreView, true);
        }
    }

    public boolean isFooterShowing() {
        return !this.removeFooter;
    }

    public void removeFooterView() {
        if (!this.removeFooter) {
            notifyItemRemoved(getItemCount() - 1);
            this.removeFooter = true;
        }
    }

    public boolean isEmpty() {
        return getAdapterItemCount() == 0;
    }

    public final void onBindViewHolder(VH holder, int position) {
        int start = getStart();
        if (!isHeader(position) && !isFooter(position)) {
            onBindViewHolder(holder, position - start, true);
        }
    }

    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && (lp instanceof StaggeredGridLayoutManager.LayoutParams)) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            boolean z = isFooter(position) || isHeader(position);
            p.setFullSpan(z);
        }
    }

    public void setCustomLoadMoreView(View footerView) {
        if (footerView instanceof LoadingLayout) {
            this.customLoadMoreView = footerView;
            removeViewFromParent(this.customLoadMoreView);
            showFooter(this.customLoadMoreView, false);
            notifyDataSetChanged();
            return;
        }
        throw new RuntimeException("footerView must be implementes LoadingLayout!");
    }

    public void setHeaderView(View headerView, RecyclerView recyclerView) {
        if (recyclerView != null) {
            removeViewFromParent(headerView);
            this.customHeaderView = headerView;
            notifyDataSetChanged();
        }
    }

    public View setHeaderView(@LayoutRes int id, RecyclerView recyclerView) {
        if (recyclerView == null) {
            return null;
        }
        Context context = recyclerView.getContext();
        if (context.getResources().getResourceTypeName(id).contains("layout")) {
            this.customHeaderView = LayoutInflater.from(context).inflate(id, new FrameLayout(recyclerView.getContext()), false);
            notifyDataSetChanged();
            return this.customHeaderView;
        }
        throw new RuntimeException(context.getResources().getResourceName(id) + " is a illegal layoutid , please check your layout id first !");
    }

    public boolean isFooter(int position) {
        return this.customLoadMoreView != null && position >= getAdapterItemCount() + getStart();
    }

    public boolean isHeader(int position) {
        return getStart() > 0 && position == 0;
    }

    public View getCustomLoadMoreView() {
        return this.customLoadMoreView;
    }

    public final int getItemViewType(int position) {
        if (isHeader(position)) {
            return -3;
        }
        if (isFooter(position)) {
            return -1;
        }
        if (getStart() > 0) {
            position--;
        }
        return getAdapterItemViewType(position);
    }

    public int getAdapterItemViewType(int position) {
        return -4;
    }

    public int getStart() {
        return this.customHeaderView == null ? 0 : 1;
    }

    public final int getItemCount() {
        int count = getAdapterItemCount() + getStart();
        if (this.customLoadMoreView == null || this.removeFooter) {
            return count;
        }
        return count + 1;
    }

    public void swapPositions(List<?> list, int from, int to) {
        Collections.swap(list, from, to);
    }

    public void insideEnableFooter(boolean enable) {
        this.isFooterEnable = enable;
    }

    public <T> void insert(List<T> list, T object, int position) {
        list.add(position, object);
        notifyItemInserted(getStart() + position);
    }

    public void remove(List<?> list, int position) {
        if (list.size() > 0) {
            notifyItemRemoved(getStart() + position);
        }
    }

    public void clear(List<?> list) {
        int start = getStart();
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(start, size);
    }

    private void removeViewFromParent(View view) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
    }
}
