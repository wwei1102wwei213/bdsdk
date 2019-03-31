package com.wei.wlib.elasticity;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.wei.wlib.R;
import com.wei.wlib.elasticity.adapters.IElasticityAdapter;


public abstract class ElasticityBounceEffectBase implements IElasticity, OnTouchListener {
    public static final float DEFAULT_DECELERATE_FACTOR = -2.0f;
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK = 1.0f;
    public static final float DEFAULT_TOUCH_DRAG_MOVE_RATIO_FWD = 3.0f;
    protected static final int MAX_BOUNCE_BACK_DURATION_MS = 1000;
    public static final float MAX_SCALE_FACTOR = 1.2f;
    protected static final int MIN_BOUNCE_BACK_DURATION_MS = 300;
    protected final BounceBackState mBounceBackState;
    protected IDecoratorState mCurrentState;
    protected final IdleState mIdleState;
    protected final OverScrollStartAttributes mOverScrollStartAttr = new OverScrollStartAttributes();
    protected final OverScrollingState mOverScrollingState;
    protected IElasticityStateListener mStateListener = new ElasticityListenerStubs.ElasticityStateListenerStub();
    protected IElasticityUpdateListener mUpdateListener = new ElasticityListenerStubs.ElasticityUpdateListenerStub();
    protected float mVelocity;
    protected final IElasticityAdapter mViewAdapter;
    private float maxScaleFactor = 1.2f;

    protected static abstract class AnimationAttributes {
        public float mAbsOffset;
        public float mMaxOffset;
        public Property<View, Float> mProperty;

        protected abstract void init(View view);

        protected AnimationAttributes() {
        }
    }

    protected interface IDecoratorState {
        int getStateId();

        void handleEntryTransition(IDecoratorState iDecoratorState);

        boolean handleMoveTouchEvent(MotionEvent motionEvent);

        boolean handleUpOrCancelTouchEvent(MotionEvent motionEvent);
    }

    protected class BounceBackState implements IDecoratorState, AnimatorListener, AnimatorUpdateListener {
        protected final AnimationAttributes mAnimAttributes;
        protected final Interpolator mBounceBackInterpolator = new BounceInterpolator();
        protected final float mDecelerateFactor;
        protected final float mDoubleDecelerateFactor;

        public BounceBackState(float decelerateFactor) {
            this.mDecelerateFactor = decelerateFactor;
            this.mDoubleDecelerateFactor = 2.0f * decelerateFactor;
            this.mAnimAttributes = ElasticityBounceEffectBase.this.createAnimationAttributes();
        }

        public int getStateId() {
            return 3;
        }

