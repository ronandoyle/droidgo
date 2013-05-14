package com.droidgo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.droidgo.comm.CarDroidConnection;
import com.droidgo.httpservice.LocalHttpService;
import com.droidgo.settings.Preferences;

/**
 * The main activity for the project.
 * 
 * @author Ronan Doyle
 * 
 */

public class MainActivity extends Activity implements OnTouchListener,
		PreviewCallback, Callback {

	private LinearLayout controlsLayout; // Section to contain the joystick.
	private TextView connectionStatus; // Displays connection status (Connected or Disconnected).
	private int latchX, latchY = 0; // Used for refining the joystick control mechanism.
	private Joystick joystick; // Joystick object.
	private CarDroidConnection carDroidCon; // Handles the Bluetooth connection between the App and NXT. So far not being used for the WiFi version (DO NOT REMOVE!).
	private WebView webV;
	private Switch connSwitch;
	int cameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
	final Camera camera = Camera.open(cameraIndex);
	private SurfaceHolder previewHolder;
	private LocalHttpService mBoundService;
	private boolean mIsBound;
	private String inArea;
	private AudioManager audioManager = null;

	public static SharedPreferences iSettings = null;

	//ACTIVE used when user is touching app, IDLE for when no touch exists
	private enum StateMachine {
		ACTIVE, IDLE
	};
	private enum DriveCommands{
		FORWARD, FORWARDx2, FORWARDx3, FORWARDx4, FORWARDx5, FORWARD_LEFT, FORWARD_RIGHT, BACK, BACKx2, CENTER, LEFT, RIGHT
	};

	private StateMachine state;
	private DriveCommands driveCommands;
	SendMic sendMic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		carDroidCon = new CarDroidConnection(this);
		sendMic = new SendMic();

		controlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
		controlsLayout.setClickable(true);
		joystick = new Joystick(this);
		controlsLayout.addView(joystick);

		joystick.setOnTouchListener(this);

		connectionStatus = (TextView) findViewById(R.id.connection_status);
		connectionStatus.setText("Disconnected...");
		connectionStatus.setTextColor(Color.RED);

		connSwitch = (Switch) findViewById(R.id.switch1);
		connSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {
					//Start camera
					doBindService();
					//Start audio
					sendMic.start();
					connectionStatus.setText("Connected...");
					connectionStatus.setTextColor(Color.GREEN);

				} else {
					//Stop camera
					doUnbindService();
					//Stop audio
					sendMic.stop();
					connectionStatus.setText("Disconnected...");
					connectionStatus.setTextColor(Color.RED);
				}
			}

		});

		audioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

		SetupCamera();

		SetupCameraFeed();
		
		inArea = "";

	}

	private void SetupCameraFeed() {
		iSettings = PreferenceManager.getDefaultSharedPreferences(this);

		webV = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webV.getSettings();

		webSettings.setJavaScriptEnabled(true);

		webV.setWebViewClient(new WebViewClient());
		webV.loadUrl("http://"
				+ iSettings.getString(Preferences.IP_ADDRESS, "") + ":"
				+ iSettings.getString(Preferences.PORT, "8080")
				+ "/javascript_simple.html");
//		webV.loadUrl("http://"
//				+ iSettings.getString(Preferences.VIDEO_STREAM, "bambuser.com/v/3523717"));
	}

	private void SetupCamera() {
		SharedPreferences settings = getSharedPreferences("CAMPREFS", 0);
		LocalHttpService.setPwd(settings.getString("pwd", ""));
		String password = "droidgo";
		LocalHttpService.setPwd(password);
		Editor editor = settings.edit();
		editor.putString("password", password);
		editor.commit();

		final SurfaceView preview = (SurfaceView) findViewById(R.id.cameraView);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setKeepScreenOn(true);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Needed for Android devices pre 3.0.

		setCameraDisplayOrientation(this, cameraIndex, camera);
		camera.setPreviewCallback(this);
		state = StateMachine.IDLE;
		driveCommands = DriveCommands.CENTER;
	}

	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		
		// FIXME This code does not work. Need to investigate camera image rotation capabilities further.
		/*******************************/
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
	/*******************************/

	private void initPreview(int width, int height) {
		try {
			camera.setPreviewDisplay(previewHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void startPreview() {
		Parameters params = camera.getParameters();

		/**
		 * This is for HTC One S (Smartphone's).
		 */
		// params.setPreviewFpsRange(1, 150);

		/**
		 * This is for HTC Flyer (Tablet's)
		 */
		params.setPreviewFpsRange(5000, 31000); // setPreviewFrameRate() depreciated for setPreviewFpsRange();

		List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
//		System.out.println("PREVIEW SIZES: " + previewSizes.toString());

		List<int[]> previewFPS = params.getSupportedPreviewFpsRange();
//		System.out.println("PREVIEW FPS: " + previewFPS.toString());

		camera.setParameters(params);
		camera.startPreview();
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}

		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			initPreview(width, height);
			startPreview();

		}
	};
	
	private void activeEvent(MotionEvent event) {
//		System.out.println("LATCHX: " + latchX);
//		System.out.println("LATCHY: " + latchY);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_UP:
			// Stopping motors.
			state = StateMachine.IDLE;
//			carDroidCon.issueCommand(CarDroidConnection.MOTOR_A_C_STOP, 0); Used for Bluetooth.
//			carDroidCon.issueCommand(CarDroidConnection.MOTOR_B_CENTER, 0);
			latchX = 0;
			latchY = 0;
//			System.out.println("LATCH X: " + latchX);
//			System.out.println("LATCH Y: " + latchY);
			break;

		case MotionEvent.ACTION_MOVE:

			// forward and reverse
			forwardReverse((int) event.getX(),(int) event.getY());

			// left and right
//			leftRight((int) event.getX(),(int) event.getY());
			break;

		default:
			break;

		}
		// System.out.println("Current State: ACTIVE, Event: " + event
		// + ", New State: " + state);
	}

	private void idleEvent(MotionEvent event) {
		// The app waits for the state to change to active before sending data to the server.
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			state = StateMachine.ACTIVE;
			break;

		case MotionEvent.ACTION_UP:
			break;

		case MotionEvent.ACTION_MOVE:
			break;

		default:
			// System.out.println("ERROR: Incorrect Event " + event);
			break;

		}
		// System.out.println("Current State: ACTIVE, Event: " + event
		// + ", New State: " + state);
	}

	public boolean onTouch(View v, MotionEvent event) {
		boolean result = false;
		if (v == joystick) {
			switch (state) {
			case IDLE:
				idleEvent(event);
				break;

			case ACTIVE:

				activeEvent(event);
				break;

			default:
				// System.out.println("Error: Incorrect State " + state);
			}
		}
		return result;
	}

	private void forwardReverse(int x, int y) {

		/**
		 * The following x,y values are hardcoded for a HTC One S screen
		 * dimensions. Tests have not been properly carried out on other
		 * devices.
		 * 
		 * Will more than likely need to change these values to be dynamic,
		 * based on the devices screen size.
		 */
		
		if (y >= 139 && y < 159 && (x >= 250 && x <= 290)) 
		{
			driveCommands = DriveCommands.FORWARD;
		} 
		else if (y >= 119 && y < 139 && (x >= 250 && x <= 290)) 
		{
			driveCommands = DriveCommands.FORWARDx2;
		}
		else if (y >= 99 && y < 119 && (x >= 250 && x <= 290))
		{
			driveCommands = DriveCommands.FORWARDx3;
		}
		else if (y >= 79 && y < 99 && (x >= 250 && x <= 290))
		{
			driveCommands = DriveCommands.FORWARDx4;
		}
		else if (y < 79 && (x >= 250 && x <= 290))
		{
			driveCommands = DriveCommands.FORWARDx5;
		}
		
		else if (x < 250 && (y >= 159 && y <= 199)) {
			driveCommands = DriveCommands.LEFT;
		}
		// right
		//FLYER 325
		else if (x > 290 && (y >= 159 && y <= 199)) {
			driveCommands = DriveCommands.RIGHT;
		}
		
		// diagonal
		else if (y < 159 && x < 250)
		{
			driveCommands = DriveCommands.FORWARD_LEFT;
		}
		else if (y < 159 && x > 250)
		{
			driveCommands = DriveCommands.FORWARD_RIGHT;
		}
		// reverse
		// FLYER 267
		else if (y > 199 && y < 280) 
		{
			driveCommands = DriveCommands.BACK;
		} 
		else if (y > 280) 
		{
			driveCommands = DriveCommands.BACKx2;
		}
		// center
		// FLYER 187 267
		else if (y >= 159 && y <= 199) 
		{
			driveCommands = DriveCommands.CENTER;
		}
		

		driveSwitch();
	}
	
	private void driveSwitch() {
		switch (driveCommands) {
		case FORWARD:
			if (inArea != "FORWARD") {
				new SendToServer("FORWARD").execute();
				System.out.println("FORWARD");
				inArea = "FORWARD";
			}
			break;
		case FORWARDx2:
			if (inArea != "FORWARDx2") {
				new SendToServer("FORWARDx2").execute();
				System.out.println("FORWARDx2");
				inArea = "FORWARDx2";
			}
			break;
		case FORWARDx3:
			if (inArea != "FORWARDx3") {
				new SendToServer("FORWARDx3").execute();
				System.out.println("FORWARDx3");
				inArea = "FORWARDx3";
			}
			break;
		case FORWARDx4:
			if (inArea != "FORWARDx4") {
				new SendToServer("FORWARDx4").execute();
				System.out.println("FORWARDx4");
				inArea = "FORWARDx4";
			}
			break;
		case FORWARDx5:
			if (inArea != "FORWARDx5") {
				new SendToServer("FORWARDx5").execute();
				System.out.println("FORWARDx5");
				inArea = "FORWARDx5";
			}
			break;
		case FORWARD_LEFT:
			if (inArea != "FORWARD_LEFT") {
				new SendToServer("FORWARD_LEFT").execute();
				System.out.println("FORWARD_LEFT");
				inArea = "FORWARD_LEFT";
			}
			break;
		case FORWARD_RIGHT:
			if (inArea != "FORWARD_RIGHT") {
				new SendToServer("FORWARD_RIGHT").execute();
				System.out.println("FORWARD_RIGHT");
				inArea = "FORWARD_RIGHT";
			}
			break;
		case BACK:
			if (inArea != "BACK") {
				new SendToServer("BACK").execute();
				System.out.println("BACK");
				inArea = "BACK";
			}
			break;
		case BACKx2:
			if (inArea != "BACKx2") {
				new SendToServer("BACKx2").execute();
				System.out.println("BACKx2");
				inArea = "BACKx2";
			}
			break;
		case LEFT:
			if(inArea != "LEFT"){
				 new SendToServer("LEFT").execute();
				 System.out.println("LEFT");
				 inArea = "LEFT";
			}
			break;
		case RIGHT:
			if(inArea != "RIGHT"){
				 new SendToServer("RIGHT").execute();
				 System.out.println("RIGHT");
				 inArea = "RIGHT";
			}
			break;
		case CENTER:
			if (inArea != "CENTER") {
				new SendToServer("CENTER").execute();
				System.out.println("CENTER");
				inArea = "CENTER";
			}
			break;
		default:
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			goToPrefs();
		}
		return true;
	}
	
