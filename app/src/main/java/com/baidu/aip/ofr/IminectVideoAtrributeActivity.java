/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.ofr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.ImageFrame;

import com.baidu.aip.callback.ILivenessCallBack;

import com.baidu.aip.entity.LivenessModel;
import com.baidu.aip.iminect.ColorSurfaceView;
import com.baidu.aip.iminect.Jni;

import com.baidu.aip.manager.FaceEnvironment;
import com.baidu.aip.manager.FaceLiveness;


import com.baidu.aip.utils.ImageUtils;

import com.baidu.idl.facesdk.FaceAttribute;
import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.hjimi.api.iminect.ImiCamera;
import com.hjimi.api.iminect.ImiCameraFrame;
import com.hjimi.api.iminect.ImiCameraFrameMode;
import com.hjimi.api.iminect.ImiCameraPixelFormat;
import com.hjimi.api.iminect.ImiDevice;
import com.hjimi.api.iminect.ImiDeviceAttribute;
import com.hjimi.api.iminect.ImiDeviceState;
import com.hjimi.api.iminect.ImiFrameMode;
import com.hjimi.api.iminect.ImiFrameType;
import com.hjimi.api.iminect.ImiImageFrame;
import com.hjimi.api.iminect.ImiNect;
import com.hjimi.api.iminect.ImiPixelFormat;
import com.hjimi.api.iminect.Utils;
import com.orbbec.obDepth2.HomeKeyListener;
import com.orbbec.view.OpenGLView;

import org.openni.Device;
import org.openni.VideoStream;
import org.openni.android.OpenNIHelper;


import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.baidu.aip.manager.FaceLiveness.MASK_RGB;

