package com.wei.wlib.widget.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wei.wlib.R;

import java.lang.ref.WeakReference;


/**
 * 伸缩TextView
 */
public class CollapsibleTextView extends LinearLayout implements View.OnClickListener {
    private static final int COLLAPSIBLE_STATE_NONE = 0;// 不显示
    private static final int COLLAPSIBLE_STATE_SHRINKUP = 1;// 显示收起
    private static final int COLLAPSIBLE_STATE_SPREAD = 2;// 显示全文
    private int mState = COLLAPSIBLE_STATE_SPREAD;
    private String COLLAPSIBLE_STATE_SHRINKUP_TEXT;
    private String COLLAPSIBLE_STATE_SPREAD_TEXT;
    private TextView mText;
    private Context mContext;

    /**
     * @return Returns the mText.
     */
    public TextView getmText() {
        return mText;
    }

    public int getmState() {
        return mState;
    }

    public void setmState(int mState) {
        this.mState = mState;
    }

    private TextView mTextTip;
    private changeState changeStateCallBack;
    private boolean isNeedLayout;
    private int maxLineCount = 8;

    private void handleMsg(){
        try {
            if (mText.getLineCount() <= maxLineCount) {
                // 行数不足不做处理
                mState = COLLAPSIBLE_STATE_NONE;
                mText.setMaxLines(Integer.MAX_VALUE);
                mTextTip.setVisibility(View.GONE);
            } else {
                switch (mState) {
                    case COLLAPSIBLE_STATE_SPREAD:
                        // 全文状态
                        mText.setMaxLines(maxLineCount);
                        mTextTip.setVisibility(View.VISIBLE);
                        mTextTip.setText(COLLAPSIBLE_STATE_SPREAD_TEXT);
                        break;
                    case COLLAPSIBLE_STATE_SHRINKUP:
                        // 收起状态
                        mText.setMaxLines(Integer.MAX_VALUE);
                        mTextTip.setVisibility(View.VISIBLE);
                        mTextTip.setText(COLLAPSIBLE_STATE_SHRINKUP_TEXT);
                        break;
                    default:
                        // 除非发生不可知状态，一般不会执行到这个
                        mState = COLLAPSIBLE_STATE_NONE;
                        mText.setMaxLines(Integer.MAX_VALUE);
                        mTextTip.setVisibility(View.GONE);
                        break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<CollapsibleTextView> weak;
        private MyHandler(CollapsibleTextView view) {
            weak = new WeakReference<>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            if (weak!=null&&weak.get()!=null) {
                weak.get().handleMsg();
            }
        }
    }

    public CollapsibleTextView(Context context) {
        this(context, null);
        this.mContext = context;
        initView();
    }

    public CollapsibleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    @SuppressLint("NewApi")
    public CollapsibleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView();
    }

    private void initView() {
        COLLAPSIBLE_STATE_SHRINKUP_TEXT = mContext.getResources().getString(R.string.wlib_collapse_text);
        COLLAPSIBLE_STATE_SPREAD_TEXT = mContext.getResources().getString(R.string.wlib_full_text);
        View view = inflate(getContext(), R.layout.wlib_collapsible_textview, this);
        view.setPadding(0, -1, 0, 0);
        mText = (TextView) view.findViewById(R.id.desc_tv);
        mTextTip = (TextView) view.findViewById(R.id.desc_op_tv);
        mTextTip.setOnClickListener(this);
    }

    /**
     * 设置文本
     *
     * @param charSequence
     * @param bufferType
     */
    public final void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        isNeedLayout = true;
        mState = COLLAPSIBLE_STATE_SPREAD;
        mText.setText(charSequence, bufferType);
    }

    /**
     * 设置文本
     *
     * @param charSequence
     */
    public final void setText(CharSequence charSequence) {
        isNeedLayout = true;
        mState = COLLAPSIBLE_STATE_SPREAD;
        mText.setText(charSequence);
    }

    @Override
    public void onClick(View v) {
        isNeedLayout = true;
        if (mState == COLLAPSIBLE_STATE_SPREAD) {
            // 如果是全文状态，就改成收起状态
            mState = COLLAPSIBLE_STATE_SHRINKUP;
            requestLayout();
        } else if (mState == COLLAPSIBLE_STATE_SHRINKUP) {
            // 如果是收起状态，就改成全文状态
            mState = COLLAPSIBLE_STATE_SPREAD;
            requestLayout();
        }
        if (null != changeStateCallBack) {
            changeStateCallBack.changeFlag(v);
        }
    }

    private MyHandler handler;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        try {
            if (isNeedLayout) {
                isNeedLayout = false;
                if (handler==null) handler = new MyHandler(this);
                handler.sendMessage(Message.obtain());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getMaxLineCount() {
        return maxLineCount;
    }

    public void setMaxLineCount(int maxLineCount) {
        this.maxLineCount = maxLineCount;
    }

    public changeState getChangeStateCallBack() {
        return changeStateCallBack;
    }

    public void setChangeStateCallBack(changeState changeStateCallBack) {
        this.changeStateCallBack = changeStateCallBack;
    }

    public interface changeState {
        public void changeFlag(View v);
    }
}