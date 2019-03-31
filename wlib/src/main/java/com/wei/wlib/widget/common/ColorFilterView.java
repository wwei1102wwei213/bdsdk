package com.wei.wlib.widget.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ColorFilterView extends ImageView {

    private Paint myPaint = null;
    private Bitmap bitmap = null;
    private ColorMatrix myColorMatrix = null;
    private float[] colorArray = {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};

    public ColorFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //新建画笔对象
        myPaint = new Paint();
        //描画（原始图片）
        canvas.drawBitmap(bitmap, 0, 0, myPaint);
        //新建颜色矩阵对象
        myColorMatrix = new ColorMatrix();
        //设置颜色矩阵的值
        myColorMatrix.set(colorArray);
        //设置画笔颜色过滤器
        myPaint.setColorFilter(new ColorMatrixColorFilter(myColorMatrix));
        //描画（处理后的图片）
        canvas.drawBitmap(bitmap, 0, 0, myPaint);
    }


    //设置颜色数值
    public void setColorArray(float[] colorArray) {
        this.colorArray = colorArray;
    }

    //设置图片
    public void setBitmap(Bitmap bitmap) {

        this.bitmap = bitmap;
    }

    public float[] getDefaultFilter(){
        float[] array = {1, 0, 0, 0, 0,
                0, 1, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};
        return array;
    }

    public float[] getBaoLiLaiFilter(){
        float[] array = {1.438f, -0.062f, -0.062f, 0, 0,
                -0.122f, 1.378f, -0.122f, 0, 0,
                -0.016f, -0.016f, 1.483f, 0, 0,
                -0.03f, 0.05f, -0.02f, 1, 0};

        return array;
    }

    public float[] getHuiJiuFilter(){
        float[] array = {0.393f,0.769f,0.189f,0,0,
                0.349f,0.686f,0.168f,0,0,
                0.272f,0.534f,0.131f,0,0,
                0,0,0,1,0};
        return array;
    }

    public float[] getRedFilter(){
        float[] array = {2,0,0,0,0,
                0,1,0,0,0,
                0,0,1,0,0,
                0,0,0,1,0 };
        return array;
    }

    public float[] getGreenFilter(){
        float[] array = {1,0,0,0,0,
                0,1.4f,0,0,0,
                0,0,1,0,0,
                0,0,0,1,0  };
        return array;
    }

    public float[] getBlueFilter(){
        float[] array = {1,0,0,0,0,
                0,1,0,0,0,
                0,0,1.6f,0,0,
                0,0,0,1,0 };
        return array;
    }

    public float[] getYelloFilter() {
        float[] array = {1, 0, 0, 0, 50,
                0, 1, 0, 0, 50,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};
        return array;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

}
