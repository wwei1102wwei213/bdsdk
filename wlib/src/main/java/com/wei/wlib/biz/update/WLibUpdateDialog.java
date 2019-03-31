package com.wei.wlib.biz.update;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wei.wlib.R;


/**
 * APK升级窗口
 */
public class WLibUpdateDialog extends Dialog {

    private ProgressBar pb;
    private TextView tv;
    private TextView tv_cancel;
//    private ImageView close;
    private View.OnClickListener listener;

    public WLibUpdateDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public TextView getButtonText(){
        return tv;
    }

    public void setButtonText(String text) {
        if (tv == null) {
            tv = (TextView) this.findViewById(R.id.progress_tv);
            if (listener != null) {
                tv.setOnClickListener(listener);
            }
        }
        tv.setText(text);
    }

    public void setClose() {
        /*if (close == null) {
            close = (ImageView) this.findViewById(R.id.update_close);
            close.setVisibility(View.VISIBLE);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WLibUpdateDialog.this.dismiss();
                }
            });
        }*/
        setCancelView();
    }

    public void setCancelView() {
        if (tv_cancel == null) {
            tv_cancel = (TextView) this.findViewById(R.id.cancel_tv);
            tv_cancel.setVisibility(View.VISIBLE);
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WLibUpdateDialog.this.dismiss();
                }
            });
        }
    }

    public void setProgress(int progress) {
        if (pb == null) {
            pb = (ProgressBar) this.findViewById(R.id.progressbar);
        }
        pb.setProgress(progress);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setButtonClickable(boolean flag) {
        if (tv != null) {
            tv.setClickable(flag);
        }
        if (tv_cancel!=null) {
            tv_cancel.setClickable(flag);
        }
    }

    public static class Builder {

        private Context context;
        private WLibUpdateDialog dialog;
        private CharSequence title;
        private CharSequence content;
        private View.OnClickListener listener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setText(CharSequence title, CharSequence content) {
            this.title = title;
            this.content = content;
            return this;
        }

        public Builder setOnClickListener(View.OnClickListener listener) {
            this.listener = listener;
            return this;
        }

        public WLibUpdateDialog create() {

            dialog = new WLibUpdateDialog(context, R.style.wlib_custom_dialog_update);
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.wlib_dialog_update, null);
            initView(layout);
            dialog.setContentView(layout);
            dialog.setOnClickListener(listener);
            return dialog;
        }

        private void initView(View layout) {

            TextView tvTitle = (TextView) layout.findViewById(R.id.update_title);
            if (title != null) {
                tvTitle.setText(title);
            } else {
                tvTitle.setVisibility(View.GONE);
            }

            TextView tvContent = (TextView) layout.findViewById(R.id.update_content);
            if (content != null) {
                tvContent.setText(content);
            } else {
                tvContent.setVisibility(View.GONE);
            }

        }

    }
}
