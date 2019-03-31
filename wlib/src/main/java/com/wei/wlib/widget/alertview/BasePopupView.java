package com.wei.wlib.widget.alertview;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.wei.wlib.R;


/**
 * Created by Administrator on 2018/4/26 0026.
 *
 * @author wwei
 */
public class BasePopupView {

    private final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM
    );

    private Context context;
    private ViewGroup contentContainer;
    private ViewGroup decorView;//activity的根View
    private ViewGroup rootView;//AlertView 的 根View

    private OnDismissListener onDismissListener;
    private boolean isDismissing;

    private Animation outAnim;
    private Animation inAnim;
    private int gravity = Gravity.BOTTOM;

    public BasePopupView( Context context){
        this.context = context;
        initViews();
        init();
        initEvents();
    }

    protected void initViews(){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        decorView = (ViewGroup) ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.wlib_alert_layout_alertview, decorView, false);
        rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
        params.setMargins(0,0,0,0);
        params.gravity = Gravity.TOP;
        contentContainer.setLayoutParams(params);
        gravity = Gravity.TOP;
        initPopupViews(layoutInflater);
    }

    protected void initPopupViews(LayoutInflater layoutInflater) {
        ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.wlib_alert_populview_base,contentContainer);

    }

    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }
    protected void initEvents() {
    }

    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        decorView.addView(view);
        contentContainer.startAnimation(inAnim);
    }
    /**
     * 添加这个View到Activity的根视图
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        onAttached(rootView);
    }
    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        View view = decorView.findViewById(R.id.outmost_container);
        return view != null;
    }
    public void dismiss() {
        if (isDismissing) {
            return;
        }
        //消失动画
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                decorView.post(new Runnable() {
                    @Override
                    public void run() {
                        //从activity根视图移除
                        decorView.removeView(rootView);
                        isDismissing = false;
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(BasePopupView.this);
                        }
                    }
                });
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        contentContainer.startAnimation(outAnim);
        isDismissing = true;
    }

    public Animation getInAnimation() {
        int res = AlertAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }

    public Animation getOutAnimation() {
        int res = AlertAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }

    public BasePopupView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    /**
     * 主要用于拓展View的时候有输入框，键盘弹出则设置MarginBottom往上顶，避免输入法挡住界面
     */
    public void setMarginBottom(int marginBottom){
        int margin_alert_left_right = context.getResources().getDimensionPixelSize(R.dimen.margin_alert_left_right);
        params.setMargins(margin_alert_left_right,0,margin_alert_left_right,marginBottom);
        contentContainer.setLayoutParams(params);
    }

    public BasePopupView setCancelable(boolean isCancelable) {
        View view = rootView.findViewById(R.id.outmost_container);
        if (isCancelable) {
            view.setOnTouchListener(onCancelableTouchListener);
        }
        else{
            view.setOnTouchListener(null);
        }
        return this;
    }
    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    private final View.OnTouchListener onCancelableTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss();
            }
            return false;
        }
    };

}
