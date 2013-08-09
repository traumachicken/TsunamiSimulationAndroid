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
 * ���J�X�^����class.jar���g�p���Ă܂��D
 * �ύX�_�FUnityPlayerActivity.java
 * ���R�FAction Bar��\�����邽�߁D�i�f�t�H���ł͔�\���j
 * �Q�lURL�Fhttp://bit.ly/17CKLzB
 */


public class IntegrateTsunamiActivity extends UnityPlayerActivity implements SensorEventListener, LocationListener, OnClickListener {
	
	public static final String TAG = "Tsunami";
	
	//�������f���̍����̕��ʒ��p���W
	//public static final double TKYOffsetX = -35998.4575713; 
	//public static final double TKYOffsetY = -7500.4590171;
	
	//�{���w���f���̕��ʒ��p���W
	//public static final double TKYOffsetX = -183453.02320; 
	//public static final double TKYOffsetY = 413.2737542;
	
	//����
	public static final double TKYOffsetX = -194398.895; 
	public static final double TKYOffsetY = 7263.217;
	
	//��w
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
	private float touchSensitivity = 15; // �Ⴂ�قǕq��
	private float touchThreshold = 6; // ���̒l�ȏ�̑傫���̓��͖͂�������

	
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
	 *  Material�̐ݒ�
	 *****************************************************/
	public int currentBuildingMaterial = 0 ;
	public int currentWaterMaterial = 0 ;
	public int currentGroundMaterial = 0 ;
	public static final int MATERIAL_TEXTURE = 0;
	public static final int MATERIAL_TRANSPARENT = 1;
	public static final int MATERIAL_CULLING = 2;
	public static final int MATERIAL_GRID = 3;
	
	/****************************************************
	 *  ���ʂ̍���
	 *****************************************************/
	public float waterHeight = 10; // ���ʍ��i0�n�_����́j
	
	// Unity����A�N�Z�X
	// �����ƃG���[
	// �Z�����͒n�`���f������Z�o����̂�Unity����Ȃ��ƌv�Z�ł��Ȃ�
	//
	public double floodHeight = 0; // �Z�����i�n�ʂ���́j
	
	// ������
	public boolean FLAG_WAVE_ANIMATION = false ;
	
	// UnityReadyCallback()�Q��
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
	 *  NEXUS7�p
	 *****************************************************/
	public static final boolean MODE_NEXUS = true;
	
	/****************************************************
	 *  DEBUG���[�h�I���I�t
	 *****************************************************/
	private static final boolean DEBUG = true;
	
	
	
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        
        // Sensor�̏�����
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        initListeners();
        
        //gyroSetup();
        
        /*
		if (mSensorManager != null) {
			mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		*/
		// GPS�̏�����
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		
		
        // View�̏�����
        viewSetup();
        
