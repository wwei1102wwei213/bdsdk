package com.wei.wlib.widget.alertview;

import android.view.Gravity;

import com.wei.wlib.R;


/**
 * Created by Sai on 15/8/9.
 */
public class AlertAnimateUtil {
    private static final int INVALID = -1;
    /**
     * Get default animation resource when not defined by the user
     *
     * @param gravity       the gravity of the dialog
     * @param isInAnimation determine if is in or out animation. true when is is
     * @return the id of the animation resource
     */
    static int getAnimationResource(int gravity, boolean isInAnimation) {
        switch (gravity) {
            case Gravity.BOTTOM:
                return isInAnimation ? R.anim.wlib_alert_slide_in_bottom : R.anim.wlib_alert_slide_out_bottom;
            case Gravity.CENTER:
                return isInAnimation ? R.anim.wlib_alert_fade_in_center : R.anim.wlib_alert_fade_out_center;
            case Gravity.TOP:
                return isInAnimation ? R.anim.wlib_alert_activity_in_from_right : R.anim.wlib_alert_translate_down;
        }
        return INVALID;
    }
}
