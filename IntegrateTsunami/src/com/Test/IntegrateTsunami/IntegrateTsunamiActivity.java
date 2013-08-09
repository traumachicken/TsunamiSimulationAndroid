package com.Test.IntegrateTsunami;

import jp.jasminesoft.gcat.scalc.DMSconv;

import jp.jasminesoft.gcat.scalc.LatLong2XY;
import android.app.ActionBar;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Config;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Master
 *
 * ■カスタムのclass.jarを使用してます．
 * 変更点：UnityPlayerActivity.java
 * 理由：Action Barを表示するため．（デフォルでは非表示）
 * 参考URL：http://bit.ly/17CKLzB
 */


public class IntegrateTsunamiActivity extends UnityPlayerActivity implements SensorEventListener, LocationListener, OnClickListener {
	
	public static final String TAG = "Tsunami";
	
	//東京モデルの左下の平面直角座標
	//public static final double TKYOffsetX = -35998.4575713; 
	//public static final double TKYOffsetY = -7500.4590171;
	
	//宮城大学モデルの平面直角座標
	//public static final double TKYOffsetX = -183453.02320; 
	//public static final double TKYOffsetY = 413.2737542;
	
	//自宅
	public static final double TKYOffsetX = -194398.895; 
	public static final double TKYOffsetY = 7263.217;
	
	//大学
	//public static final double MYOffsetX = 
	
	//CAMERA ROTATION
	
	/************************************
	 * Legacy sensor
	 ************************************/
	
	private SensorManager mSensorManager;
	private Sensor mMagneticField;
	private Sensor mAccelerometer;
	public static float[] mMagneticFieldValues;
	public static float[] mAccelerometerValues;
	public static final int DIMENSION = 3;
	public static final int MATRIX_SIZE = 16;
	private boolean mMagneticFieldRegistered;
	private boolean mAccelerometerRegistered;
	public float magX;
	public float magY;
	public float magZ;
	
	
	/*************************************
	 * Sensor Fusion
	 * ***********************************/
	
	//private SensorManager mSensorManager = null;
	public float fDegX;
	public float fDegY;
	public float fDegZ;
	
