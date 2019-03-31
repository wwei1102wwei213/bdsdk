package com.wei.wlib.elasticity;

import android.view.MotionEvent;
import android.view.View;

import com.wei.wlib.elasticity.adapters.IElasticityAdapter;


public class HorizontalElasticityBounceEffect extends ElasticityBounceEffectBase {

    protected class AnimationAttributesHorizontal extends AnimationAttributes {
        private AnimationAttributesHorizontal() {
            this.mProperty = View.TRANSLATION_X;
        }

        protected void init(View view) {
            this.mAbsOffset = HorizontalElasticityBounceEffect.this.getViewOffset(view);
            this.mMaxOffset = (float) view.getWidth();
        }
    }

    protected class MotionAttributesHorizontal extends MotionAttributes {
        protected MotionAttributesHorizontal() {
        }

        public boolean init(View view, MotionEvent event) {
            boolean z = false;
            if (event.getHistorySize() == 0) {
                return false;
            }
            float dx = event.getX(0) - event.getHistoricalX(0, 0);
            if (Math.abs(dx) < Math.abs(event.getY(0) - event.getHistoricalY(0, 0)) || dx == 0.0f) {
                return false;
            }
            this.mAbsOffset = HorizontalElasticityBounceEffect.this.getViewOffset(view);
            this.mDeltaOffset = dx;
            if (this.mDeltaOffset > 0.0f) {
                z = true;
            }
            this.mDir = z;
            return true;
        }
    }

    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter) {
        this(viewAdapter, 3.0f, 1.0f, -2.0f, 1.2f);
    }

    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter, float scaleFactor) {
        this(viewAdapter, 3.0f, 1.0f, -2.0f, scaleFactor);
    }

    public HorizontalElasticityBounceEffect(IElasticityAdapter viewAdapter, float touchDragRatioFwd, float touchDragRatioBck, float decelerateFactor, float scaleFactor) {
        super(viewAdapter, decelerateFactor, scaleFactor, touchDragRatioFwd, touchDragRatioBck);
    }

    protected MotionAttributes createMotionAttributes() {
        return new MotionAttributesHorizontal();
    }

    protected AnimationAttributes createAnimationAttributes() {
        return new AnimationAttributesHorizontal();
    }

    protected void translateView(View view, boolean dir, float offset) {
        setViewOffset(view, offset);
        view.setPivotY(0.0f);
        if (dir) {
            view.setPivotX(0.0f);
        } else {
            view.setPivotX((float) view.getMeasuredWidth());
        }
        view.setScaleX(Math.min(getMaxScaleFactor(), 1.0f + (Math.abs(offset) / ((float) view.getWidth()))));
        view.postInvalidate();
    }

    protected void translateViewAndEvent(View view, boolean dir, float offset, MotionEvent event) {
        translateView(view, dir, offset);
        event.offsetLocation(offset - event.getX(0), 0.0f);
    }
}
