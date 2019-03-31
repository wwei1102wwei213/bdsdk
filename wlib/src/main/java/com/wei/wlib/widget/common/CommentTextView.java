package com.wei.wlib.widget.common;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.wei.wlib.util.WLibLog;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2018-08-17.
 */
public class CommentTextView extends AppCompatTextView {

    public CommentTextView(Context context) {
        super(context);
    }

    public CommentTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CommentTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*public void setTextComment(final MomentComment itemBean, final CommentPartClickListener callback, int position){
        try {
            String name = itemBean.getSender().getNick().trim();
            String content = itemBean.getContent().trim();
            SpannableString tSS = new SpannableString(name + "："+content);
            tSS.setSpan(new ClickPacketSpan(callback, itemBean, position), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tSS.setSpan(new StyleSpan(Typeface.NORMAL), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tSS.setSpan(new ForegroundColorSpan(Color.parseColor("#3C82AA")), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为洋红色
            setText(tSS);
            setMovementMethod(LinkMovementMethod.getInstance());
            setHighlightColor(Color.TRANSPARENT);
        } catch (Exception e){
            WLibLog.e(e);
        }
    }*/

    static class ClickPacketSpan extends ClickableSpan {

        private WeakReference<Context> reference;
        private CommentPartClickListener callback;
        private int position;
        /*private MomentComment itemBean;

        public ClickPacketSpan(CommentPartClickListener callback, MomentComment itemBean, int position) {
            this.callback = callback;
            this.itemBean = itemBean;
            this.position = position;
        }*/

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            try {
                // 设置文本的颜色
                ds.setColor(Color.parseColor("#483d8b"));
                // 超链接形式的下划线，false 表示不显示下划线，true表示显示下划线
                ds.setUnderlineText(false);
            } catch (Exception e){
                WLibLog.e(e);
            }
        }

        @Override
        public void onClick(View widget) {
            if (callback!=null){
//                callback.onCommentPartClickListener(itemBean, position);
            }
        }
    }

    public interface CommentPartClickListener{
//        void onCommentPartClickListener(MomentComment itemBean, int position);
    }

}