public class IminectVideoAtrributeActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "IminectVideoMatchImageActivity";
    private static final int PICK_PHOTO = 100;
    private TextView tipTv;
    private HomeKeyListener mHomeListener;
    private Activity mContext;
    private ExecutorService es;
    private Future future;
    private byte[] photoFeature = new byte[2048];
    private volatile boolean matching = false;

    private TextView detectDurationTv;
    private TextView rgbLivenssDurationTv;
    private TextView rgbLivenessScoreTv;
    private TextView depthLivenssDurationTv;
    private TextView depthLivenessScoreTv;
    private TextView tvAttrIminect;
    private ImageView photoIv;

    private OpenGLView mDepthGLView;
    private OpenGLView mRgbGLView;

    private boolean initOk = false;
    private Device device;
    private Thread thread;
    private OpenNIHelper mOpenNIHelper;
    private VideoStream depthStream;
    private VideoStream rgbStream;

    private int mWidth = com.orbbec.utils.GlobalDef.RESOLUTION_X;
    private int mHeight = com.orbbec.utils.GlobalDef.RESOLUTION_Y;
    private final int depthNeedPermission = 33;
    private Object sync = new Object();
    private boolean exit = false;


    private ColorSurfaceView mColorSurfaceView;
    private ColorSurfaceView mDepthSurfaceView;
    private TextView rgbLivenessResult;
    private TextView depthLivenessResult;
    boolean rgblivenessSuccess = false;
    private static ImiNect m_ImiNect = null;
    private static MainListener mMainListener = null;
    private ImiDevice mImiDevice0 = null;
    private ImiDeviceAttribute mDeviceAttribute = null;
    private boolean mIsExitLoop = false;
    private ByteBuffer mColorBuffer;
    private ByteBuffer mDepthBuffer;
    private boolean mIsDigging = false;
    private ImiPixelFormat mColorPixel;

    private ImiCamera mCamera = null;

    private FaceAttribute mFaceAttribute;

    private int expectUserFrameWidth = 640;
    private int expectUserFrameHeight = 480;

    private static final int DEVICE_OPEN_FALIED = 1;
    private static final int DEVICE_DISCONNECT = 2;

    private static final int REQUEST_CAMERA_CODE = 0x007;

    // textureView用于绘制人脸框等。
    private TextureView textureView;


    private ImiDeviceState deviceState = ImiDeviceState.IMI_DEVICE_STATE_CONNECT;

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DEVICE_OPEN_FALIED:
                case DEVICE_DISCONNECT:
                    showMessageDialog();
                    break;
                default:
                    break;
            }
        }
    };

    private void showMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("The device is not connected!!!");
        builder.setPositiveButton("quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }

    LivenessModel mModel;

    class MainListener implements ImiCamera.OpenCameraListener, ImiDevice.OpenDeviceListener,
            ImiDevice.DeviceStateListener {

        @Override
        public void onOpenCameraSuccess() {
            mCamera = m_ImiNect.Camera();

            m_ImiNect.Device().open(mMainListener);
        }

        @Override
        public void onOpenCameraFailed(String errorMsg) {
            mainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
        }

        @Override
        public void onOpenDeviceSuccess() {
            mImiDevice0 = m_ImiNect.Device();
            mDeviceAttribute = mImiDevice0.getAttribute();

            try {
                setUserExpectMode(expectUserFrameWidth, expectUserFrameHeight);
            } catch (Exception e) {
                Log.e(TAG, "setUserExpectMode: falied, invalid frame mode");
            }

            mImiDevice0.setImageRegistration(true); // set image registration.

            new Thread(new ColorViewRefreshRunnable()).start();

            FaceLiveness.getInstance().setLivenessCallBack(new ILivenessCallBack() {
                @Override
                public void onCallback(LivenessModel livenessModel) {
                    checkResult(livenessModel);
                    mModel=livenessModel;
                }

                @Override
                public void onTip(int code, final String msg) {
                    Log.i("wtf", "onCallback" + msg);
                    showFrame(mModel);
                }

                @Override
                public void onCanvasRectCallback(LivenessModel livenessModel) {
                }
            });
        }

        @Override
        public void onOpenDeviceFailed(String errorMsg) {
            mainHandler.sendEmptyMessage(DEVICE_OPEN_FALIED);
        }

        @Override
        public void onDeviceStateChanged(String deviceUri, ImiDeviceState state) {
            if (state == ImiDeviceState.IMI_DEVICE_STATE_DISCONNECT) {
                // device disconnect.
                // Toast.makeText(MainActivity.this, s+" DISCONNECT", Toast.LENGTH_SHORT).show();
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    deviceState = ImiDeviceState.IMI_DEVICE_STATE_DISCONNECT;
                    mainHandler.sendEmptyMessage(DEVICE_DISCONNECT);
                }
            } else if (state == ImiDeviceState.IMI_DEVICE_STATE_CONNECT) {
                // Toast.makeText(MainActivity.this, s+" CONNECT", Toast.LENGTH_SHORT).show();
                if (mDeviceAttribute != null && mDeviceAttribute.getUri().equals(deviceUri)) {
                    deviceState = ImiDeviceState.IMI_DEVICE_STATE_CONNECT;
                }
            }
        }
    }

    private void setUserExpectMode(int width, int height) {
        ImiFrameMode frameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_DEP_16BIT, width, height);
        mImiDevice0.setFrameMode(ImiFrameType.DEPTH, frameMode);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iminect_video_attribute);
        findView();
        mContext = this;

        // registerHomeListener();
        FaceSDK.faceAttributeModelInit(this);
        es = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findView() {

        textureView = findViewById(R.id.texture_view);

        tipTv = (TextView) findViewById(R.id.message);

        mDepthSurfaceView = (ColorSurfaceView) findViewById(R.id.depthGlView);
        mColorSurfaceView = (ColorSurfaceView) findViewById(R.id.rgbGlView);

        detectDurationTv = (TextView) findViewById(R.id.detect_duration_tv);
        rgbLivenssDurationTv = (TextView) findViewById(R.id.rgb_liveness_duration_tv);
        rgbLivenessScoreTv = (TextView) findViewById(R.id.rgb_liveness_score_tv);
        depthLivenssDurationTv = (TextView) findViewById(R.id.depth_liveness_duration_tv);
        depthLivenessScoreTv = (TextView) findViewById(R.id.depth_liveness_score_tv);
        tvAttrIminect = (TextView) findViewById(R.id.tv_attr_iminect);
        photoIv = (ImageView) findViewById(R.id.pick_from_album_iv);

        m_ImiNect = ImiNect.create(IminectVideoAtrributeActivity.this);

        mMainListener = new MainListener();

        m_ImiNect.Device().addDeviceStateListener(mMainListener);

        if (isCameraPermission(IminectVideoAtrributeActivity.this, REQUEST_CAMERA_CODE)) {
            m_ImiNect.Camera().open(mMainListener);
        }

    }

    public boolean isCameraPermission(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, requestCode);
                Toast.makeText(this, "requestPermissions Camera Permission",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Toast.makeText(this, "Already have Camera Permission", Toast.LENGTH_SHORT).show();
        return true;
    }


    private class ColorViewRefreshRunnable implements Runnable {

        @Override
        public void run() {
            try {
                ImiCameraFrameMode frameMode = new ImiCameraFrameMode(
                        ImiCameraPixelFormat.IMI_CAMERA_PIXEL_FORMAT_RGB888,
                        640, 480, 30);
                mCamera.startPreview(frameMode);

                ImiFrameMode depthFrameMode = new ImiFrameMode(ImiPixelFormat.IMI_PIXEL_FORMAT_DEP_16BIT,
                        640, 480);
                mImiDevice0.setFrameMode(ImiFrameType.DEPTH, depthFrameMode);
                mImiDevice0.openStream(ImiFrameType.DEPTH);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            ImiCameraFrame colorFrame = null;
            ImiImageFrame depthFrame = null;
            ByteBuffer depthByteBuf = null;
            ByteBuffer colorByteBuf = null;
            Jni jni = new Jni();

            while (!mIsExitLoop) {

                if (depthFrame == null) {
                    depthFrame = mImiDevice0.readNextFrame(ImiFrameType.DEPTH, 40);
                }

                if (colorFrame == null) {
                    colorFrame = mCamera.readNextFrame(40);
                }
                if (null == depthFrame || null == colorFrame) {
                    continue;
                }

                mColorBuffer = colorFrame.getData();
                mDepthBuffer = depthFrame.getData();
                ByteBuffer depthframeData;
                depthframeData = Utils.depth2RGB888(depthFrame, true, false);
                int rgbLen = mColorBuffer.remaining();
                byte[] rgbByte = new byte[rgbLen];
                mColorBuffer.get(rgbByte);
                final Bitmap bitmap = ImageUtils.RGB2Bitmap(rgbByte, 640, 480);
                mColorSurfaceView.updateVertices(mColorBuffer);
                mDepthSurfaceView.updateVertices(depthframeData);
                int depthLen = mDepthBuffer.remaining();
                byte[] depthByte = new byte[depthLen];
                mDepthBuffer.get(depthByte);
                FaceLiveness.getInstance().setRgbBitmap(bitmap);
                FaceLiveness.getInstance().setDepthData(depthByte);
                FaceLiveness.getInstance().livenessCheck(640, 480, 0X0101);
                colorFrame = null;
                depthFrame = null;
            }
        }
    }

    ;


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsExitLoop = true;

        if (mImiDevice0 != null) {
            mImiDevice0.closeStream(ImiFrameType.DEPTH);
            mImiDevice0.close();
            mImiDevice0 = null;
        }

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.close();
            mCamera = null;
        }
        m_ImiNect.destroy();
        finish();
    }


    private void showAlertAndExit(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == depthNeedPermission) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Permission Grant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void checkResult(LivenessModel model) {

        if (model == null) {
            clearTip();
            return;
        }

        displayResult(model);
        int type = model.getLiveType();
        boolean livenessSuccess = false;
        // 同一时刻都通过才认为活体通过，开发者也可以根据自己的需求修改策略
        if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
            livenessSuccess = (model.getRgbLivenessScore() > FaceEnvironment.LIVENESS_RGB_THRESHOLD) ? true : false;
        }
        if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
            boolean irScore = (model.getIrLivenessScore() > FaceEnvironment.LIVENESS_IR_THRESHOLD) ? true : false;
            if (!irScore) {
                livenessSuccess = false;
            } else {
                livenessSuccess &= irScore;
            }
        }
        if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
            boolean depthScore = (model.getDepthLivenessScore() > FaceEnvironment.LIVENESS_DEPTH_THRESHOLD) ? true :
                    false;
            if (!depthScore) {
                livenessSuccess = false;
            } else {
                livenessSuccess &= depthScore;
            }
        }

        if (livenessSuccess) {
            attrCheck(model.getFaceInfo(), model.getImageFrame());
        }
    }

    /**
     * 进行属性检测
     */
    private void attrCheck(FaceInfo faceInfo, ImageFrame imageFrame) {
        // todo 人脸属性数据获取
        mFaceAttribute = FaceSDK.faceAttribute(imageFrame.getArgb(),
                imageFrame.getWidth(), imageFrame.getHeight(), FaceSDK.ImgType.ARGB,
                faceInfo.landmarks);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAttrIminect.setText("人脸属性：" + getMsg(mFaceAttribute));
            }
        });
    }

    public String getMsg(FaceAttribute attribute) {
        StringBuilder msg = new StringBuilder();
        if (attribute != null) {
            msg.append((int) attribute.age).append(",")
                    .append(attribute.race == 0 ? "黄种人" : attribute.race == 1 ? "白种人" :
                            attribute.race == 2 ? "黑人" : attribute.race == 3 ? "印度人" : "地球人").append(",")
                    .append(attribute.expression == 0 ? "正常" : attribute.expression == 1 ? "微笑" : "大笑").append(",")
                    .append(attribute.gender == 0 ? "女" : attribute.gender == 1 ? "男" : "婴儿").append(",")
                    .append(attribute.glasses == 0 ? "不戴眼镜" : attribute.glasses == 1 ? "普通透明眼镜" : "太阳镜");
        }
        return msg.toString();
    }

    private void displayResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int type = livenessModel.getLiveType();
                detectDurationTv.setText("人脸检测耗时：" + livenessModel.getRgbDetectDuration());
                if ((type & FaceLiveness.MASK_RGB) == FaceLiveness.MASK_RGB) {
                    rgbLivenessScoreTv.setText("RGB活体得分：" + livenessModel.getRgbLivenessScore());
                    rgbLivenssDurationTv.setText("RGB活体耗时：" + livenessModel.getRgbLivenessDuration());
                }

                if ((type & FaceLiveness.MASK_IR) == FaceLiveness.MASK_IR) {
                    Log.e(TAG, FaceLiveness.MASK_IR + "");
                }

                if ((type & FaceLiveness.MASK_DEPTH) == FaceLiveness.MASK_DEPTH) {
                    depthLivenessScoreTv.setText("Depth活体得分：" + livenessModel.getDepthLivenessScore());
                    depthLivenssDurationTv.setText("Depth活体耗时：" + livenessModel.getDetphtLivenessDuration());
                }
            }

        });
    }


    private void registerHomeListener() {
        mHomeListener = new HomeKeyListener(this);
        mHomeListener
                .setOnHomePressedListener(new HomeKeyListener.OnHomePressedListener() {

                    @Override
                    public void onHomePressed() {
                        // TODO
                        finish();
                    }

                    @Override
                    public void onHomeLongPressed() {
                        // TODO
                    }
                });
        mHomeListener.startWatch();
    }

    private void unRegisterHomeListener() {
        if (mHomeListener != null) {
            mHomeListener.stopWatch();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void clearTip() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detectDurationTv.setText("");
                rgbLivenessScoreTv.setText("");
                rgbLivenssDurationTv.setText("");
                depthLivenessScoreTv.setText("");
                depthLivenssDurationTv.setText("");
                tvAttrIminect.setText("");
            }
        });
    }

    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IminectVideoAtrributeActivity.this, tip, Toast.LENGTH_SHORT).show();
            }
        });
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
    private void showFrame(LivenessModel model) {
        Canvas canvas = textureView.lockCanvas();
        if (canvas == null) {
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        if (model == null) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        FaceInfo[] faceInfos = model.getTrackFaceInfo();
        ImageFrame imageFrame = model.getImageFrame();
        if (faceInfos == null || faceInfos.length == 0) {
            // 清空canvas
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            textureView.unlockCanvasAndPost(canvas);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        FaceInfo faceInfo = faceInfos[0];

        rectF.set(getFaceRectTwo(faceInfo, imageFrame));

        // 检测图片的坐标和显示的坐标不一样，需要转换。
        // mPreview[typeIndex].mapFromOriginalRect(rectF);

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

    public Rect getFaceRectTwo(FaceInfo faceInfo, ImageFrame frame) {
        Rect rect = new Rect();
        int[] points = new int[8];
        faceInfo.getRectPoints(points);
        int left = points[2];
        int top = points[3];
        int right = points[6];
        int bottom = points[7];
//        int previewWidth=surfaViews[typeIndex].getWidth();
//        int previewHeight=surfaViews[typeIndex].getHeight();
        int previewWidth = mColorSurfaceView.getWidth();
        int previewHeight = mColorSurfaceView.getHeight();
        float scaleW = 1.0f * previewWidth / frame.getWidth();
        float scaleH = 1.0f * previewHeight / frame.getHeight();
        int width = (right - left);
        int height = (bottom - top);
        left = (int) ((faceInfo.mCenter_x - width / 2) * scaleW);
        top = (int) ((faceInfo.mCenter_y - height / 2) * scaleW);
//        left = (int) ((faceInfo.mCenter_x)* scaleW);
//        top =  (int) ((faceInfo.mCenter_y) * scaleW);
        rect.top = top < 0 ? 0 : top;
        rect.left = left < 0 ? 0 : left;
        rect.right = (left + width) > frame.getWidth() ? frame.getWidth() : (left + width);
        rect.bottom = (top + height) > frame.getHeight() ? frame.getHeight() : (top + height);
        return rect;
    }
}
