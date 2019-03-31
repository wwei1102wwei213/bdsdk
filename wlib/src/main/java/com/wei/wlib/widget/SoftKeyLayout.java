package com.wei.wlib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class SoftKeyLayout extends LinearLayout {

    public SoftKeyLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public SoftKeyLayout(Context context) {
        super(context);
    }

    private OnSoftKeyboardListener onSoftKeyboardListener;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (onSoftKeyboardListener != null) {
            final int newSpec = MeasureSpec.getSize(heightMeasureSpec);
            final int oldSpec = getMeasuredHeight();

            if (oldSpec > newSpec){
                onSoftKeyboardListener.onShown();
            } else {
                WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                int height = wm.getDefaultDisplay().getHeight();
                if((newSpec - oldSpec) > height/3)
                    onSoftKeyboardListener.onHidden();
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setOnSoftKeyboardListener(final OnSoftKeyboardListener listener) {
        this.onSoftKeyboardListener = listener;
    }


    public interface OnSoftKeyboardListener {
        void onShown();
        void onHidden();
    }
}