/*
 * TODO - Create a an option to allow Bluetooth connectivity?
 */
	// public void connect() {
	// // carDroidCon.configBluetooth();
	// // carDroidCon.establishConnection();
	// // connector = CarDroidConnection.connect(CONN_TYPE.LEGO_LCP);
	// // connector.connectTo("00:16:53:1A:49:A0");
	// if (carDroidCon.socket == null) {
	// carDroidCon.configBluetooth();
	// } else {
	// carDroidCon.closeBluetoothConnection();
	// }
	// }
	//
	// public void disconnect() {
	// carDroidCon.closeBluetoothConnection();
	// }
	

	/*
	 * TODO Extract this class into its own class.
	 */
	

	private void goToPrefs() {
		startActivity(new Intent(MainActivity.this, Preferences.class));
		finish();
	}

	public void onPreviewFrame(byte[] data, Camera camera) {
		Camera.Parameters params = camera.getParameters();
		Size size = params.getPreviewSize();
		final YuvImage image = new YuvImage(data, params.getPreviewFormat(),
				size.width, size.height, null);
		mBoundService.setImage(image);
	}

	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((LocalHttpService.LocalBinder) service)
					.getService();

		}

		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;

		}
	};

	public void doBindService() {
		Intent mServiceIntent = new Intent(this, LocalHttpService.class);
		bindService(mServiceIntent, conn, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	public void doUnbindService() {
		if (mIsBound) {
			unbindService(conn);

			camera.release();
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		camera.setPreviewCallback(null);
		camera.release();
		doUnbindService();

		super.onDestroy();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

}