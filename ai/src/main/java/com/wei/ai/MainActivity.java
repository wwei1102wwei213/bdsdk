package com.wei.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
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
import com.baidu.aip.entity.Group;
import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.TexturePreviewView;
import com.baidu.aip.face.camera.CameraView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.manager.FaceDetector;
import com.baidu.aip.manager.FaceLiveness;
import com.baidu.aip.manager.FaceSDKManager;
import com.baidu.aip.utils.FeatureUtils;
import com.baidu.aip.utils.FileUitls;
import com.baidu.aip.utils.ImageUtils;
import com.baidu.aip.utils.PreferencesUtil;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;
import com.baidu.idl.license.AndroidLicenser;
import com.huashi.otg.sdk.HsOtgApi;
import com.wei.ai.utils.GlobalFaceTypeModel;
import com.wei.ai.utils.WLibPermissionsBiz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements View.OnClickListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wltlib";// 授权目录
        mHandler = new MyHandler(this);
        uri = Uri.parse("content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20181021_171721.jpg");
        faceDetectManager = new FaceDetectManager(getApplicationContext());
        initViews();

        initPermissions();
    }

    private TextView tv_status;
    private TexturePreviewView previewView;
    private TextureView textureView;
    private TextView tipTv, matchScoreTv;
    private ImageView iv;
    private View btn;
    private void initViews() {
        tv_status = findViewById(R.id.tv_status);
        previewView = findViewById(R.id.preview_view);
        textureView = findViewById(R.id.texture_view);
        tipTv =  findViewById(R.id.tip);
        matchScoreTv = (TextView) findViewById(R.id.match_score_tv);
        iv = findViewById(R.id.pick_from_album_iv);
        /*try {
            final Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(uri));
            iv.setImageBitmap(bitmap);
            Log.e("MERROR", "setImageBitmap");
        } catch (Exception e){
            Log.e("MERROR", e.getMessage());
        }*/
        btn = findViewById(R.id.btn);
    }

    int count = 0;
    private void initListener() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceDetectManager.setUseDetect(false);
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO);
            }
        });
        // 设置回调，回调人脸检测结果。
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(int retCode, FaceInfo[] infos, ImageFrame frame) {
                // TODO 显示检测的图片。用于调试，如果人脸sdk检测的人脸需要朝上，可以通过该图片判断
                Log.e("MBAD", ""+(++count));
                final Bitmap bitmap =
                        Bitmap.createBitmap(frame.getArgb(), frame.getWidth(), frame.getHeight(),
                                Bitmap.Config.ARGB_8888);
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        testView.setImageBitmap(bitmap);
                    }
                });*/
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
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("正在初始化设备");
        dialog.show();
        PreferencesUtil.initPrefs(this);
        // 使用人脸1：n时使用
        DBManager.getInstance().init(this);
        livnessTypeTip();
