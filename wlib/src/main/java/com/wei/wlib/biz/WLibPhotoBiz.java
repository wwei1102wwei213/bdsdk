package com.wei.wlib.biz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.wei.wlib.R;
import com.wei.wlib.util.WLibLog;
import com.wei.wlib.widget.alertview.AlertView;
import com.wei.wlib.widget.alertview.OnItemClickListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/4/17 0017.
 *
 * @author wwei
 */
public class WLibPhotoBiz {

    public static final int PHOTO_HRAPH = 119;// 拍照
    public static final int PHOTO_ZOOM = 120; // 缩放
    public static final int PHOTO_RESOULT = 121;// 结果
    public static final int NONE = 0;//没有选择图片
    public static final String IMAGE_UNSPECIFIED = "image/*";

    private Context context;
    private Uri tempUri = null;
    private boolean isNeedFile = false;
    private TakePhotoListener takePhotoListener;

    public WLibPhotoBiz(Context context, TakePhotoListener takePhotoListener, boolean isNeedFile){
        this.context = context;
        this.takePhotoListener = takePhotoListener;
        this.isNeedFile = isNeedFile;
    }

    public void takePhoto() {
        try {
            new AlertView("上传头像方式", null, "取消", null,
                    new String[]{"拍照", "从相册中选择"},
                    context, AlertView.Style.ActionSheet, new OnItemClickListener() {
                @Override
                public void onItemClick(Object o, int position) {
                    switch (position) {
                        case 0:
                            toCamera();
                            break;
                        case 1:
                            toPhoto();
                            break;
                    }
                }
            }).setCancelable(true).show();
        }catch (Exception e){
            WLibLog.e(e, "WLibPhotoBiz takePhoto");
        }
    }

    /**
     * 从拍照中获取图片
     */
    private void toCamera() {
        try {
            //创建一个file，用来存储拍照后的照片
            File outputfile = new File(context.getExternalCacheDir(),"output.png");
            try {
                if (outputfile.exists()){
                    outputfile.delete();//删除
                }
                outputfile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Uri imageuri ;
            if (Build.VERSION.SDK_INT >= 24){
                imageuri = FileProvider.getUriForFile(context,
                        context.getResources().getString(R.string.wlib_provider_authorities),
                        outputfile);
            }else{
                imageuri = Uri.fromFile(outputfile);
            }
            //启动相机程序
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
            ((Activity) context).startActivityForResult(intent,PHOTO_HRAPH);
        }catch (Exception e){
            WLibLog.e(e, "toCamera");
        }
    }

    /**
     * 从相册中获取图片
     */
    private void toPhoto() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
            ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            if (componentName != null) {
                ((Activity) context).startActivityForResult(intent, PHOTO_ZOOM);
            } else {
                Toast.makeText(context, "无法连接到相册", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            WLibLog.e(e, "toPhoto");
        }
    }

    /**
     * 处理拍照
     * @param camerapath 路径
     * @param imgname img 的名字
     * @return
     */
    private void handleCamera(String camerapath, String imgname) {
        try {
            //初始化 uri
            Uri imageUri = null; //返回来的 uri
            Uri outputUri = null; //真实的 uri
            Intent intent = new Intent("com.android.camera.action.CROP");
            //拍照留下的图片
            File camerafile = new File(camerapath,imgname);
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                imageUri = FileProvider.getUriForFile(context, context.getResources().
                        getString(R.string.wlib_provider_authorities), camerafile);
            } else {
                imageUri = Uri.fromFile(camerafile);
            }
            //设置裁剪之后的图片路径文件
            File cutfile = new File(Environment.getExternalStorageDirectory(),
                    "temp_cut_camera.png"); //随便命名一个
            if (cutfile.exists()){ //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cutfile.delete();
            }
            cutfile.createNewFile();
            outputUri = Uri.fromFile(cutfile);
            toCut(intent, imageUri, outputUri);
        } catch (Exception e) {
            WLibLog.e(e, "handlePhoto");
        }
    }

