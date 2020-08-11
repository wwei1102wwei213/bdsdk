package com.wei.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;
import com.baidu.aip.api.FaceApi;
import com.baidu.aip.db.DBManager;
import com.baidu.aip.entity.ARGBImg;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.TexturePreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceTracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;
import com.huashi.otg.sdk.Test;
import com.wei.ai.biz.Factory;
import com.wei.ai.biz.HttpFlag;
import com.wei.ai.db.CheckDataBean;
import com.wei.ai.db.DBHelper;
import com.wei.ai.db.InfoBean;
import com.wei.ai.utils.FileUtils;
import com.wei.ai.utils.GlobalFaceTypeModel;
import com.wei.ai.utils.MyUtils;
import com.wei.ai.utils.SPLongUtils;
import com.wei.ai.utils.WLibPermissionsBiz;
import com.wei.wlib.http.WLibHttpListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements WLibHttpListener{

    private static final String TAG = "BD_AI";
    private WLibPermissionsBiz biz;
    private HsOtgApi api;
    private String filepath;
    private String picPath;
    private MyHandler mHandler;
    private byte[] photoFeature = new byte[2048];
    private long MAX_ONCE_CHECK_TIME;
    private int CHECK_SIZE;
    private int MATCH_SCORE;
    private String signNum = "";
    private String baseHost = "";
    private long onceStartTime;
    private boolean isMatching = false;
    private int CHECK_SUCCESS_COUNT = 0;
    private int CHECK_FAIL_COUNT = 0;
    private boolean isExit = false;
    private static final int MSG_HIDE_LOADING = 254;
    private static final int MSG_CHECK_RESULT_SUCCESS = 255;
    private static final int MSG_CHECK_RESULT_FAIL = 256;
    private MyTimeTask mTask;

    private TextView tv_status;
    private TexturePreviewView previewView;
    private TextureView textureView;
    private TextView tipTv, matchScoreTv;
    private ImageView  iv_photo;
    private Button btn_setting, btn_search, btn_close;
    private TextView tv_name, tv_sex, tv_num;
    private TextView tv_birthday, tv_date, tv_check_time, tv_address;
    private TextView tv_time, tv_gw, tv_hg, tv_sb;
    private TextView tv_result;
    private TextView tv_loading_hint;
    private ImageView iv_loading;
    private ProgressBar pb_loading;
    private View v_big_loading, v_person_result;
    private Button btn_person_fail, btn_person_success;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFilePath();
        setSPData();
        mHandler = new MyHandler(this);
        faceDetectManager = new FaceDetectManager(getApplicationContext());
        m_Auto = true;
        initViews();
        mTask = new MyTimeTask();
        mHandler.postDelayed(mTask, 30000);
        if (MyUtils.IsNetWorkEnable(this)) {
            initPermissions();
        } else {
            showNetworkHint();
        }
    }

    /**
     * 无网络提示
     */
    private void showNetworkHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请设置网络");
        builder.setPositiveButton("已连接网络", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (MyUtils.IsNetWorkEnable(MainActivity.this)) {
                    initPermissions();
                } else {
                    showNetworkHint();
                }
            }
        });
        builder.create().show();
    }

    /**
     * 设置UI
     */
    private void initViews() {

        tv_loading_hint = findViewById(R.id.tv_loading_hint);
        iv_loading = findViewById(R.id.iv_loading);
        pb_loading = findViewById(R.id.pb_loading);
        v_big_loading = findViewById(R.id.v_big_loading);
        v_person_result = findViewById(R.id.v_person_result);
        btn_person_fail = findViewById(R.id.btn_person_fail);
        btn_person_success = findViewById(R.id.btn_person_success);
        tv_status = findViewById(R.id.tv_status);
        previewView = findViewById(R.id.preview_view);
        textureView = findViewById(R.id.texture_view);
        tipTv =  findViewById(R.id.tip);
        matchScoreTv = (TextView) findViewById(R.id.match_score_tv);
        iv_photo = findViewById(R.id.iv_photo);
        btn_search =findViewById(R.id.btn_search);
        btn_setting =findViewById(R.id.btn_setting);
        btn_close =findViewById(R.id.btn_close);
        tv_name = findViewById(R.id.tv_name);
        tv_sex = findViewById(R.id.tv_sex);
        tv_num = findViewById(R.id.tv_num);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_date = findViewById(R.id.tv_date);
        tv_check_time = findViewById(R.id.tv_check_time);
        tv_address = findViewById(R.id.tv_address);
        tv_result = findViewById(R.id.tv_result);
        tv_time = findViewById(R.id.tv_time);
        tv_gw = findViewById(R.id.tv_gw);
        tv_hg = findViewById(R.id.tv_hg);
        tv_sb = findViewById(R.id.tv_sb);

        tv_gw.setText(TextUtils.isEmpty(signNum)?"未设置":signNum);
        SimpleDateFormat ms = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        tv_time.setText(ms.format(new Date()));

        v_big_loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}
        });
        v_person_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}
        });
        btn_person_fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFail();
            }
        });
        btn_person_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSuccess();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toExit();
            }
        });
    }

    @Override
    public void handleResp(Object formatData, int flag, Object tag, String response, String hint) {
    }

    @Override
    public void handleLoading(int flag, Object tag, boolean isShow) {}

    @Override
    public void handleError(int flag, Object tag, int errorType, String response, String hint) {}

    @Override
    public void handleAfter(int flag, Object tag) {}


    /**
     * 设置监听
     */
    private void initListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                if (!isMatching) return;
                checkFace(retCode, infos, frame);
                showFrame(frame, infos);
            }
        });
    }

    /**
     * 权限检测
     */
    private void initPermissions () {
        biz = new WLibPermissionsBiz(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 124, new WLibPermissionsBiz.RequestPermissionsListener() {
            @Override
            public void RequestComplete(boolean isOk) {
                if (isOk) init();
            }
        });
        biz.toCheckPermission();
    }

    /**
     * 初始化
     */
    private void init() {
        try {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("正在初始化算法");
            dialog.show();
            initMeition();
            PreferencesUtil.initPrefs(this);
            // 使用人脸1：n时使用
            DBManager.getInstance().init(this);
            livnessTypeTip();
            FaceSDKManager.getInstance().init(this);
            FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
                @Override
                public void initStart() {}
                @Override
                public void initSuccess() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setMessage("正在初始化设备");
                            mHandler.sendEmptyMessageDelayed(MSG_HIDE_LOADING, 5000);
                        }
                    });
                }
                @Override
                public void initFail(int errorCode, String msg) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            tv_status.setText("初始化失败");
                        }
                    });
                }
            });
        } catch (Exception e){
            e.printStackTrace();

        }
    }

    /**
     * 初始化读卡设备
     * */
    private void initMeition() {
        try {
            copy(MainActivity.this, "base.dat", "base.dat", filepath);
            copy(MainActivity.this, "license.lic", "license.lic", filepath);
            api = new HsOtgApi(h, MainActivity.this);
            int ret = api.init();// 因为第一次需要点击授权，所以第一次点击时候的返回是-1所以我利用了广播接受到授权后用handler发送消息
            if (ret == 1) {
                tv_status.setText("连接成功");
            } else {
                tv_status.setText("连接失败");
            }
        } catch (Exception e){
            Log.e("HsOtgApi", e.getMessage());
            e.printStackTrace();
        }
        initCamera();
    }

    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;
    private void initCamera() {
        try {
            // 从系统相机获取图片帧。
            final CameraImageSource cameraImageSource = new CameraImageSource(this);
            // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
//             可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
            /*Camera camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size size : supportedPreviewSizes) {
                Log.e(TAG, "supportedPreviewSizes:"+size.width+","+size.height);
            }*/

            cameraImageSource.getCameraControl().setPreferredPreviewSize(dip2px(this, 480), dip2px(this, 640));
//            cameraImageSource.getCameraControl().setPreferredPreviewSize(360, 400);

            // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
            FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(CHECK_SIZE);
            // 设置预览
            cameraImageSource.setPreviewView(previewView);
            // 设置图片源
            faceDetectManager.setImageSource(cameraImageSource);
            textureView.setOpaque(false);
            // 不需要屏幕自动变黑。
            textureView.setKeepScreenOn(true);
            boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
            setCameraType(cameraImageSource);
            initListener();
            m_Auto = true;

        } catch (Exception e){
            showToast("相机初始化失败");
        }


    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        try {
            // TODO 选择使用前置摄像头
            cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
            // TODO 选择使用usb摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
            // TODO 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
            // 如果不设置，人脸框会镜像，显示不准
            previewView.getTextureView().setScaleX(-1);
        } catch (Exception e){

        }
    }


    private void copy(Context context, String fileName, String saveName,
                      String savePath) {
        File path = new File(savePath);
        if (!path.exists()) {
            path.mkdir();
        }
        try {
            File e = new File(savePath + "/" + saveName);
            if (e.exists() && e.length() > 0L) {
                Log.i("LU", saveName + "存在了");
                return;
            }
            FileOutputStream fos = new FileOutputStream(e);
            InputStream inputStream = context.getResources().getAssets()
                    .open(fileName);
            byte[] buf = new byte[1024];
            boolean len = false;
            int len1;
            while ((len1 = inputStream.read(buf)) != -1) {
                fos.write(buf, 0, len1);
            }
            fos.close();
            inputStream.close();
        } catch (Exception var11) {
            Log.i("LU", "IO异常");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==124&&biz!=null) {
            biz.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkFace(int retCode, FaceInfo[] faceInfos, ImageFrame frame) {
        if (retCode == FaceTracker.ErrCode.OK.ordinal() && faceInfos != null) {
            FaceInfo faceInfo = faceInfos[0];
            String tip = filter(faceInfo, frame);
            displayTip(tip);
        } else {
            String tip = checkFaceCode(retCode);
            displayTip(tip);
        }
    }

    private void displayTip(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tipTv.setText(tip);
            }
        });
    }

    private void displayTip(final String tip, final TextView textView) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(tip);
            }
        });
    }

    private String filter(FaceInfo faceInfo, ImageFrame imageFrame) {
        if (!isMatching) return "";
        String tip = "";
        try {
            if (faceInfo.mConf < 0.6) {
                tip = "人脸置信度太低";
                return tip;
            }
            float[] headPose = faceInfo.headPose;
            if (Math.abs(headPose[0]) > 20 || Math.abs(headPose[1]) > 20 || Math.abs(headPose[2]) > 20) {
                tip = "人脸置角度太大，请正对屏幕";
                return tip;
            }
            int width = imageFrame.getWidth();
            int height = imageFrame.getHeight();
            // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
            // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
            float ratio = (float) faceInfo.mWidth / (float) height;
//            Log.i("liveness_ratio", "ratio=" + ratio);
            if (ratio > 0.6) {
                tip = "人脸离屏幕太近，请调整与屏幕的距离";
                return tip;
            } else if (ratio < 0.2) {
                tip = "人脸离屏幕太远，请调整与屏幕的距离";
                return tip;
            } else if (faceInfo.mCenter_x > width * 3 / 4) {
                tip = "人脸在屏幕中太靠右";
                return tip;
            } else if (faceInfo.mCenter_x < width / 4) {
                tip = "人脸在屏幕中太靠左";
                return tip;
            } else if (faceInfo.mCenter_y > height * 3 / 4) {
                tip = "人脸在屏幕中太靠下";
                return tip;
            } else if (faceInfo.mCenter_x < height / 4) {
                tip = "人脸在屏幕中太靠上";
                return tip;
            }
            asyncMath(photoFeature, faceInfo, imageFrame);
        } catch (Exception e){

        }
        return tip;
    }

    private String checkFaceCode(int errCode) {
        String tip = "";
        try {
            if (errCode == FaceTracker.ErrCode.NO_FACE_DETECTED.ordinal()) {
                //            tip = "未检测到人脸";
            } else if (errCode == FaceTracker.ErrCode.IMG_BLURED.ordinal() ||
                    errCode == FaceTracker.ErrCode.PITCH_OUT_OF_DOWN_MAX_RANGE.ordinal() ||
                    errCode == FaceTracker.ErrCode.PITCH_OUT_OF_UP_MAX_RANGE.ordinal() ||
                    errCode == FaceTracker.ErrCode.YAW_OUT_OF_LEFT_MAX_RANGE.ordinal() ||
                    errCode == FaceTracker.ErrCode.YAW_OUT_OF_RIGHT_MAX_RANGE.ordinal()) {
                tip = "请静止平视屏幕";
            } else if (errCode == FaceTracker.ErrCode.POOR_ILLUMINATION.ordinal()) {
                tip = "光线太暗，请到更明亮的地方";
            } else if (errCode == FaceTracker.ErrCode.UNKNOW_TYPE.ordinal()) {
                tip = "未检测到人脸";
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return tip;
    }

    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private volatile boolean matching = false;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private void asyncMath(final byte[] photoFeature, final FaceInfo faceInfo, final ImageFrame imageFrame) {
        if (!isMatching) return;
        if (matching) {
            return;
        }

        es.submit(new Runnable() {
            @Override
            public void run() {
                match(photoFeature, faceInfo, imageFrame);
            }
        });
    }

    private void match(final byte[] photoFeature, FaceInfo faceInfo, ImageFrame imageFrame) {
        if (faceInfo == null) return;
        float raw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        //人脸的三个角度大于20不进行识别  角度越小，人脸越正，比对时分数越高
        if (raw > 20 || patch > 20 || roll > 20) {
            return;
        }
        matching = true;
        int[] argb = imageFrame.getArgb();
        int rows = imageFrame.getHeight();
        int cols = imageFrame.getWidth();
        int[] landmarks = faceInfo.landmarks;
        int type = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL, GlobalFaceTypeModel.RECOGNIZE_LIVE);
        float score = 0;
        if (type == GlobalFaceTypeModel.RECOGNIZE_LIVE) {
            score = FaceApi.getInstance().match(photoFeature, argb, rows, cols, landmarks);
        } else if (type == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO) {
            score = FaceApi.getInstance().matchIDPhoto(photoFeature, argb, rows, cols, landmarks);
        }
        matching = false;
        if (!isMatching) return;

        displayTip( ""+score, matchScoreTv);
        try {
            if (score>=MATCH_SCORE) {
                faceDetectManager.stop();
                isMatching = false;
                mImageFrame = imageFrame;
                mHandler.removeCallbacks(checkRunnable);
                mHandler.sendEmptyMessage(MSG_CHECK_RESULT_SUCCESS);
            } else {
                if (System.currentTimeMillis() - onceStartTime > MAX_ONCE_CHECK_TIME) {
                    faceDetectManager.stop();
                    isMatching = false;
                    mHandler.removeCallbacks(checkRunnable);
                    mHandler.sendEmptyMessage(MSG_CHECK_RESULT_FAIL);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private Paint paint = new Paint();
    {
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(30);
    }
    RectF rectF = new RectF();
    /**
     * 绘制人脸框。
     */
    private void showFrame(ImageFrame imageFrame, FaceInfo[] faceInfos) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];

        rectF.set(getFaceRect(faceInfo, imageFrame));

        // 检测图片的坐标和显示的坐标不一样，需要转换。
        previewView.mapFromOriginalRect(rectF);

        float yaw = Math.abs(faceInfo.headPose[0]);
        float patch = Math.abs(faceInfo.headPose[1]);
        float roll = Math.abs(faceInfo.headPose[2]);
        if (yaw > 20 || patch > 20 || roll > 20) {
            // 不符合要求，绘制黄框
            paint.setColor(Color.YELLOW);

            String text = "请正视屏幕";
            float width = paint.measureText(text) + 50;
            float x = rectF.centerX() - width / 2;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, x + 25, rectF.top - 20, paint);
            paint.setColor(Color.YELLOW);

        } else {
            // 符合检测要求，绘制绿框
            paint.setColor(Color.GREEN);
        }
        paint.setStyle(Paint.Style.STROKE);
        // 绘制框
        canvas.drawRect(rectF, paint);
        textureView.unlockCanvasAndPost(canvas);
    }

    /**
     * 获取人脸框区域。
     *
     * @return 人脸框区域
     */
    // TODO padding?
    public Rect getFaceRect(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);
        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];
        int width = (right - left);
        int height = (bottom - top);
        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height / 2);
        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);
        return rect;
    }

    private void livnessTypeTip() {

    }

    private void toast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper());


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            return;
        }
    }


    private void pickPhotoFeature(final Bitmap bitmap) {
        faceDetectManager.setUseDetect(false);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    /*final Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri));*/
                    ARGBImg argbImg = FeatureUtils.getImageInfo(bitmap);
                    int type = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_MODEL,
                            GlobalFaceTypeModel.RECOGNIZE_LIVE);
                    int ret = 0;
                    if (type == GlobalFaceTypeModel.RECOGNIZE_LIVE) {
                        ret = FaceSDKManager.getInstance().getFaceFeature().faceFeature(argbImg,
                                photoFeature, 50);
                    } else if (type == GlobalFaceTypeModel.RECOGNIZE_ID_PHOTO) {
                        ret = FaceSDKManager.getInstance().getFaceFeature()
                                .faceFeatureForIDPhoto(argbImg, photoFeature, 50);
                    }
                    // 如果要求比较严格，可以ret FaceDetector.DETECT_CODE_OK和 FaceDetector.DETECT_CODE_HIT_LAST
                    if (ret == FaceDetector.NO_FACE_DETECTED) {
                        toast("未检测到人脸，可能原因：人脸太小（必须大于最小检测人脸minFaceSize）" +
                                "，或者人脸角度太大，人脸不是朝上");
                    } else if (ret != 512) {
                        toast("抽取特征失败");
                    } else if (ret == 512) {
                        faceDetectManager.setUseDetect(true);
                    } else {
                        toast("未检测到人脸");
                    }
                    Log.i("wtf", "photoFeature from image->" + ret);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // 结束检测。
            faceDetectManager.stop();
        } catch (Exception e){

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            isExit = true;
            m_Auto = false;
            faceDetectManager.stop();
        } catch (Exception e){

        }
        if (mHandler!=null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler=null;
        }
        try {
            if (api != null) {
                api.unInit();
            }
        } catch (Exception e){

        }
    }

    private boolean isWorking = false;
    private boolean m_Auto = false;
    public class CPUThread extends Thread {
        public CPUThread() {
            super();
        }
        @Override
        public void run() {
            super.run();
            HSIDCardInfo ici;
            Message msg;
            try {
                while (!isExit&&m_Auto) {
                    try {
                        if (!isWorking&&!isMatching) {
                            if (api.Authenticate(200, 200) != 1) {
                                msg = Message.obtain();
                                msg.what = HandlerMsg.READ_ERROR;
                                h.sendMessage(msg);
                            } else {
                                ici = new HSIDCardInfo();
                                if (api.ReadCard(ici, 200, 1300) == 1) {
                                    msg = Message.obtain();
                                    msg.obj = ici;
                                    msg.what = HandlerMsg.READ_SUCCESS;
                                    h.sendMessage(msg);
                                }
                            }
                        }
                        SystemClock.sleep(300);
                    } catch (Exception e){
                        if (!isMatching) {
                            msg = Message.obtain();
                            msg.what = 188;
                            h.sendMessage(msg);
                            m_Auto = false;
                        }
                    }
                }
            } catch (Exception e){
                msg = Message.obtain();
                msg.what = 188;
                h.sendMessage(msg);
            }
        }
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式


    private InfoBean mInfoBean;
    Handler h = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 99 || msg.what == 100) {
                tv_status.setText((String)msg.obj);
            }
            //第一次授权时候的判断是利用handler判断，授权过后就不用这个判断了
            if (msg.what ==HandlerMsg.CONNECT_SUCCESS) {
                tv_status.setText("连接成功");

            }
            if (msg.what == HandlerMsg.CONNECT_ERROR) {
                tv_status.setText("连接失败");
            }
            if (msg.what == HandlerMsg.READ_ERROR) {
                //cz();
//                tv_status.setText("卡认证失败");
            }
            if (msg.what == HandlerMsg.READ_SUCCESS) {
//                faceDetectManager.setUseDetect(false);
                Log.e("ITEM_S", "读卡成功");
                tv_status.setText("读卡成功");
                HSIDCardInfo ic = (HSIDCardInfo) msg.obj;

                mInfoBean = null;
                if (ic.getcertType() == " ") {
                    try {
                        tv_name.setText(ic.getPeopleName());
                        tv_sex.setText(ic.getSex());
                        tv_num.setText(ic.getIDCard());
                        tv_address.setText(ic.getAddr());
                        tv_birthday.setText(df.format(ic.getBirthDay()));
                        tv_date.setText(ic.getStrartDate()  + " - " + ic.getEndDate());
                        tv_check_time.setText(sdf.format(new Date(System.currentTimeMillis())));
                        mInfoBean = new InfoBean();
                        mInfoBean.setName(ic.getPeopleName());
                        mInfoBean.setSex(ic.getSex());
                        mInfoBean.setCard(ic.getIDCard());
                        mInfoBean.setAddress(ic.getAddr());
                        mInfoBean.setBirthday(df.format(ic.getBirthDay()));
                        mInfoBean.setDate(ic.getStrartDate() + "-" + ic.getEndDate());
                        mInfoBean.setDepartment(ic.getDepartment());
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                } else {


                }
                Test.test("/mnt/sdcard/test.txt4", ic.toString());
                try {
                    int ret = api.Unpack(filepath, ic.getwltdata());// 照片解码
                    Test.test("/mnt/sdcard/test3.txt", "解码中");
                    if (ret != 0) {// 读卡失败
                        return;
                    }
                    FileInputStream fis = new FileInputStream(filepath + "/zp.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
                    iv_photo.setImageBitmap(bmp);
                    isMatching = true;
                    faceDetectManager.start();
                    tv_result.setText("正在检测...");
                    tv_loading_hint.setText("正在检测...");
                    iv_loading.setVisibility(View.GONE);
                    pb_loading.setVisibility(View.VISIBLE);
                    v_big_loading.setVisibility(View.VISIBLE);
                    onceStartTime = System.currentTimeMillis();
                    matchScoreTv.setText("");
                    mHandler.postDelayed(checkRunnable, MAX_ONCE_CHECK_TIME);
                    pickPhotoFeature(bmp);
                    try {
                        if (mInfoBean!=null) {
                            InfoBean temp = DBHelper.getInstance().queryInfoData(BaseApplication.getInstance(), mInfoBean.getCard());
                            if (temp==null) {
                                DBHelper.getInstance().insertObject(BaseApplication.getInstance(), mInfoBean, InfoBean.class);
                                FileUtils.writeInfoData(mInfoBean.toString());
                            }
                            FileUtils.copyFile(filepath + "/zp.bmp", picPath+"/"+mInfoBean.getCard()+".bmp");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "头像不存在！", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    Toast.makeText(getApplicationContext(), "头像读取错误", Toast.LENGTH_SHORT).show();
                }catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "头像解码失败", Toast.LENGTH_SHORT).show();
                }

            }
        };
    };

    private void handleUpdateTime(String str) {
        if (str==null) return;
        try {
            tv_time.setText(str);
        } catch (Exception e){
            Log.e("MAI", e.getMessage());
        }
    }

    private ImageFrame mImageFrame;
    private void showCheckResult(boolean isSuccess) {
        try {
            if (isSuccess) {
                handleSuccess();
            } else {
                showPersonResult();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 人工确认结果
     */
    private void showPersonResult() {
        v_person_result.setVisibility(View.VISIBLE);
    }

    /**
     * 检测成功
     */
    private void handleSuccess() {
        if (v_person_result.getVisibility()!=View.GONE) {
            v_person_result.setVisibility(View.GONE);
        }
        tv_result.setText("检测成功");
        CHECK_SUCCESS_COUNT++;
        tv_hg.setText(""+CHECK_SUCCESS_COUNT);
        tv_loading_hint.setText("检测成功");
        iv_loading.setImageResource(R.drawable.ai_success);
        iv_loading.setVisibility(View.VISIBLE);
        pb_loading.setVisibility(View.GONE);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isWorking = false;
                if (!matching) {
                    v_big_loading.setVisibility(View.GONE);
                }
            }
        }, 2500);

        if (mInfoBean==null) return;
        //写入本地数据
        try {
            CheckDataBean bean = new CheckDataBean();
            bean.setName(mInfoBean.getName());
            bean.setCard_number(mInfoBean.getCard());
            bean.setCreate_time(System.currentTimeMillis());
            bean.setSex(mInfoBean.getSex());
            bean.setStatus(1);
            DBHelper.getInstance().insertObject(BaseApplication.getInstance(), bean, CheckDataBean.class);
            FileUtils.writeCheckData(bean.toString());
        } catch (Exception e){
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(baseHost)) return;
        //上报服务端
        try {
            Bitmap bmp = Bitmap.createBitmap(mImageFrame.getArgb(), 0, mImageFrame.getWidth(), mImageFrame.getWidth(), mImageFrame.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Map<String, String> map = new HashMap<>();
            map.put("Tag", signNum);
            map.put("InfoName", mInfoBean.getName());
            map.put("IdCard", mInfoBean.getCard());
            map.put("CompareResult", "1");
            map.put("Icon", MyUtils.getStringForBitmap(bmp));
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String temp = df2.format(new Date());
            map.put("Time", temp);
            map.put("Remark", "");
            Factory.resp(null, HttpFlag.FLAG_INSERT_ATTENDANCE, null).post(map);
        } catch (Exception e){
            showToast("error FLAG_INSERT_ATTENDANCE");
        }
    }

    private void handleFail() {
        if (v_person_result.getVisibility()!=View.GONE) {
            v_person_result.setVisibility(View.GONE);
        }
        try {
            tv_result.setText("检测失败");
            CHECK_FAIL_COUNT++;
            tv_sb.setText(""+CHECK_FAIL_COUNT);
            tv_loading_hint.setText("检测失败");
            iv_loading.setImageResource(R.drawable.ai_fail);
            iv_loading.setVisibility(View.VISIBLE);
            pb_loading.setVisibility(View.GONE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        isWorking = false;
                        if (!matching) {
                            v_big_loading.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {

                    }
                }
            }, 2500);
            if (mInfoBean==null) return;
            try {
                CheckDataBean bean = new CheckDataBean();
                bean.setName(mInfoBean.getName());
                bean.setCard_number(mInfoBean.getCard());
                bean.setCreate_time(System.currentTimeMillis());
                bean.setSex(mInfoBean.getSex());
                bean.setStatus(0);
                DBHelper.getInstance().insertObject(BaseApplication.getInstance(), bean, CheckDataBean.class);
                FileUtils.writeCheckData(bean.toString());
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (Exception e){

        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<MainActivity> weak;

        public MyHandler(MainActivity activity) {
            weak = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (weak.get()==null) return;
                if (msg.what == MSG_CHECK_RESULT_SUCCESS || msg.what == MSG_CHECK_RESULT_FAIL) {
                    weak.get().showCheckResult(msg.what==MSG_CHECK_RESULT_SUCCESS);
                } else if (msg.what == MSG_HIDE_LOADING) {
                    weak.get().hideLoading();
                }
            } catch (Exception e){
                Log.e("MAI", e.getMessage());
            }

        }
    }

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                faceDetectManager.stop();
                isMatching = false;
                showCheckResult(false);
            } catch (Exception e){

            }
        }
    };

    /**
     * 初始化成功,结束loading,启动检测
     */
    private void hideLoading() {
        tv_status.setText("初始化成功");
        if (dialog!=null) dialog.dismiss();
        new Thread(new CPUThread()).start();
    }

    /**
     * 更新系统时间
     */
    private class MyTimeTask implements Runnable {
        private SimpleDateFormat sdf;
        public MyTimeTask() {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        }

        @Override
        public void run() {
            try {
                if (!isExit) {
                    tv_time.setText(sdf.format(new Date()));
                    mHandler.postDelayed(mTask, 30000);
                }
            } catch (Exception e){
                Log.e("MyTimeTask", e.getMessage());
            }
        }
    }

    /**
     * 页面返回刷新配置参数
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        setSPData();
    }

    /**
     * 屏蔽返回
     */
    @Override
    public void onBackPressed() {}

    /**
     * 退出应用
     */
    private void toExit(){
        try {
            isExit = true;
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置存储目录
     */
    private void setFilePath() {
        filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wltlib";// 授权目录
        picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cardpic";// 授权目录
        try {
            File mT1 = new File(filepath);
            if (!mT1.exists()) {
                mT1.mkdir();
            }
            File mT2 = new File(picPath);
            if (!mT2.exists()) {
                mT2.mkdir();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置配置参数
     */
    private void setSPData() {
        MAX_ONCE_CHECK_TIME = SPLongUtils.getInt(this, "mbad_once_check_time", 30000);
        CHECK_SIZE = SPLongUtils.getInt(this, "mbad_check_size", 80);
        MATCH_SCORE = SPLongUtils.getInt(this, "mbad_match_score", 55);
        signNum = SPLongUtils.getString(this, "config_sign_table_num", "");
        baseHost = SPLongUtils.getString(this, "config_base_host", "");
        if (tv_gw!=null) tv_gw.setText(TextUtils.isEmpty(signNum)?"未设置":signNum);
        HttpFlag.changeBaseUrl(baseHost);
    }
}