//        FaceEnvironment faceEnvironment = new FaceEnvironment();
//        // 模糊度范围 (0-1) 推荐小于0.7
//        faceEnvironment.setBlurrinessThreshold(FaceEnvironment.VALUE_BLURNESS);
//        // 光照范围 (0-1) 推荐大于40
//        faceEnvironment.setIlluminationThreshold(FaceEnvironment.VALUE_BLURNESS);
//        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
//        faceEnvironment.setPitch(FaceEnvironment.VALUE_HEAD_PITCH);
//        faceEnvironment.setRoll(FaceEnvironment.VALUE_HEAD_ROLL);
//        faceEnvironment.setYaw(FaceEnvironment.VALUE_HEAD_YAW);
//        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
//        faceEnvironment.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
//        // 人脸置信度（0-1）推荐大于0.6
//        faceEnvironment.setNotFaceThreshold(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
//        // 人脸遮挡范围 （0-1） 推荐小于0.5
//        faceEnvironment.setOcclulationThreshold(FaceEnvironment.VALUE_OCCLUSION);
//        // 是否进行质量检测,开启会降低性能
//        faceEnvironment.setCheckQuality(false);
//        FaceSDKManager.getInstance().getFaceDetector().setFaceEnvironment(faceEnvironment);
        FaceSDKManager.getInstance().init(this);
        FaceSDKManager.getInstance().setSdkInitListener(new FaceSDKManager.SdkInitListener() {
            @Override
            public void initStart() {
//                toast("sdk init start");
            }

            @Override
            public void initSuccess() {
                dialog.dismiss();
                toast("初始化成功");
            }

            @Override
            public void initFail(int errorCode, String msg) {
                dialog.dismiss();
                toast("初始化失败:" + msg);
            }
        });

        copy(MainActivity.this, "base.dat", "base.dat", filepath);
        copy(MainActivity.this, "license.lic", "license.lic", filepath);
        api = new HsOtgApi(mHandler, MainActivity.this);
        int ret = api.init();// 因为第一次需要点击授权，所以第一次点击时候的返回是-1所以我利用了广播接受到授权后用handler发送消息
        if (ret == 1) {
            tv_status.setText("连接成功");
//            sam.setText(api.GetSAMID());
        } else {
            tv_status.setText("连接失败");
            initCamera();
        }

    }

    // 用于检测人脸。
    private FaceDetectManager faceDetectManager;
    private void initCamera() {

        // 从系统相机获取图片帧。
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        // 图片越小检测速度越快，闸机场景640 * 480 可以满足需求。实际预览值可能和该值不同。和相机所支持的预览尺寸有关。
        // 可以通过 camera.getParameters().getSupportedPreviewSizes()查看支持列表。
        cameraImageSource.getCameraControl().setPreferredPreviewSize(dip2px(this, 300), dip2px(this, 400));

        // 设置最小人脸，该值越小，检测距离越远，该值越大，检测性能越好。范围为80-200
        FaceSDKManager.getInstance().getFaceDetector().setMinFaceSize(120);
        // 设置预览
        cameraImageSource.setPreviewView(previewView);
        // 设置图片源
        faceDetectManager.setImageSource(cameraImageSource);

        textureView.setOpaque(false);
        // 不需要屏幕自动变黑。
        textureView.setKeepScreenOn(true);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
            // 相机坚屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
            // 相机横屏模式
            cameraImageSource.getCameraControl().setDisplayOrientation(CameraView.ORIENTATION_HORIZONTAL);
        }

        setCameraType(cameraImageSource);

        initListener();
        Log.e("MERROR", "initCrema");

    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void setCameraType(CameraImageSource cameraImageSource) {
        // TODO 选择使用前置摄像头
        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_FRONT);
        previewView.getTextureView().setScaleX(-1);
        // TODO 选择使用usb摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_USB);
//        // 如果不设置，人脸框会镜像，显示不准
//        previewView.getTextureView().setScaleX(-1);

        // TODO 选择使用后置摄像头
