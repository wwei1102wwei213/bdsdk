package com.wei.ai;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;
import com.huashi.otg.sdk.Test;
import com.wei.ai.utils.GlobalFaceTypeModel;
import com.wei.ai.utils.SPLongUtils;
import com.wei.ai.utils.WLibPermissionsBiz;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String TAG = "BD_AI";
    private static final int PICK_PHOTO = 100;
    //权限处理业务
    private WLibPermissionsBiz biz;

    ArrayList<String> list = new ArrayList<>();
    private boolean success = false;

    private HsOtgApi api;
    private String filepath;
    private MyHandler mHandler;

    private byte[] photoFeature = new byte[2048];

    private Uri uri;

    private long MAX_ONCE_CHECK_TIME;

    private int CHECK_SIZE;

    private int MATCH_SCORE;

    private long onceStartTime;
    private boolean isMatching = false;

    private int CHECK_SUCCESS_COUNT = 0;
    private int CHECK_FAIL_COUNT = 0;
    private MyTimeTask mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wltlib";// 授权目录
            mHandler = new MyHandler(this);
            MAX_ONCE_CHECK_TIME = SPLongUtils.getInt(this, "mbad_once_check_time", 30000);
            CHECK_SIZE = SPLongUtils.getInt(this, "mbad_check_size", 80);
            MATCH_SCORE = SPLongUtils.getInt(this, "mbad_match_score", 55);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int W = dm.widthPixels;
            int H = dm.heightPixels;
            Log.e(TAG, "W:"+W+",H:"+H);
            faceDetectManager = new FaceDetectManager(getApplicationContext());
            m_Auto = true;
            initViews();
//            new Thread(new MyTimeTask()).start();
            mTask = new MyTimeTask();
            mHandler.postDelayed(mTask, 30000);
        } catch (Exception e){
            e.printStackTrace();
        }
        initPermissions();
    }

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
    private void initViews() {
        try {
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
//            iv_result = findViewById(R.id.iv_result);

            tv_time = findViewById(R.id.tv_time);
            tv_gw = findViewById(R.id.tv_gw);
            tv_hg = findViewById(R.id.tv_hg);
            tv_sb = findViewById(R.id.tv_sb);

            SimpleDateFormat ms = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            tv_time.setText(ms.format(new Date()));

            btn_setting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            btn_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.exit(0);
                }
            });
        } catch (Exception e){
            showToast(e.getMessage());
        }

    }

    int count = 0;
    private Bitmap bitmap;
    private void initListener() {
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断

                if (!isMatching) return;
//                Log.e("MBAD", ""+(++count));
                bitmap = Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888);
                checkFace(retCode, infos, frame);
                showFrame(frame, infos);
            }
        });
    }

    private void initPermissions () {
        biz = new WLibPermissionsBiz(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                124,
                new WLibPermissionsBiz.RequestPermissionsListener() {
                    @Override
                    public void RequestComplete(boolean isOk) {
                        if (isOk) {
                            init();
                        }
                    }
                });
        biz.toCheckPermission();
    }

    private ProgressDialog dialog;
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
                public void initStart() {
                }

                @Override
                public void initSuccess() {
                    dialog.dismiss();
//                    dialog.setMessage("正在初始化设备");
                    toast("初始化成功");
//                    initMeition();
                }

                @Override
                public void initFail(int errorCode, String msg) {
                    dialog.dismiss();
                    toast("初始化失败:" + msg);
                }
            });

//            m_Auto = true;
//            new Thread(new CPUThread()).start();


        } catch (Exception e){
            e.printStackTrace();
            showToast(e.getMessage());
        }

