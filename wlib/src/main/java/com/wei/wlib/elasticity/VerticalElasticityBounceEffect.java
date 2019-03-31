package com.wei.wlib.elasticity;

import android.view.MotionEvent;
import android.view.View;

import com.wei.wlib.elasticity.adapters.IElasticityAdapter;


public class VerticalElasticityBounceEffect extends ElasticityBounceEffectBase {

    protected class AnimationAttributesVertical extends AnimationAttributes {
        private AnimationAttributesVertical() {
            this.mProperty = View.TRANSLATION_Y;
        }

        protected void init(View view) {
            this.mAbsOffset = VerticalElasticityBounceEffect.this.getViewOffset(view);
            this.mMaxOffset = (float) view.getHeight();
        }
    }

    protected class MotionAttributesVertical extends MotionAttributes {
        protected MotionAttributesVertical() {
        }

        public boolean init(View view, MotionEvent event) {
            boolean z = false;
            if (event.getHistorySize() == 0) {
                return false;
            }
            float dy = event.getY(0) - event.getHistoricalY(0, 0);
            if (Math.abs(event.getX(0) - event.getHistoricalX(0, 0)) > Math.abs(dy) || dy == 0.0f) {
                return false;
            }
            this.mAbsOffset = VerticalElasticityBounceEffect.this.getViewOffset(view);
            this.mDeltaOffset = dy;
            if (this.mDeltaOffset > 0.0f) {
                z = true;
            }
            this.mDir = z;
            return true;
        }
    }

    public VerticalElasticityBounceEffect(IElasticityAdapter viewAdapter) {
        this(viewAdapter, 3.0f, 1.0f, -2.0f, 1.2f);
    }

    public VerticalElasticityBounceEffect(IElasticityAdapter viewAdapter, float scaleFactor) {
        this(viewAdapter, 3.0f, 1.0f, -2.0f, scaleFactor);
    }

    public VerticalElasticityBounceEffect(IElasticityAdapter viewAdapter, float touchDragRatioFwd, float touchDragRatioBck, float decelerateFactor, float scaleFactor) {
        super(viewAdapter, decelerateFactor, scaleFactor, touchDragRatioFwd, touchDragRatioBck);
    }

    protected MotionAttributes createMotionAttributes() {
        return new MotionAttributesVertical();
    }

    protected AnimationAttributes createAnimationAttributes() {
        return new AnimationAttributesVertical();
    }

    protected void translateView(View view, boolean dir, float offset) {
        setViewOffset(view, offset);
        view.setPivotX(0.0f);
        if (dir) {
            view.setPivotY(0.0f);
        } else {
            view.setPivotY((float) view.getMeasuredHeight());
        }
        view.setScaleY(Math.min(getMaxScaleFactor(), 1.0f + (Math.abs(offset) / ((float) view.getWidth()))));
        view.postInvalidate();
    }

    protected void translateViewAndEvent(View view, boolean dir, float offset, MotionEvent event) {
        translateView(view, dir, offset);
        event.offsetLocation(offset - event.getY(0), 0.0f);
    }
}