    /**
     * 处理选择的图片
     * @param uri
     * @return
     */
    private void handlePhoto(Uri uri) {
        try {
            //直接裁剪
            Intent intent = new Intent("com.android.camera.action.CROP");
            //设置裁剪之后的图片路径文件
            File cutfile = new File(Environment.getExternalStorageDirectory().getPath(),
                    "temp_cut_camera.png"); //随便命名一个
            if (cutfile.exists()){ //如果已经存在，则先删除,这里应该是上传到服务器，然后再删除本地的，没服务器，只能这样了
                cutfile.delete();
            }
            cutfile.createNewFile();
            //初始化 uri
            Uri outputUri = null; //真实的 uri
            outputUri = Uri.fromFile(cutfile);
            toCut(intent, uri, outputUri);
        } catch (Exception e) {
            WLibLog.e(e, "handlePhoto");
        }
    }

    /**
     *
     * 裁剪
     *
     * @param
     */
    private void toCut(Intent intent, Uri uri, Uri outputUri){
        try {
            //把这个 uri 提供出去，就可以解析成 bitmap了
            tempUri = outputUri;
            // crop为true是设置在开启的intent中设置显示的view可以剪裁
            intent.putExtra("crop",true);
            // aspectX,aspectY 是宽高的比例，这里设置正方形
            intent.putExtra("aspectX",1);
            intent.putExtra("aspectY",1);
            //设置要裁剪的宽高
            intent.putExtra("outputX", 200); //200dp
            intent.putExtra("outputY",200);
            intent.putExtra("scale",true);
            //如果图片过大，会导致oom，这里设置为false
            intent.putExtra("return-data",false);
            if (uri != null) {
                intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
            }
            if (outputUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            }
            intent.putExtra("noFaceDetection", true);
            //压缩图片
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            if (componentName != null) {
                ((Activity)context).startActivityForResult(intent, PHOTO_RESOULT);
            } else {
                Toast.makeText(context, "无法连接到系统裁剪功能", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e){
            Toast.makeText(context, "无法连接到系统裁剪功能", Toast.LENGTH_SHORT).show();
            WLibLog.e(e, "图片裁剪错误");
        }
    }

    private File tempFile = null;
    public void handleActivityResult(int requestCode, int resultCode, Intent data){
        try {
            if (resultCode == NONE) {
                Toast.makeText(context, "没有选择照片", Toast.LENGTH_SHORT).show();
                return;
            }
            // 拍照
            if (requestCode == PHOTO_HRAPH) {
                try {
                    String path = context.getExternalCacheDir().getPath();
                    String name = "output.png";
                    handleCamera(path, name);
                }catch (Exception e){
                    WLibLog.e(e,"requestCode");
                }
            }
            if (data == null) return;
            // 读取相册缩放图片
            if (requestCode == PHOTO_ZOOM) {
                handlePhoto(data.getData());
            }
            // 处理结果
            if (requestCode == PHOTO_RESOULT) {
                try {
                    Uri reUrl = data.getData();
                    if (reUrl == null&&tempUri!=null){
                        reUrl = tempUri;
                        tempUri = null;
                    }
                    if (isNeedFile) {
                        Bitmap photo = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(reUrl));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int options = 100;
                        photo.compress(Bitmap.CompressFormat.JPEG, options, baos);
                        FileOutputStream fos = null;
                        tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());
                        try {
                            fos = new FileOutputStream(tempFile);
                            fos.write(baos.toByteArray());
                            fos.flush();
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        takePhotoListener.onTakePhotoListener(reUrl, photo, tempFile);
                    } else {
                        takePhotoListener.onTakePhotoListener(reUrl, null, null);
                    }

                }catch (Exception e){
                    WLibLog.e(e,"handleActivityResult PHOTO_RESOULT");
                }
            }
        }catch (Exception e){
            WLibLog.e(e, "handleActivityResult");
        }
    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return "TEMP_"+dateFormat.format(date) + ".png";
    }

    public void clear(){
        try {
            if (tempUri!=null) tempUri=null;
            if (tempFile!=null) {
                tempFile.delete();
            }
        } catch (Exception e){
            WLibLog.e(e);
        }
    }
    public interface TakePhotoListener{
        void onTakePhotoListener(Uri uri, Bitmap bmp, File resultFile);
    }

}