        public void handleEntryTransition(IDecoratorState fromState) {
            ElasticityBounceEffectBase.this.mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), getStateId());
            Animator bounceBackAnim = createAnimator();
            bounceBackAnim.addListener(this);
            bounceBackAnim.start();
        }

        public boolean handleMoveTouchEvent(MotionEvent event) {
            return true;
        }

        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            return true;
        }

        public void onAnimationEnd(Animator animation) {
            ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mIdleState);
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            ElasticityBounceEffectBase.this.translateView(ElasticityBounceEffectBase.this.mViewAdapter.getView(), ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir, ((Float) animation.getAnimatedValue()).floatValue());
            ElasticityBounceEffectBase.this.mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, 3, ((Float) animation.getAnimatedValue()).floatValue());
        }

        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationCancel(Animator animation) {
            ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mIdleState);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        protected Animator createAnimator() {
            this.mAnimAttributes.init(ElasticityBounceEffectBase.this.mViewAdapter.getView());
            if (ElasticityBounceEffectBase.this.mVelocity == 0.0f || ((ElasticityBounceEffectBase.this.mVelocity < 0.0f && ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir) || (ElasticityBounceEffectBase.this.mVelocity > 0.0f && !ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir))) {
                return createBounceBackAnimator(this.mAnimAttributes.mAbsOffset, 0.0f);
            }
            float slowdownDuration = (-ElasticityBounceEffectBase.this.mVelocity) / this.mDecelerateFactor;
            if (slowdownDuration < 0.0f) {
                slowdownDuration = 0.0f;
            }
            float slowdownEndOffset = this.mAnimAttributes.mAbsOffset + (((-ElasticityBounceEffectBase.this.mVelocity) * ElasticityBounceEffectBase.this.mVelocity) / this.mDoubleDecelerateFactor);
            ValueAnimator slowdownAnim = createSlowdownAnimator((int) slowdownDuration, this.mAnimAttributes.mAbsOffset, slowdownEndOffset);
            ValueAnimator bounceBackAnim = createBounceBackAnimator(slowdownEndOffset, 0.0f);
            AnimatorSet wholeAnim = new AnimatorSet();
            wholeAnim.playSequentially(new Animator[]{slowdownAnim, bounceBackAnim});
            return wholeAnim;
        }

        protected ValueAnimator createSlowdownAnimator(int slowdownDuration, float slowdownStartOffset, float slowdownEndOffset) {
            ValueAnimator slowdownAnim = ValueAnimator.ofFloat(new float[]{slowdownStartOffset, slowdownEndOffset});
            slowdownAnim.setDuration((long) slowdownDuration);
            slowdownAnim.setInterpolator(new DecelerateInterpolator());
            slowdownAnim.addUpdateListener(this);
            return slowdownAnim;
        }

        protected ValueAnimator createBounceBackAnimator(float startOffset, float endOffset) {
            float bounceBackDuration = (Math.abs(startOffset) / this.mAnimAttributes.mMaxOffset) * 1000.0f;
            ValueAnimator bounceBackAnim = ValueAnimator.ofFloat(new float[]{startOffset, endOffset});
            bounceBackAnim.setDuration((long) Math.max((int) bounceBackDuration, ElasticityBounceEffectBase.MIN_BOUNCE_BACK_DURATION_MS));
            bounceBackAnim.addUpdateListener(this);
            return bounceBackAnim;
        }
    }

    protected class IdleState implements IDecoratorState {
        final MotionAttributes mMoveAttr;

        public IdleState() {
            this.mMoveAttr = ElasticityBounceEffectBase.this.createMotionAttributes();
        }

        public int getStateId() {
            return 0;
        }

        public boolean handleMoveTouchEvent(MotionEvent event) {
            if (!this.mMoveAttr.init(ElasticityBounceEffectBase.this.mViewAdapter.getView(), event)) {
                return false;
            }
            if ((!ElasticityBounceEffectBase.this.mViewAdapter.isInAbsoluteStart() || !this.mMoveAttr.mDir) && (!ElasticityBounceEffectBase.this.mViewAdapter.isInAbsoluteEnd() || this.mMoveAttr.mDir)) {
                return false;
            }
            ElasticityBounceEffectBase.this.mOverScrollStartAttr.mPointerId = event.getPointerId(0);
            ElasticityBounceEffectBase.this.mOverScrollStartAttr.mAbsOffset = this.mMoveAttr.mAbsOffset;
            ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir = this.mMoveAttr.mDir;
            ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mOverScrollingState);
            return ElasticityBounceEffectBase.this.mOverScrollingState.handleMoveTouchEvent(event);
        }

        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            return false;
        }

        public void handleEntryTransition(IDecoratorState fromState) {
            ElasticityBounceEffectBase.this.mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), getStateId());
        }
    }

    protected static abstract class MotionAttributes {
        public float mAbsOffset;
        public float mDeltaOffset;
        public boolean mDir;

        protected abstract boolean init(View view, MotionEvent motionEvent);

        protected MotionAttributes() {
        }
    }

    protected static class OverScrollStartAttributes {
        protected float mAbsOffset;
        protected boolean mDir;
        protected int mPointerId;

        protected OverScrollStartAttributes() {
        }
    }

    protected class OverScrollingState implements IDecoratorState {
        int mCurrDragState;
        final MotionAttributes mMoveAttr;
        protected final float mTouchDragRatioBck;
        protected final float mTouchDragRatioFwd;

        public OverScrollingState(float touchDragRatioFwd, float touchDragRatioBck) {
            this.mMoveAttr = ElasticityBounceEffectBase.this.createMotionAttributes();
            this.mTouchDragRatioFwd = touchDragRatioFwd;
            this.mTouchDragRatioBck = touchDragRatioBck;
        }

        public int getStateId() {
            return this.mCurrDragState;
        }

        public boolean handleMoveTouchEvent(MotionEvent event) {
            if (ElasticityBounceEffectBase.this.mOverScrollStartAttr.mPointerId != event.getPointerId(0)) {
                ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mBounceBackState);
            } else {
                View view = ElasticityBounceEffectBase.this.mViewAdapter.getView();
                if (this.mMoveAttr.init(view, event)) {
                    float deltaOffset = this.mMoveAttr.mDeltaOffset / (this.mMoveAttr.mDir == ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir ? this.mTouchDragRatioFwd : this.mTouchDragRatioBck);
                    float newOffset = this.mMoveAttr.mAbsOffset + deltaOffset;
                    if ((!ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir || this.mMoveAttr.mDir || newOffset > ElasticityBounceEffectBase.this.mOverScrollStartAttr.mAbsOffset) && (ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir || !this.mMoveAttr.mDir || newOffset < ElasticityBounceEffectBase.this.mOverScrollStartAttr.mAbsOffset)) {
                        if (view.getParent() != null) {
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        long dt = event.getEventTime() - event.getHistoricalEventTime(0);
                        if (dt > 0) {
                            ElasticityBounceEffectBase.this.mVelocity = deltaOffset / ((float) dt);
                        }
                        ElasticityBounceEffectBase.this.translateView(view, ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir, newOffset);
                        ElasticityBounceEffectBase.this.mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, this.mCurrDragState, newOffset);
                    } else {
                        ElasticityBounceEffectBase.this.translateViewAndEvent(view, ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir, ElasticityBounceEffectBase.this.mOverScrollStartAttr.mAbsOffset, event);
                        ElasticityBounceEffectBase.this.mUpdateListener.onOverScrollUpdate(ElasticityBounceEffectBase.this, this.mCurrDragState, 0.0f);
                        ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mIdleState);
                    }
                }
            }
            return true;
        }

        public boolean handleUpOrCancelTouchEvent(MotionEvent event) {
            ElasticityBounceEffectBase.this.issueStateTransition(ElasticityBounceEffectBase.this.mBounceBackState);
            return true;
        }

        public void handleEntryTransition(IDecoratorState fromState) {
            this.mCurrDragState = ElasticityBounceEffectBase.this.mOverScrollStartAttr.mDir ? 1 : 2;
            ElasticityBounceEffectBase.this.mStateListener.onOverScrollStateChange(ElasticityBounceEffectBase.this, fromState.getStateId(), getStateId());
        }
    }

    protected abstract AnimationAttributes createAnimationAttributes();

    protected abstract MotionAttributes createMotionAttributes();

    protected abstract void translateView(View view, boolean z, float f);

    protected abstract void translateViewAndEvent(View view, boolean z, float f, MotionEvent motionEvent);

    public ElasticityBounceEffectBase(IElasticityAdapter viewAdapter, float decelerateFactor, float scaleFactor, float touchDragRatioFwd, float touchDragRatioBck) {
        this.mViewAdapter = viewAdapter;
        this.mIdleState = new IdleState();
        this.mOverScrollingState = new OverScrollingState(touchDragRatioFwd, touchDragRatioBck);
        this.mBounceBackState = new BounceBackState(decelerateFactor);
        this.mCurrentState = this.mIdleState;
        this.maxScaleFactor = scaleFactor;
        attach();
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case 1:
            case 3:
                setTouchEvent(false);
                return this.mCurrentState.handleUpOrCancelTouchEvent(event);
            case 2:
                setTouchEvent(true);
                return this.mCurrentState.handleMoveTouchEvent(event);
            default:
                return false;
        }
    }

    private void setTouchEvent(boolean isTouch) {
        if (getView() == null) {
            return;
        }
        if (getView() instanceof ElasticityNestedScrollView) {
            ((ElasticityNestedScrollView) getView()).setTouchEvent(isTouch);
        } else if (getView() instanceof ElasticityRecyclerView) {
            ((ElasticityRecyclerView) getView()).setTouchEvent(isTouch);
        }
    }

    public void setOverScrollStateListener(IElasticityStateListener listener) {
        if (listener == null) {
            listener = new ElasticityListenerStubs.ElasticityStateListenerStub();
        }
        this.mStateListener = listener;
    }

    public void setOverScrollUpdateListener(IElasticityUpdateListener listener) {
        if (listener == null) {
            listener = new ElasticityListenerStubs.ElasticityUpdateListenerStub();
        }
        this.mUpdateListener = listener;
    }

    public int getCurrentState() {
        return this.mCurrentState.getStateId();
    }

    public View getView() {
        return this.mViewAdapter.getView();
    }

    protected void issueStateTransition(IDecoratorState state) {
        IDecoratorState oldState = this.mCurrentState;
        this.mCurrentState = state;
        this.mCurrentState.handleEntryTransition(oldState);
    }

    protected void attach() {
        getView().setOnTouchListener(this);
        getView().setOverScrollMode(2);
    }

    public void detach() {
        getView().setOnTouchListener(null);
        getView().setOverScrollMode(0);
    }

    protected float getViewOffset(View view) {
        if (view.getTag(R.id.offsetValue) != null) {
            return ((Float) view.getTag(R.id.offsetValue)).floatValue();
        }
        return 0.0f;
    }

    protected void setViewOffset(View view, float offset) {
        view.setTag(R.id.offsetValue, Float.valueOf(offset));
    }

    protected float getMaxScaleFactor() {
        return this.maxScaleFactor;
    }
}