        // GPS�̒l���擾����܂ŏ����ʒu�ɐݒ�
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
    	 * OverlayView�̒ǉ�
    	 * TouchEvent���擾����̂ɕK�v+�f�o�b�O�p�����[�^�̕`��
    	 * Unity��Extend��������view�ł�Unity��touchEvent������肷�邽�߁D
    	 */
    	addContentView(new OverlayView(this), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	
    	/*
    	 * MainView�̒ǉ�
    	 * 
    	 */
    	View view = this.getLayoutInflater().inflate(R.layout.main, null);
    	addContentView(view,new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    	
    	/*
    	 * Action bar��\��������D
    	 * 
    	 * getActionBar();
    	 * setContent�ȍ~�łȂ���null��Ԃ��D
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
    	
    	// �s�v�H
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST);
        
        // �s�v�H
        mSensorManager.registerListener(this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST);
        
        // �s�v�H
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
    
    
    //�Z���T�[�̍X�V
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
	
	
	// GPS�̍X�V�n���h��
	@Override
	public void onLocationChanged(Location arg0) {
		
		// �ܓx�E�o�x�E�W���E���x�E���t�̎擾
		mNowLocation = arg0 ;
		latitude = (double) mNowLocation.getLatitude(); //digit
		longitude = (double) mNowLocation.getLongitude(); //digit
		altitude = (double) mNowLocation.getAltitude();
		accuracy = (float) mNowLocation.getAccuracy();
		locDate	= new Date(mNowLocation.getTime());
		
		// ���ʒ��p���W�n�֕ϊ�
		LatLong2XY ll2xy = new LatLong2XY(-1);
		
		//�@10�i������DMS�֕ϊ�
		ll2xy.setLatitude(DMSconv.deg2dms((float)latitude));
		ll2xy.setLongitude(DMSconv.deg2dms((float)longitude));
		
		// �ϐ��֑��
		kei = ll2xy.getKei();
		RPCSX = ll2xy.getX();
		RPCSY = ll2xy.getY();
		
		// Debug
		if(DEBUG)
			Log.d(TAG, ""+kei+":"+DMSconv.deg2dms((float)latitude)+","+DMSconv.deg2dms((float)longitude)+","+RPCSX+","+RPCSY);
		
		// ��������
		// �Ƃ肠�������ݒn�Ń}�b�v��������I�t�Z�b�g�l�ɐݒ�
		if(GPSInit == false){
			//setOffsetRPCS(RPCSX, RPCSY);
			GPSInit = true ;
		}
		
		// GPS�������I���Ȃ�A�b�v�f�[�g
		if(FLAG_SYNC_GPS)	UpdateCameraPositionByGPS();
	}
	
	//�@GPS�ŃJ�������ړ�
	public void UpdateCameraPositionByGPS() {
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSX", String.valueOf(RPCSX - offsetRPCSX));
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSY", String.valueOf(RPCSY - offsetRPCSY));
		lastRPCSX = RPCSX;
		lastRPCSY = RPCSY;
	}

	// �^�b�`�ŃJ�������ړ�
	public void UpdateCameraPositionByTouch() {
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSX", String.valueOf(lastRPCSX - offsetRPCSX));
		UnityPlayer.UnitySendMessage("Main Camera", "setRPCSY", String.valueOf(lastRPCSY - offsetRPCSY));
	}

	// ���f���̌��_�̒��p���ʍ��W�Őݒ�
	public void setOffsetRPCS( double ox, double oy ){
		offsetRPCSX = ox ;
		offsetRPCSY = oy ;
	}
	
	// ���f���̌��_���ܓx�o�x�Őݒ�
	// �g���ĂȂ��H
	public void setOffsetLLCS( double ox, double oy ){
		LatLong2XY ll2xy = new LatLong2XY(-1);
		ll2xy.setLatitude(DMSconv.deg2dms((float)latitude));
		ll2xy.setLongitude(DMSconv.deg2dms((float)longitude));
		setOffsetRPCS(ll2xy.getX(), ll2xy.getY());
	}
	
	// ���[�U�[�̒n�ʂ���̍���
	public void setUserHeight( double height ){
		userHeight = height ;
		// Unity���X�V
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
	
	// �^�b�`�C�x���g�n���h���[
	// OverlayView����R�[���o�b�N
	//�@�f�t�H���g�̂�Unity������肷��̂Ŏg���Ȃ�
	public boolean onTouchEventHandler(MotionEvent event){
		
		// debug log
		Log.d(TAG, ""+event.getAction());
		
		// 
		switch(event.getAction()){
		
			case MotionEvent.ACTION_DOWN: // �����ꂽ�Ƃ�
				
				//�@�^�b�`�t���O�i�g���ĂȂ��j
				mTouch = true ;

				// �蓮�����J�n�Ɠ����Ɋe�������I�t�ɂ���
				if(currentMode == MODE_AR){
					if ( FLAG_SYNC_SENSOR == true ){
						FLAG_SYNC_SENSOR = false ;
						Toast.makeText(this, "�J�����̓����I�t", Toast.LENGTH_SHORT).show();
					}
				}
				if(currentMode == MODE_MAP){
					if ( FLAG_SYNC_GPS == true ){
						FLAG_SYNC_GPS = false ;
						Toast.makeText(this, "GPS�̓����I�t", Toast.LENGTH_SHORT).show();
					}
				}
				
				// �Ŋ��Ƀ^�b�`���ꂽ�ʒu���L������
				updateLastTouch(event);
				
				break;
				
			case MotionEvent.ACTION_UP: // �����ꂽ�Ƃ�
				
				// �������؂��̂Ń{�^����\��
				if(currentMode == MODE_PERSPECTIVE && !FLAG_SYNC_SENSOR){
					// �����{�^���̒ǉ�
				}
				
				// �������؂��̂Ń{�^����\��
				if(currentMode == MODE_OVERLOOK && !FLAG_SYNC_GPS){
					// �����{�^���̒ǉ�
				}
				
				// �^�b�`�I�t�i�g���ĂȂ��j
				mTouch = false ;
				
				break;
			case MotionEvent.ACTION_MOVE: //�@�h���b�O��
				
				// �h���b�O�ʂ��擾
				PointF diff = touchMove(event);
				
				// ���݂̃��[�h�ɂ���ē����ύX
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
		// �Ŋ��Ƀ^�b�`���ꂽ�ʒu
		mLastDown.x = event.getX();
		mLastDown.y = event.getY();
	}
	
	// �h���b�O�ʂ�Ԃ�
	public PointF touchMove(MotionEvent event){
		
		PointF diff = new PointF();
		
		// �Ŋ��Ƀ^�b�`�����ʒu����h���b�O�ʂ��v�Z
		diff.x = mLastDown.x - event.getX();
		diff.y = mLastDown.y - event.getY();
		
		// �Ŋ��Ƀ^�b�`�����ʒu���X�V 
		updateLastTouch(event);
		
		// �h���b�O�ʂ�Ԃ�
		return diff;
	}
	
	// �X���C�v����ŃJ�����̌�����ύX
	public void scrollCameraAngle(PointF diff){
		lastMagZ += diff.x / touchSensitivity;
		lastMagX += diff.y / touchSensitivity;
		
		UpdateCameraRotationByTouch();
	}
	
	// �X���C�v����ŃJ�����ʒu��ύX
	private void scrollCameraPosition(PointF diff) {
		lastRPCSX -= diff.y / touchSensitivity;
		lastRPCSY += diff.x / touchSensitivity;
		
		UpdateCameraPositionByTouch();
	}
	
	// �X���C�v����ŃJ�����̍�����ύX
	private void scrollUserHeight(PointF diff) {
		userHeight += diff.y / ( touchSensitivity * 8 );
		UpdateUserHeight(userHeight);
	}
	
	// �X���C�v����Ő��ʂ�ύX
	private void scrollWaterHeight(PointF diff) {
		waterHeight += diff.y / ( touchSensitivity * 8 );
		UpdateWaterHeight();
	}
	
	/*
	 * 
	 * �I�v�V�������j���[�̏�����(non-Javadoc)
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
	 * �I�v�V�������j���[�{�^���̐ݒ�
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
			Toast.makeText(this, "���_���Z�b�g", Toast.LENGTH_SHORT).show();
			return true ;
			
		} else if (item.getItemId() == R.id.SET_LOCAL){
			setOffsetRPCS(TKYOffsetX, TKYOffsetY);
			ForceUpdatePosition();
			Toast.makeText(this, "���n���[�h", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_SENSOR){
			FLAG_SYNC_SENSOR = !FLAG_SYNC_SENSOR;
			if(FLAG_SYNC_SENSOR)
				Toast.makeText(this, "�J�����p�𓯊�", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "�J�����p��񓯊�", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_GPS){
			FLAG_SYNC_GPS = !FLAG_SYNC_GPS;
			if(FLAG_SYNC_GPS)
				Toast.makeText(this, "GPS�𓯊�", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "GPS��񓯊�", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_SYNC_GROUND){
			FLAG_SYNC_GROUND = !FLAG_SYNC_GROUND;
			setGrounding();
			if(FLAG_SYNC_GROUND)
				Toast.makeText(this, "�ڒn���[�h�I��", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "�ڒn���[�h�I�t", Toast.LENGTH_SHORT).show();
			return true;
			
		} else if (item.getItemId() == R.id.MENU_SELECT_PREFERENCE){
			return true;
		} else if (item.getItemId() == R.id.NEXT_BUILDING_MATERIAL){
			UpdateBuildingMaterial();
			Toast.makeText(this, "�����̃}�e���A����؂�ւ�", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.NEXT_WATER_MATERIAL){
			UpdateWaterMaterial();
			Toast.makeText(this, "���ʂ̃}�e���A����؂�ւ�", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.NEXT_GROUND_MATERIAL){
			UpdateGroundMaterial();
			Toast.makeText(this, "�n�ʂ̃}�e���A����؂�ւ�", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.GALAXY_FIX){
			ToggleGalaxyFix();
			Toast.makeText(this, "GALAXY FIXED", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.CAPTURE_SCREEN){
			CaptureScreen();
			Toast.makeText(this, "�X�N���[���V���b�g���B�e���܂���", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.MINIMAP_TOGGLE){
			Toast.makeText(this, "�~�j�}�b�v�̐؂�ւ�", Toast.LENGTH_SHORT).show();
			ToggleMinimapExpand();
			return true;
		} else if (item.getItemId() == R.id.PIN_CURRENT_LOC){
			PinCurrentLocation();
			Toast.makeText(this, "���ݒn�Ƀs����ǉ����܂���", Toast.LENGTH_SHORT).show();
			return true;
		} else if (item.getItemId() == R.id.CLEAR_PIN){
			ClearAllPin();
			Toast.makeText(this, "���ׂẴs�����폜���܂���", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	

	/*
	 * �L�[�A�b�v�C�L�[�_�E���n���h��
	 * UnityPlayerActivity��True��Ԃ��ƃ��j���[��
	 * �\������Ȃ��̂ŃI�[�o�[���C�h����K�v������D
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
	 * �z�[���{�^���������ꂽ����A���̃A�v�����N���������ɌĂ΂��
	 * TODO:API13�ɕK�v���H
	 */
	 @Override
	 public void onUserLeaveHint(){
	        finish();
	 }

	// �{�^���n���h��
	@Override
	public void onClick(View v) {
		
		if(v.getId()==R.id.flip_next_btn){
			flipper.showNext();
		}else if(v.getId()==R.id.flip_previous_btn){
			flipper.showPrevious();
		}
		
		// ���݂̃��[�h���i�[
		currentMode = flipper.indexOfChild(flipper.getCurrentView());
		
		// DEBUG
		if(DEBUG){
			Log.d(TAG, ""+flipper.getCurrentView().getId());
			Log.d(TAG, "index"+flipper.indexOfChild(flipper.getCurrentView()));
		}
	}

	/****************************************************
	 * Unity�ɑ��M����֐� 
	 ****************************************************/
	
	
	/*
	 * �J�����̃A���O���̍X�V
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
	 * �J�����̃A���O���̎蓮�X�V
	 * 
	 */
	public void UpdateCameraRotationByTouch() {
		// Log.d(TAG, ""+lastMagX+", "+lastMagY+", "+lastMagZ);
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleX", String.valueOf(lastMagX));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleY", String.valueOf(lastMagY));
		UnityPlayer.UnitySendMessage("Main Camera", "setAngleZ", String.valueOf(lastMagZ));
	}
	
	/*
	 * �ʒu�𑦎����f
	 * �X���[�X���g��Ȃ�
	 */
	public void ForceUpdatePosition() {
		UpdateCameraPositionByGPS();
		UnityPlayer.UnitySendMessage("Main Camera", "forceUpdateLocation", "");
	}
	
	/*
	 * �J�����̍���
	 * Unity���M
	 */
	public void UpdateUserHeight(double height) {
		UnityPlayer.UnitySendMessage("Main Camera", "setUserHeight", String.valueOf(height));
	}
	
	/*
	 * �n�ʂɗ����ǂ���
	 * Unity���M
	 */
	public void setGrounding() {
		UnityPlayer.UnitySendMessage("Main Camera", "setGrounding", String.valueOf(FLAG_SYNC_GROUND));
	}
	
	/*
	 * �����̃}�e���A���̃T�C�N��������
	 * 
	 */
	public void UpdateBuildingMaterial(){
		currentBuildingMaterial += 1;
		if(currentBuildingMaterial > 3)	currentBuildingMaterial = 0;
		UnityPlayer.UnitySendMessage("Buildings", "setMaterialMode", String.valueOf(currentBuildingMaterial));
	}
	
	/*
	 * ���ʃ}�e���A��
	 * �T�C�N��������
	 * 
	 */
	public void UpdateWaterMaterial(){
		currentWaterMaterial += 1;
		if(currentWaterMaterial > 3)	currentWaterMaterial = 0;
		UnityPlayer.UnitySendMessage("Water Surface", "setMaterialMode", String.valueOf(currentWaterMaterial));
	}
	
	/*
	 * �n�`�}�e���A��
	 * �T�C�N��������
	 * 
	 */
	public void UpdateGroundMaterial(){
		currentGroundMaterial += 1;
		if(currentGroundMaterial > 3)	currentGroundMaterial = 0;
		UnityPlayer.UnitySendMessage("Ground Surface", "setMaterialMode", String.valueOf(currentGroundMaterial));
	}
	
	/*
	 * ���ʂ̍����𑗐M
	 * 
	 */
	public void UpdateWaterHeight(){
		UnityPlayer.UnitySendMessage("Water Surface", "setHeight", String.valueOf(waterHeight));
	}
	
	//�@������
	public void UpdateWaveAnimationState(){
		//UnityPlayer.UnitySendMessage("", "setWavingAnimation", String.valueOf(FLAG_WAVE_ANIMATION));
	}
	
	// Galaxy�o�O�Ή�
	// Unity���M
	public void ToggleGalaxyFix(){
		FLAG_GALAXY_FIX = !FLAG_GALAXY_FIX ;
		UnityPlayer.UnitySendMessage("Main Camera", "setGalaxyFix", String.valueOf(FLAG_GALAXY_FIX));
	}
	
	// �X�N���[���V���b�g�̎B�e
	// Unity���M
	public void CaptureScreen(){
		UnityPlayer.UnitySendMessage("Main Camera", "captureScreen", "");
	}
	
	// �~�j�}�b�v���ő剻����D
	// �g�O������
	public void ToggleMinimapExpand(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidToggleMap", "");
	}
	
	// �~�j�}�b�v�̌��ݒn�Ƀs�����h��
	public void PinCurrentLocation(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidPinCurrentLocation", "");
	}
	
	// �~�j�}�b�v�̃s�����N���A
	public void ClearAllPin(){
		UnityPlayer.UnitySendMessage("Game Controller", "AndroidClearPin", "");
	}
	
	/*
	 * Unity�N����ɌĂтɗ���B
	 * ���m�ɂ�Game Controller��Start()�����Ƃ��B
	 */
	public void UnityReadyCallback(){
		UNITY_READY = true;
	}
	
	// touch �ɔz�u
	// �H
	// �H
	public void Test(){
		UpdateWaterHeight();
		//Log.d(TAG, "floodHeight: "+floodHeight);
	}
}