    // angular speeds from gyro
    private float[] gyro = new float[3];
 
    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];
 
    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];
 
    // magnetic field vector
    private float[] magnet = new float[3];
 
    // accelerometer vector
    private float[] accel = new float[3];
 
    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];
 
    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];
 
    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;
    
	public static final int TIME_CONSTANT = 30;
	public static final float FILTER_COEFFICIENT = 0.98f;
	private Timer fuseTimer = new Timer();
	
	/********************************************************
	 * Rotation Vector
	 ********************************************************/
	
	public float[] rotationVectorMatrix = new float[9];
	public float[] rotationVectorOrientation = new float[3];
	
	
	/*********************************************************************
	 *  The following members are only for displaying the sensor output.
	 *********************************************************************/
	public Handler mHandler;
	private RadioGroup mRadioGroup;
	private TextView mAzimuthView;
	private TextView mPitchView;
	private TextView mRollView;
	private int radioSelection;
	DecimalFormat d = new DecimalFormat("#.##");
	
	
	/****************************************
	 * Touch Event
	 ****************************************/
	private PointF mLastDown = new PointF(0,0);
	private float touchSensitivity = 15; // 低いほど敏感
	private float touchThreshold = 6; // この値以上の大きさの入力は無視する

	
	/*********************************
	 * GPS
	 *********************************/
	private boolean GPSInit = false ;
	private LocationManager mLocationManager = null;
	private static Location mNowLocation = null;
	public double latitude = 0;
	public double longitude = 0;
	public double altitude = 0;
	public float accuracy = 0;
	public Date locDate ;
	public double RPCSX = 0;
	public double RPCSY = 0;
	public double offsetRPCSX = 0;
	public double offsetRPCSY = 0;
	public double lastRPCSX = 0;
	public double lastRPCSY = 0;
	
	public int kei = 0;

	private float lastMagX = 0;
	private float lastMagY = 0;
	private float lastMagZ = 0;
	
	//Height
	public double userHeight = 1.6; // camera height from ground
	public double overlookHeight = 400 ;
	
	/***********************************
	 * MODE
	 ***********************************/
	public int currentMode = 0;
	public static final int MODE_PERSPECTIVE = 0;
	public static final int MODE_OVERLOOK = 1;
	
	/****************************************************
	 * FLAG
	 *****************************************************/
	public boolean FLAG_SYNC_SENSOR = true ;
	public boolean FLAG_SYNC_GPS = true ;
	public boolean FLAG_SYNC_GROUND = true ;
	public boolean FLAG_GALAXY_FIX = false ;
	private boolean mTouch;
	
	/****************************************************
	 * MENU
	 *****************************************************/
	public static final int MENU_SELECT_PERSPECTIVE = 0;
	public static final int MENU_SELECT_OVERLOOK = 1;
	public static final int MENU_SELECT_SYNC_SENSOR = 2;
	public static final int MENU_SELECT_SYNC_GPS = 3;
	public static final int MENU_SELECT_SYNC_GROUND = 4;
	public static final int MENU_SELECT_RENDERING_OPTION = 5;
	public static final int MENU_SELECT_PREFERENCE = 6;
	
	/****************************************************
	 *  Materialの設定
	 *****************************************************/
	public int currentBuildingMaterial = 0 ;
	public int currentWaterMaterial = 0 ;
	public int currentGroundMaterial = 0 ;
	public static final int MATERIAL_TEXTURE = 0;
	public static final int MATERIAL_TRANSPARENT = 1;
	public static final int MATERIAL_CULLING = 2;
	public static final int MATERIAL_GRID = 3;
	
	/****************************************************
	 *  水面の高さ
	 *****************************************************/
	public float waterHeight = 10; // 水面高（0地点からの）
	
	// Unityからアクセス
	// 無いとエラー
	// 浸水高は地形モデルから算出するのでUnityじゃないと計算できない
	//
	public double floodHeight = 0; // 浸水高（地面からの）
	
	// 未実装
	public boolean FLAG_WAVE_ANIMATION = false ;
	
	// UnityReadyCallback()参照
	public boolean UNITY_READY = false ;
	
	
	/****************************************************
	 *  Layout
	 *****************************************************/
	public ViewFlipper flipper;
	public static final int VIEW_MAP_ID = 0;
	public static final int VIEW_AR_ID = 1;
	public static final int VIEW_WATER_ID = 2;
	public static final int VIEW_FLOOD_ID = 3;
	
	/****************************************************
	 *  mode
	 *****************************************************/
	public static final int MODE_MAP = 0;
	public static final int MODE_AR = 1;
	public static final int MODE_WATER = 2;
	public static final int MODE_FLOOD = 3;
	
	/****************************************************
	 *  NEXUS7用
	 *****************************************************/
	public static final boolean MODE_NEXUS = true;
	
	/****************************************************
	 *  DEBUGモードオンオフ
	 *****************************************************/
	private static final boolean DEBUG = true;
	
	
	
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        
        // Sensorの初期化
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();
        
        //gyroSetup();
        
        /*
		if (mSensorManager != null) {
			mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		*/
		// GPSの初期化
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		
		
        // Viewの初期化
        viewSetup();
        
        // GPSの値を取得するまで初期位置に設定
        RPCSX = offsetRPCSX ;
        RPCSY = offsetRPCSY ;
        lastRPCSX = RPCSX ;
        lastRPCSY = RPCSY ;
        
        // test
        setUserHeight(1.7);
        UpdateWaveAnimationState();
    }
    
    
    
    
    
    public void viewSetup(){
    	
    	/*
    	 * OverlayViewの追加
    	 * TouchEventを取得するのに必要+デバッグパラメータの描画
    	 * UnityをExtendしたこのviewではUnityがtouchEventを横取りするため．
    	 */
    	addContentView(new OverlayView(this), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	
    	/*
    	 * MainViewの追加
    	 * 
    	 */
    	View view = this.getLayoutInflater().inflate(R.layout.main, null);
    	addContentView(view,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    	
    	/*
    	 * Action barを表示させる．
    	 * 
    	 * getActionBar();
    	 * setContent以降でないとnullを返す．
    	 */
    	ActionBar ab = getActionBar();
        if( ab == null){
        	Log.d(TAG, "ba = null");
        }else{
	        ab.show();
	    	Log.d(TAG, "Action bar show");
        }
    	
    	/* 
    	 * main view UI
    	 * store button
    	 */
        
    	Button flipNextButton = (Button)findViewById(R.id.flip_next_btn);
    	flipNextButton.setOnClickListener((OnClickListener) this);
    	Button flipPreviousButton = (Button)findViewById(R.id.flip_previous_btn);
    	flipPreviousButton.setOnClickListener((OnClickListener) this);
    	
    	flipper = (ViewFlipper)findViewById(R.id.flipper);
    	   	
    }
    
    // Util
    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];
     
        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);
     
        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;
     
        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;
     
        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;
     
        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }
    
    
    // Util
    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];
     
        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];
     
        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];
     
        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];
     
        return result;
    }
    
    
    
    
    
    
    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.
    public void initListeners(){
    	
    	// 不要？
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST);
        
        // 不要？
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST);
        
        // 不要？
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_FASTEST);
        
        mSensorManager.registerListener(this,
        	mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
        	SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    
    @Override
	protected void onResume() {
				
		if (DEBUG)
			Log.d(TAG, "onResume");
		
		initListeners();
		
		//GPS
		if (mLocationManager != null)
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		super.onResume();
	}
    
    
    @Override
	protected void onPause() {
		
    	// unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    	
		//GPS
		if(mLocationManager!=null)
			mLocationManager.removeUpdates(this);
		
		super.onPause();
	}
    
    @Override
	protected void onStop() {
		super.onStop();
		mSensorManager.unregisterListener(this);
	}
    
    
    //センサーの更新
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		switch (event.sensor.getType()) {
		case Sensor.TYPE_MAGNETIC_FIELD:
			//mMagneticFieldValues = event.values.clone();
			System.arraycopy(event.values, 0, magnet, 0, 3);
			break;
		case Sensor.TYPE_ACCELEROMETER:
			// copy new accelerometer data into accel array and calculate orientation
	        System.arraycopy(event.values, 0, accel, 0, 3);
	        
			//mAccelerometerValues = event.values.clone();
			break;
		case Sensor.TYPE_ROTATION_VECTOR:
			// Sensor => rotationVectorMatrix
			SensorManager.getRotationMatrixFromVector(rotationVectorMatrix, event.values);
			
			// rotationVectorMatrix => remapedMatrix
			float[] remapedMatrix = new float[9];
			SensorManager.remapCoordinateSystem(rotationVectorMatrix,
					SensorManager.AXIS_X, // TODO reverse Z
					SensorManager.AXIS_Z,
					remapedMatrix);
			
			// remapedMatrix => rotationVectorOrientation
			SensorManager.getOrientation(remapedMatrix,
					rotationVectorOrientation);
			
			// rotationVectorOrientation => X, Y, Z
			magX = (float) Math.toDegrees(rotationVectorOrientation[1]);
			magY = (float) Math.toDegrees(rotationVectorOrientation[2]);
			magZ = (float) Math.toDegrees(rotationVectorOrientation[0]);
			
			if ( MODE_NEXUS )
			{
				magX *= -1;
				magY *= -1;
				
				// calculate the facing direction of the front camera
				//if (magZ > 0)	magZ -= 180f;
				//else			magZ += 180f;
			}
			
			
			if(FLAG_SYNC_SENSOR)	UpdateCameraRotationBySensor();
		}
		
		/*
		if (mMagneticFieldValues != null && mAccelerometerValues != null) {
			float[] rotationMatrix = new float[MATRIX_SIZE];
			float[] inclinationMatrix = new float[MATRIX_SIZE];
			float[] remapedMatrix = new float[MATRIX_SIZE];
			float[] orientationValues = new float[MATRIX_SIZE];

			SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix,
					mAccelerometerValues, mMagneticFieldValues);
			SensorManager.remapCoordinateSystem(rotationMatrix,
					SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedMatrix);
			SensorManager.getOrientation(remapedMatrix, orientationValues);

			magX = (float) Math.toDegrees(orientationValues[1]);
			magY = (float) Math.toDegrees(orientationValues[2]);
			magZ = (float) Math.toDegrees(orientationValues[0]);
			
			if ( MODE_NEXUS )
			{
				magX *= -1;
				magY *= -1;
				
				// calculate the facing direction of the front camera
				if (magZ > 0)	magZ -= 180f;
				else			magZ += 180f;
			}
		}
		*/
		
		
	}
	
	
	// GPSの更新ハンドラ
	@Override
	public void onLocationChanged(Location arg0) {
		
		// 緯度・経度・標高・制度・日付の取得
		mNowLocation = arg0 ;
		latitude = (double) mNowLocation.getLatitude(); //digit
		longitude = (double) mNowLocation.getLongitude(); //digit
		altitude = (double) mNowLocation.getAltitude();
		accuracy = (float) mNowLocation.getAccuracy();
		locDate	= new Date(mNowLocation.getTime());
		
		// 平面直角座標系へ変換
		LatLong2XY ll2xy = new LatLong2XY(-1);
		
		//　10進数からDMSへ変換
		ll2xy.setLatitude(DMSconv.deg2dms((float)latitude));
		ll2xy.setLongitude(DMSconv.deg2dms((float)longitude));
		
		// 変数へ代入
		kei = ll2xy.getKei();
		RPCSX = ll2xy.getX();
		RPCSY = ll2xy.getY();
		
		// Debug
		if(DEBUG)
			Log.d(TAG, ""+kei+":"+DMSconv.deg2dms((float)latitude)+","+DMSconv.deg2dms((float)longitude)+","+RPCSX+","+RPCSY);
		
		// 初期動作
		// とりあえず現在地でマップが見えるオフセット値に設定
		if(GPSInit == false){
			//setOffsetRPCS(RPCSX, RPCSY);
			GPSInit = true ;
		}
		
		// GPS同期がオンならアップデート
		if(FLAG_SYNC_GPS)	UpdateCameraPositionByGPS();
	}
	
	//　GPSでカメラを移動
	public void UpdateCameraPositionByGPS() {
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSX", String.valueOf(RPCSX - offsetRPCSX));
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSY", String.valueOf(RPCSY - offsetRPCSY));
		lastRPCSX = RPCSX;
		lastRPCSY = RPCSY;
	}

	// タッチでカメラを移動
	public void UpdateCameraPositionByTouch() {
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSX", String.valueOf(lastRPCSX - offsetRPCSX));
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSY", String.valueOf(lastRPCSY - offsetRPCSY));
	}

	// モデルの原点の直角平面座標で設定
	public void setOffsetRPCS( double ox, double oy ){
		offsetRPCSX = ox ;
		offsetRPCSY = oy ;
	}
	
	// モデルの原点を緯度経度で設定
	// 使ってない？
	public void setOffsetLLCS( double ox, double oy ){
		LatLong2XY ll2xy = new LatLong2XY(-1);
		ll2xy.setLatitude(DMSconv.deg2dms((float)latitude));
		ll2xy.setLongitude(DMSconv.deg2dms((float)longitude));
		setOffsetRPCS(ll2xy.getX(), ll2xy.getY());
	}
	
	// ユーザーの地面からの高さ
	public void setUserHeight( double height ){
		userHeight = height ;
		// Unity側更新
		UpdateUserHeight(userHeight);
	}

	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {	}
	
	// タッチイベントハンドラー
	// OverlayViewからコールバック
	//　デフォルトのはUnityが横取りするので使えない
	public boolean onTouchEventHandler(MotionEvent event){
		
		// debug log
		Log.d(TAG, ""+event.getAction());
		
		// 
		switch(event.getAction()){
		
			case MotionEvent.ACTION_DOWN: // 押されたとき
				
				//　タッチフラグ（使ってない）
				mTouch = true ;

				// 手動調整開始と同時に各同期をオフにする
				if(currentMode == MODE_AR){
					if ( FLAG_SYNC_SENSOR == true ){
						FLAG_SYNC_SENSOR = false ;
						Toast.makeText(this, "カメラの同期オフ", Toast.LENGTH_SHORT).show();
					}
				}
				if(currentMode == MODE_MAP){
					if ( FLAG_SYNC_GPS == true ){
						FLAG_SYNC_GPS = false ;
						Toast.makeText(this, "GPSの同期オフ", Toast.LENGTH_SHORT).show();
					}
				}
				
				// 最期にタッチされた位置を記憶する
				updateLastTouch(event);
				
				break;
				
			case MotionEvent.ACTION_UP: // 離されたとき
				
				// 同期が切れるのでボタンを表示
				if(currentMode == MODE_PERSPECTIVE && !FLAG_SYNC_SENSOR){
					// 同期ボタンの追加
				}
				
				// 同期が切れるのでボタンを表示
				if(currentMode == MODE_OVERLOOK && !FLAG_SYNC_GPS){
					// 同期ボタンの追加
				}
				
				// タッチオフ（使ってない）
				mTouch = false ;
				
				break;
			case MotionEvent.ACTION_MOVE: //　ドラッグ時
				
				// ドラッグ量を取得
				PointF diff = touchMove(event);
				
				// 現在のモードによって動作を変更
				if		(currentMode == MODE_AR)		scrollCameraAngle(diff);
				else if	(currentMode == MODE_MAP)		scrollCameraPosition(diff);	
				else if (currentMode == MODE_WATER)		scrollUserHeight(diff);
				else if (currentMode == MODE_FLOOD) 	scrollWaterHeight(diff);
				break;

			default:
				break;
		}
		return false;
	}
	
	public void updateLastTouch(MotionEvent event){
		// 最期にタッチされた位置
		mLastDown.x = event.getX();
		mLastDown.y = event.getY();
	}
	
	// ドラッグ量を返す
	public PointF touchMove(MotionEvent event){
		
		PointF diff = new PointF();
		
		// 最期にタッチした位置からドラッグ量を計算
		diff.x = mLastDown.x - event.getX();
		diff.y = mLastDown.y - event.getY();
		
		// 最期にタッチした位置を更新 
		updateLastTouch(event);
		
		// ドラッグ量を返す
		return diff;
	}
	
	// スワイプ操作でカメラの向きを変更
	public void scrollCameraAngle(PointF diff){
		lastMagZ += diff.x / touchSensitivity;
		lastMagX += diff.y / touchSensitivity;
		
		UpdateCameraRotationByTouch();
	}
	
	// スワイプ操作でカメラ位置を変更
	private void scrollCameraPosition(PointF diff) {
		lastRPCSX -= diff.y / touchSensitivity;
		lastRPCSY += diff.x / touchSensitivity;
		
		UpdateCameraPositionByTouch();
	}
	
	// スワイプ操作でカメラの高さを変更
	private void scrollUserHeight(PointF diff) {
		userHeight += diff.y / ( touchSensitivity * 8 );
		UpdateUserHeight(userHeight);
	}
	
	// スワイプ操作で水位を変更
	private void scrollWaterHeight(PointF diff) {
		waterHeight += diff.y / ( touchSensitivity * 8 );
		UpdateWaterHeight();
	}
	
	/*
	 * 
	 * オプションメニューの初期化(non-Javadoc)
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.game_menu, menu);
	    return true;
	}
	
	/*
	 * 
	 * 
	 * 
	 * オプションメニューボタンの設定
	 * 
	 * 
	 * 
	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.SET_ORIGIN) {
			setOffsetRPCS(0, 0);
			
			ForceUpdatePosition();
			Toast.makeText(this, "原点リセット", Toast.LENGTH_SHORT).show();
			return true ;
			
		} else if (item.getItemId() == R.id.SET_LOCAL){
			setOffsetRPCS(TKYOffsetX, TKYOffsetY);
			ForceUpdatePosition();
			Toast.makeText(this, "現地モード", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_SENSOR){
			FLAG_SYNC_SENSOR = !FLAG_SYNC_SENSOR;
			if(FLAG_SYNC_SENSOR)
				Toast.makeText(this, "カメラ角を同期", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "カメラ角を非同期", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_GPS){
			FLAG_SYNC_GPS = !FLAG_SYNC_GPS;
			if(FLAG_SYNC_GPS)
				Toast.makeText(this, "GPSを同期", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "GPSを非同期", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_GROUND){
			FLAG_SYNC_GROUND = !FLAG_SYNC_GROUND;
			setGrounding();
			if(FLAG_SYNC_GROUND)
				Toast.makeText(this, "接地モードオン", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "接地モードオフ", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_PREFERENCE){
			return true;
		} else if (item.getItemId() == R.id.NEXT_BUILDING_MATERIAL){
			UpdateBuildingMaterial();
			Toast.makeText(this, "建物のマテリアルを切り替え", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.NEXT_WATER_MATERIAL){
			UpdateWaterMaterial();
			Toast.makeText(this, "水面のマテリアルを切り替え", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.NEXT_GROUND_MATERIAL){
			UpdateGroundMaterial();
			Toast.makeText(this, "地面のマテリアルを切り替え", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.GALAXY_FIX){
			ToggleGalaxyFix();
			Toast.makeText(this, "GALAXY FIXED", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.CAPTURE_SCREEN){
			CaptureScreen();
			Toast.makeText(this, "スクリーンショットを撮影しました", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.MINIMAP_TOGGLE){
			Toast.makeText(this, "ミニマップの切り替え", Toast.LENGTH_SHORT).show();
			ToggleMinimapExpand();
			return true;
		} else if (item.getItemId() == R.id.PIN_CURRENT_LOC){
			PinCurrentLocation();
			Toast.makeText(this, "現在地にピンを追加しました", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.CLEAR_PIN){
			ClearAllPin();
			Toast.makeText(this, "すべてのピンを削除しました", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	

	/*
	 * キーアップ，キーダウンハンドラ
	 * UnityPlayerActivityがTrueを返すとメニューが
	 * 表示されないのでオーバーライドする必要がある．
	 * 
	 * 
	 * 
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( keyCode == KeyEvent.KEYCODE_MENU )	return false;
		if( keyCode == KeyEvent.KEYCODE_HOME )	{
			finish();
			return true ;
		}
        return super.onKeyDown(keyCode, event); 
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if( keyCode == KeyEvent.KEYCODE_MENU )	return false;
        return super.onKeyDown(keyCode, event); 
	}
	
	
	
	/* 
	 * ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
	 * TODO:API13に必要か？
	 */
	 @Override
	 public void onUserLeaveHint(){
	        finish();
	 }

	// ボタンハンドラ
	@Override
	public void onClick(View v) {
		
		if(v.getId()==R.id.flip_next_btn){
			flipper.showNext();
		}else if(v.getId()==R.id.flip_previous_btn){
			flipper.showPrevious();
		}
		
		// 現在のモードを格納
		currentMode = flipper.indexOfChild(flipper.getCurrentView());
		
		// DEBUG
		if(DEBUG){
			Log.d(TAG, ""+flipper.getCurrentView().getId());
			Log.d(TAG, "index"+flipper.indexOfChild(flipper.getCurrentView()));
		}
	}

	/****************************************************
	 * Unityに送信する関数 
	 ****************************************************/
	
	
	/*
	 * カメラのアングルの更新
	 * 
	 */
	public void UpdateCameraRotationBySensor() {
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleX", String.valueOf(magX));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleY", String.valueOf(magY));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleZ", String.valueOf(magZ));
		lastMagX = magX;
		lastMagY = magY;
		lastMagZ = magZ;
	}
	
	/*
	 * カメラのアングルの手動更新
	 * 
	 */
	public void UpdateCameraRotationByTouch() {
		// Log.d(TAG, ""+lastMagX+", "+lastMagY+", "+lastMagZ);
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleX", String.valueOf(lastMagX));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleY", String.valueOf(lastMagY));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleZ", String.valueOf(lastMagZ));
	}
	
	/*
	 * 位置を即時反映
	 * スムースを使わない
	 */
	public void ForceUpdatePosition() {
		UpdateCameraPositionByGPS();
		UnityPlayer.UnitySendMessage("Main Camera", "forceUpdateLocation", "");
	}
	
	/*
	 * カメラの高さ
	 * Unity送信
	 */
	public void UpdateUserHeight(double height) {
		UnityPlayer.UnitySendMessage("Main Camera", "setUserHeight", String.valueOf(height));
	}
	
	/*
	 * 地面に立つかどうか
	 * Unity送信
	 */
	public void setGrounding() {
		UnityPlayer.UnitySendMessage("Main Camera", "setGrounding", String.valueOf(FLAG_SYNC_GROUND));
	}
	
	/*
	 * 建物のマテリアルのサイクルを次へ
	 * 
	 */
	public void UpdateBuildingMaterial(){
		currentBuildingMaterial += 1;
		if(currentBuildingMaterial > 3)	currentBuildingMaterial = 0;
		UnityPlayer.UnitySendMessage("Buildings", "setMaterialMode", String.valueOf(currentBuildingMaterial));
	}
	
	/*
	 * 水面マテリアル
	 * サイクルを次へ
	 * 
	 */
	public void UpdateWaterMaterial(){
		currentWaterMaterial += 1;
		if(currentWaterMaterial > 3)	currentWaterMaterial = 0;
		UnityPlayer.UnitySendMessage("Water Surface", "setMaterialMode", String.valueOf(currentWaterMaterial));
	}
	
	/*
	 * 地形マテリアル
	 * サイクルを次へ
	 * 
	 */
	public void UpdateGroundMaterial(){
		currentGroundMaterial += 1;
		if(currentGroundMaterial > 3)	currentGroundMaterial = 0;
		UnityPlayer.UnitySendMessage("Ground Surface", "setMaterialMode", String.valueOf(currentGroundMaterial));
	}
	
	/*
	 * 水面の高さを送信
	 * 
	 */
	public void UpdateWaterHeight(){
		UnityPlayer.UnitySendMessage("Water Surface", "setHeight", String.valueOf(waterHeight));
	}
	
	//　未実装
	public void UpdateWaveAnimationState(){
		//UnityPlayer.UnitySendMessage("", "setWavingAnimation", String.valueOf(FLAG_WAVE_ANIMATION));
	}
	
	// Galaxyバグ対応
	// Unity送信
	public void ToggleGalaxyFix(){
		FLAG_GALAXY_FIX = !FLAG_GALAXY_FIX ;
		UnityPlayer.UnitySendMessage("Main Camera", "setGalaxyFix", String.valueOf(FLAG_GALAXY_FIX));
	}
	
	// スクリーンショットの撮影
	// Unity送信
	public void CaptureScreen(){
		UnityPlayer.UnitySendMessage("Main Camera", "captureScreen", "");
	}
	
	// ミニマップを最大化する．
	// トグル操作
	public void ToggleMinimapExpand(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidToggleMap", "");
	}
	
	// ミニマップの現在地にピンを刺す
	public void PinCurrentLocation(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidPinCurrentLocation", "");
	}
	
	// ミニマップのピンをクリア
	public void ClearAllPin(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidClearPin", "");
	}
	
	/*
	 * Unity起動後に呼びに来る。
	 * 正確にはGame ControllerがStart()したとき。
	 */
	public void UnityReadyCallback(){
		UNITY_READY = true;
	}
	
	// touch に配置
	// ？
	// ？
	public void Test(){
		UpdateWaterHeight();
		//Log.d(TAG, "floodHeight: "+floodHeight);
	}
}