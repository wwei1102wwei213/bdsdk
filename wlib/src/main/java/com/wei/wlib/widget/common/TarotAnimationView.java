package com.wei.wlib.widget.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.wei.wlib.R;
import com.wei.wlib.util.WLibLog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 旋转动画控件
 */
public class TarotAnimationView extends View {

    private static final int TAROT_CARD_COUNT = 22;

    private static final long FIRST_ANIM_DURING = 300;
    private static final long SECOND_ANIM_DURING = 3000;
    private static final long THIRD_ANIM_DURING = 300;

    private AccelerateDecelerateInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private Point mAnimationCenter = new Point();

    private int mMaxRadius;
    private int mCurrentRadius;
    private int mBitmapLeftOffset;
    private double mSweepAngleRadians = Math.PI * 2 / TAROT_CARD_COUNT;
    private double mCurrentAngleOffset;
    private int mCurrentAnimIndex = -1;
    private int mCurrentAnimRadius;

    private Paint mPaint = new Paint();
    private Bitmap mTarotCardImage;

    private AtomicBoolean mIsAnimating = new AtomicBoolean(false);
    private OnCompleteListener mOnCompleteListener;

    public TarotAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        mTarotCardImage = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher, options);
        mBitmapLeftOffset = mTarotCardImage.getWidth() / 2;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimation();
            }
        });
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        mOnCompleteListener = onCompleteListener;
    }

    public void startAnimation() {
        if (mIsAnimating.compareAndSet(false, true)) {final long startTime = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        long currentTime = System.currentTimeMillis();
                        long span = currentTime - startTime;
                        if (span < FIRST_ANIM_DURING) {
                            float ratio = (float) span / FIRST_ANIM_DURING;
                            mCurrentRadius = (int) (mInterpolator.getInterpolation(ratio) * mMaxRadius);
                            postInvalidate();
                        } else {
                            mCurrentRadius = mMaxRadius;
                            span -= FIRST_ANIM_DURING;
                            if (span < SECOND_ANIM_DURING) {
                                float ratio = (float) span / SECOND_ANIM_DURING;
                                mCurrentAngleOffset = mInterpolator.getInterpolation(ratio) * Math.PI * 2;
                                postInvalidate();
                            } else {
                                mCurrentAngleOffset = 0;
                                span -= SECOND_ANIM_DURING;
                                if (span < THIRD_ANIM_DURING * TAROT_CARD_COUNT) {
                                    float ratio = (float) (span % THIRD_ANIM_DURING) / THIRD_ANIM_DURING;
                                    mCurrentAnimIndex = (int) (span / THIRD_ANIM_DURING);
                                    mCurrentAnimRadius = (int) (mInterpolator.getInterpolation(1 - ratio) * mMaxRadius);
                                    postInvalidate();
                                } else {
                                    break;
                                }
                            }
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            WLibLog.e(e);
                        }
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        WLibLog.e(e);
                    }
                    if (mOnCompleteListener != null) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mOnCompleteListener.onComplete();
                            }
                        });
                    }
                    mIsAnimating.set(false);
                }
            }).start();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAnimationCenter.set(w / 2, (h - mTarotCardImage.getHeight()) / 2);
        mMaxRadius = Math.min(mAnimationCenter.x, mAnimationCenter.y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < TAROT_CARD_COUNT; i++) {
            double radians = mSweepAngleRadians * i + Math.PI / 2 + mCurrentAngleOffset;
            int radius = 0;
            if (i == mCurrentAnimIndex) {
                radius = mCurrentAnimRadius;
            } else if (i > mCurrentAnimIndex || mCurrentAnimIndex == -1) {
                radius = mCurrentRadius;
            }
            int left = (int) (Math.cos(radians) * radius - mBitmapLeftOffset);
            int top = (int) (Math.sin(radians) * radius);
            canvas.drawBitmap(mTarotCardImage, left + mAnimationCenter.x, top + mAnimationCenter.y, mPaint);
        }
    }

    public interface OnCompleteListener {

        void onComplete();
    }
}
