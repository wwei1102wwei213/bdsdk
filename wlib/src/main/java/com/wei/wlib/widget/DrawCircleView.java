package com.wei.wlib.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.orhanobut.logger.Logger;


/***
 *  自定义控件
 *  用于登录页面图片轮播的标示，与ViewPager实现联动效果
 *  @user wwei
 */
public class DrawCircleView extends View {
	
	private int r;
	private int hx,sx;
	private int count ;
	private int hollow;
	private int solid;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public DrawCircleView(Context context) {
		super(context);
	}
	
	public DrawCircleView(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	public void setDrawCricle(int count,int r,int hollow,int solid, int w) {
		this.r = r;
		this.hollow = hollow;
		this.solid = solid;
		this.count = count;
		if(this.r == 0) this.r = 8;
		if(this.hollow == 0) this.hollow = Color.parseColor("#555555");
		if(this.solid == 0) this.solid = Color.parseColor("#FFFFFF");
		if(this.count== 0) this.count = 3;
		onSizeChanged(w, 0, 0, 0);
	}	
	@Override
	protected void onDraw(Canvas canvas) {
		paint.setColor(this.hollow);
		paint.setStrokeWidth(1);
		for (int i = 0; i < count; i++) {
			canvas.drawCircle(hx+r*i*4, getHeight()/2, r, paint);
		}
		paint.setColor(this.solid);
		canvas.drawCircle(sx, getHeight()/2, r, paint);
	}
	
	public void redraw(int position){
		sx = hx+position*4*r;
		invalidate();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		/*if (isOdd(count)){

			LogCustom.show("true:"+hx+",count:"+count);
		} else {
			hx = w/2-r*2*(count);
			LogCustom.show("false:"+hx+",count:"+count);
		}*/
		hx = w/2-r*4*(count/2);
		sx = hx;
		Logger.e("onSizeChanged:"+hx+",r:"+r+",c:"+count);
	}

	public boolean isOdd(int a){
		if(a%2 == 1){   //是奇数
			return true;
		}
		return false;
	}
}