//        cameraImageSource.getCameraControl().setCameraFacing(ICameraControl.CAMERA_FACING_BACK);
//        previewView.getTextureView().setScaleX(-1);
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
//        int type=PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_THREAD,GlobalFaceTypeModel.SINGLETHREAD);
//        if (type==GlobalFaceTypeModel.SINGLETHREAD){
//            FaceSDKManager.getInstance().init(this);
//        }
    }

    @Override
    public void onClick(View v) {
        /*if (v == deviceActivateBtn) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                    .PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }
            FaceSDKManager.getInstance().showActivation();
            return;
        }*/

        if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_UNACTIVATION) {
            toast("SDK还未激活，请先激活");
            return;
        } else if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_UNINIT) {
            toast("SDK还未初始化完成，请先初始化");
            return;
        } else if (FaceSDKManager.getInstance().initStatus() == FaceSDKManager.SDK_INITING) {
            toast("SDK正在初始化，请稍后再试");
            return;
        }

    }

    // 读取文本文件中的内容
    public String readFile(String strFilePath, String mark) {
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                        if (mark.equals("liscense")) {
                            list.add(line);
                        }
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    // 判断文件是否存在
    public boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    private AlertDialog alertDialog;
    private String[] items;

    public void showSingleAlertDialog() {

        List<Group> groupList = FaceApi.getInstance().getGroupList(0, 1000);
        if (groupList.size() <= 0) {
            Toast.makeText(this, "还没有分组，请创建分组并添加用户", Toast.LENGTH_SHORT).show();
            return;
        }
        items = new String[groupList.size()];
        for (int i = 0; i < groupList.size(); i++) {
            Group group = groupList.get(i);
            items[i] = group.getGroupId();
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择分组groupID");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                Toast.makeText(MainActivity.this, items[index], Toast.LENGTH_SHORT).show();

                choiceIdentityType(items[index]);
                alertDialog.dismiss();
            }
        });

        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void choiceMatchType() {
        /*int type = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
                .TYPE_NO_LIVENSS);
        if (type == LivenessSettingActivity.TYPE_NO_LIVENSS) {
//            Toast.makeText(this, "当前活体策略：无活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, RgbVideoMatchImageActivity.class);
            startActivity(intent);
        } else if (type == LivenessSettingActivity.TYPE_RGB_LIVENSS) {
            Toast.makeText(this, "当前活体策略：单目RGB活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, RgbVideoMatchImageActivity.class);
            startActivity(intent);
        } else if (type == LivenessSettingActivity.TYPE_RGB_IR_LIVENSS) {
            Toast.makeText(this, "当前活体策略：双目RGB+IR活体", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, RgbIrVideoMathImageActivity.class);
            startActivity(intent);
        } else if (type == LivenessSettingActivity.TYPE_RGB_DEPTH_LIVENSS) {
            Toast.makeText(this, "当前活体策略：双目RGB+Depth活体", Toast.LENGTH_LONG).show();
            int cameraType = PreferencesUtil.getInt(GlobalFaceTypeModel.TYPE_CAMERA, GlobalFaceTypeModel.ORBBEC);
            Intent intent = null;
            if (cameraType == GlobalFaceTypeModel.ORBBEC) {
                intent = new Intent(MainActivity.this, OrbbecVideoMatchImageActivity.class);
            } else if (cameraType == GlobalFaceTypeModel.IMIMECT) {
                intent = new Intent(MainActivity.this, IminectVideoMatchImageActivity.class);
            } else if (cameraType == GlobalFaceTypeModel.ORBBECPRO) {
                intent = new Intent(MainActivity.this, OrbbecProVideoMatchImageActivity.class);
            }
            startActivity(intent);
        }*/
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

        String tip = "";
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
        /*int liveType = PreferencesUtil.getInt(LivenessSettingActivity.TYPE_LIVENSS, LivenessSettingActivity
                .TYPE_NO_LIVENSS);
        if (liveType == LivenessSettingActivity.TYPE_NO_LIVENSS) {
            asyncMath(photoFeature, faceInfo, imageFrame);
        } else if (liveType == LivenessSettingActivity.TYPE_RGB_LIVENSS) {

            float rgbLivenessScore = rgbLiveness(imageFrame, faceInfo);
            if (rgbLivenessScore > 0.9) {
                asyncMath(photoFeature, faceInfo, imageFrame);
            } else {
                toast("rgb活体分数过低");
            }
        }*/


        return tip;
    }

    private String checkFaceCode(int errCode) {
        String tip = "";
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
        return tip;
    }

    private volatile boolean matching = false;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private void asyncMath(final byte[] photoFeature, final FaceInfo faceInfo, final ImageFrame imageFrame) {
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
        displayTip("比对得分：" + score, matchScoreTv);

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
//        rectF = new RectF(W - dip2px(this, 300), 0, 0, H - dip2px(this, 400));
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

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 4 / 3;
        //
        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height / 2;
        //
        //            rect.top = top;
        //            rect.left = left;
        //            rect.right = left + width;
        //            rect.bottom = top + height;

        //            int width = (right - left) * 4 / 3;
        //            int height = (bottom - top) * 5 / 3;
        int width = (right - left);
        int height = (bottom - top);

        //            left = getInfo().mCenter_x - width / 2;
        //            top = getInfo().mCenter_y - height * 2 / 3;
        left = (int) (faceInfo.mCenter_x - width / 2);
        top = (int) (faceInfo.mCenter_y - height / 2);


        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);

        return rect;
    }

    private void choiceIdentityType(String groupId) {

    }


    private void choiceAttrTrackType() {

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



    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory(); // 获取跟目录
        }
        if (sdDir != null) {
            return sdDir.toString();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == 0) {
            return;
        }
        if (requestCode == PICK_PHOTO && (data != null && data.getData() != null)) {
            Uri uri = ImageUtils.geturi(data, this);
            pickPhotoFeature(uri);
        }
    }



    public Uri geturi(android.content.Intent intent, Context context) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/*"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                Log.e("MTS", "image");
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.ImageColumns._ID },
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    // set _id value
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    // do nothing
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    private void pickPhotoFeature(final Uri imageUri) {
        faceDetectManager.setUseDetect(false);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {


                    final Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri));
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
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(bitmap);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始检测
        faceDetectManager.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 结束检测。
        faceDetectManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetectManager.stop();
    }


    private static class MyHandler extends Handler {

        private WeakReference<MainActivity> weak;

        public MyHandler(MainActivity activity) {
            weak = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weak.get()!=null) {

            }
        }
    }

}