//            sam.setText(api.GetSAMID());
    }

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

        try {
            initCamera();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;
    private void initCamera() {
        try {
            // 从系统相机获取图片帧。
            final CameraImageSource cameraImageSource = new CameraImageSource(this);
            // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
//             可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
            Camera camera = Camera.open();
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size size : supportedPreviewSizes) {
                Log.e(TAG, "supportedPreviewSizes:"+size.width+","+size.height);
            }

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
            new Thread(new CPUThread()).start();

        } catch (Exception e){
            showToast("initCamera=====>"+e.getMessage());
        }
        dialog.dismiss();

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
            showToast(e.getMessage());
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
            Log.i("liveness_ratio", "ratio=" + ratio);
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
            showToast(e.getMessage());
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

        if (faceInfo == null) {
            return;
        }

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
                mHandler.sendEmptyMessage(MSG_CHECK_RESULT_SUCCESS);



            } else {
                if (System.currentTimeMillis() - onceStartTime > MAX_ONCE_CHECK_TIME) {
                    faceDetectManager.stop();
                    isMatching = false;
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
    protected void onStart() {
        super.onStart();
        try {
            // 开始检测
//            faceDetectManager.start();
        } catch (Exception e){
            showToast(e.getMessage());
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // 结束检测。
            faceDetectManager.stop();
        } catch (Exception e){
            showToast(e.getMessage());
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
            showToast(e.getMessage());
        }
        if (api == null) {
            return;
        }
        api.unInit();
        try {
            if (mHandler!=null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e){

        }
    }

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
                while (m_Auto) {
                    try {
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
                        SystemClock.sleep(300);
                    } catch (Exception e){
                        msg = Message.obtain();
                        msg.what = 188;
                        h.sendMessage(msg);
                        m_Auto = false;
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

    /**
     * 指纹 指位代码
     *
     * @param FPcode
     * @return
     */
    String GetFPcode(int FPcode) {
        switch (FPcode) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "未知";
        }
    }

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
                tv_status.setText("读卡成功");
                HSIDCardInfo ic = (HSIDCardInfo) msg.obj;
                byte[] fp = new byte[1024];
                fp = ic.getFpDate();
                String m_FristPFInfo = "";
                String m_SecondPFInfo = "";

                if (fp[4] == (byte)0x01) {
                    m_FristPFInfo = String.format("指纹  信息：第一枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[5]), fp[6]);
                } else {
                    m_FristPFInfo = "身份证无指纹 \n";
                }
                if (fp[512 + 4] == (byte)0x01) {
                    m_SecondPFInfo = String.format("指纹  信息：第二枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[512 + 5]),
                            fp[512 + 6]);
                } else {
                    m_SecondPFInfo = "身份证无指纹 \n";
                }
                if (ic.getcertType() == " ") {
                    /*tv_info.setText("证件类型：身份证\n" + "姓名："
                            + ic.getPeopleName() + "\n" + "性别：" + ic.getSex()
                            + "\n" + "民族：" + ic.getPeople() + "\n" + "出生日期："
                            + df.format(ic.getBirthDay()) + "\n" + "地址："
                            + ic.getAddr() + "\n" + "身份号码：" + ic.getIDCard()
                            + "\n" + "签发机关：" + ic.getDepartment() + "\n"
                            + "有效期限：" + ic.getStrartDate() + "-"
                            + ic.getEndDate());*/
                            /*+ "\n" + m_FristPFInfo + "\n"
                            + m_SecondPFInfo);*/
                            try {
                                tv_name.setText(ic.getPeopleName());
                                tv_sex.setText(ic.getSex());
                                tv_num.setText(ic.getIDCard());
                                tv_address.setText(ic.getAddr());
                                tv_birthday.setText(df.format(ic.getBirthDay()));
                                tv_date.setText(ic.getStrartDate()  + " - " + ic.getEndDate());
                                tv_check_time.setText(sdf.format(new Date(System.currentTimeMillis())));

                            } catch (Exception e){
                                e.printStackTrace();
                            }

                } else {
                    if(ic.getcertType() == "J")
                    {
                        /*tv_info.setText("证件类型：港澳台居住证（J）\n"
                                + "姓名：" + ic.getPeopleName() + "\n" + "性别："
                                + ic.getSex() + "\n"
                                + "签发次数：" + ic.getissuesNum() + "\n"
                                + "通行证号码：" + ic.getPassCheckID() + "\n"
                                + "出生日期：" + df.format(ic.getBirthDay())
                                + "\n" + "地址：" + ic.getAddr() + "\n" + "身份号码："
                                + ic.getIDCard() + "\n" + "签发机关："
                                + ic.getDepartment() + "\n" + "有效期限："
                                + ic.getStrartDate() + "-" + ic.getEndDate()
                                + "\n"
                                + m_FristPFInfo + "\n" + m_SecondPFInfo);*/
                    }
                    else{
                        if(ic.getcertType() == "I")
                        {
                            /*tv_info.setText("证件类型：外国人永久居留证（I）\n"
                                    + "英文名称：" + ic.getPeopleName() + "\n"
                                    + "中文名称：" + ic.getstrChineseName() + "\n"
                                    + "性别：" + ic.getSex() + "\n"
                                    + "永久居留证号：" + ic.getIDCard() + "\n"
                                    + "国籍：" + ic.getstrNationCode() + "\n"
                                    + "出生日期：" + df.format(ic.getBirthDay())
                                    + "\n" + "证件版本号：" + ic.getstrCertVer() + "\n"
                                    + "申请受理机关：" + ic.getDepartment() + "\n"
                                    + "有效期限："+ ic.getStrartDate() + "-" + ic.getEndDate() + "\n"
                                    + m_FristPFInfo + "\n" + m_SecondPFInfo);*/
                        }
                    }

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
                    tv_result.setText("正在检测...");
                    onceStartTime = System.currentTimeMillis();
                    matchScoreTv.setText("");

                    faceDetectManager.start();
                    pickPhotoFeature(bmp);
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

    private void showCheckResult(boolean isSuccess) {
        try {
            if (isSuccess) {
                tv_result.setText("检测成功");
                CHECK_SUCCESS_COUNT++;
                tv_hg.setText(""+CHECK_SUCCESS_COUNT);
            } else {
                tv_result.setText("检测失败");
                CHECK_FAIL_COUNT++;
                tv_sb.setText(""+CHECK_FAIL_COUNT);
            }
        } catch (Exception e){
            e.printStackTrace();
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
                }
            } catch (Exception e){
                Log.e("MAI", e.getMessage());
            }

        }
    }

    private boolean isExit = false;
    private static final int MSG_UPDATE_CURRENT_TIME = 254;
    private static final int MSG_CHECK_RESULT_SUCCESS = 255;
    private static final int MSG_CHECK_RESULT_FAIL = 256;
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

}
