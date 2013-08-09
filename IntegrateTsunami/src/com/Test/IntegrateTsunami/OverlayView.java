package com.Test.IntegrateTsunami;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class OverlayView extends View{
	
	public static String TAG = "";
	public IntegrateTsunamiActivity mainActivity ;

	public OverlayView(Context context) {
		super(context);
		mainActivity = (IntegrateTsunamiActivity)this.getContext();
		TAG = mainActivity.TAG;
		// TODO Auto-generated constructor stub
		
	}
	
	// タッチイベント
	@Override
	public boolean onTouchEvent(MotionEvent event){
		mainActivity.onTouchEventHandler(event);
		return true;
	}
	
	
	@Override
	public void onDraw (Canvas c){
		drawParams(c);
		
	}
	
	public void drawParams (Canvas c){
		
		Paint paint = new Paint();
		paint.setARGB(255,255,255,255);
		String [] a = new String[17];
		a[0] = "magX:" + mainActivity.magX;
		a[1] = "magY:" + mainActivity.magY;
		a[2] = "magZ:" + mainActivity.magZ;
		a[3] = "waterH:" + mainActivity.waterHeight;
		a[4] = "floodH:" + mainActivity.floodHeight;
		a[5] = "userH:" + mainActivity.userHeight;
		a[6] = "rpcsX:" + mainActivity.RPCSX;
		a[7] = "rpcsY:" + mainActivity.RPCSY;
		a[8] = "Lat:" + mainActivity.latitude;
		a[9] = "Lon:" + mainActivity.longitude;
		a[10] = "Alt:" + mainActivity.altitude;
		a[11] = "Acc:" + mainActivity.accuracy + "m";
		a[12] = "offsetRPCSX:" + mainActivity.offsetRPCSX;
		a[13] = "offsetRPCSY:" + mainActivity.offsetRPCSY;
		a[14] = "currentMode:" + mainActivity.currentMode;
		a[15] = "FLAG_SYNC_SENSOR:" + mainActivity.FLAG_SYNC_SENSOR;
		a[16] = "FLAG_SYNC_GPS:" + mainActivity.FLAG_SYNC_GPS;
		int r = 315;
		for(int i=0; i<a.length; i++){
			c.drawText(a[i], 10, r, paint);
			r+=15;
		}
		
		//毎フレームごと描画処理をしたいときはinvalidate()を使う。
		invalidate();
		
	}
}
