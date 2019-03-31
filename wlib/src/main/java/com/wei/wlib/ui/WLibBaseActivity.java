package com.wei.wlib.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.wei.wlib.util.WLibLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by Administrator on 2017/8/29 0029.
 *
 * App Activity父类
 *
 * @author wwei
 */
public class WLibBaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showToast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    protected void startActivity(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    protected void setBackViews(int... ids) {
        if (ids != null) {
            for (int id : ids) {
                View view = findViewById(id);
                if (view != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            }
        }
    }

    //设置状态栏为透明
    public void setStatusBarTranslucent(){
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4以上
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    //设置全屏样式
    public void setFullScreen(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //获取状态栏高度
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //设置状态栏
    public void initStatusBar(View v){
        try {
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT){
                ViewGroup.LayoutParams params=v.getLayoutParams();
                params.height=getStatusBarHeight();
                v.setLayoutParams(params);
            }
        }catch (Exception e){
            WLibLog.e(e, "initStatusBar");
        }
    }

    //退出动画设置 @enterAnim 下个页面的开始动画 @exitAnim 这个页面的退出动画
    protected void finishForAnim(int enterAnim, int exitAnim){
        finish();
        overridePendingTransition(enterAnim,exitAnim);
    }


    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MobclickAgent.onResume(this); // 基础指标统计，不能遗漏
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MobclickAgent.onPause(this); // 基础指标统计，不能遗漏
    }

    //设置皮肤相关UI
    protected void setSkinView(){}

    protected boolean mBaseExit = false;
    @Override
    protected void onDestroy() {
        mBaseExit = true;
        super.onDestroy();
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    protected void setStatusBarFontIconDark(boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // android6.0+系统
            // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
            if (dark) {
                getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else if (isMIUI()) {
            // 小米MIUI
            try {
                Window window = getWindow();
                Class clazz = getWindow().getClass();
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                int darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {    //状态栏亮色且黑色字体
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
                } else {       //清除黑色字体
                    extraFlagField.invoke(window, 0, darkModeFlag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isFlyme()) {
            // 魅族FlymeUI
            try {
                Window window = getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    private boolean isFlyme() {
        try {
            final Method method = Build.class.getMethod("hasSmartBar");
            return method != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

}
