package com.droidgo;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import com.droidgo.httpservice.LocalHttpService;
import com.droidgo.settings.Preferences;
import com.droidgo.DriveClass;

/**
 * The main activity for the project. Displays the apps UI.
 * 
 * @author Ronan Doyle
 * 
 */
public class DROIDGO extends Activity implements OnTouchListener,
		PreviewCallback, Callback {

	private LinearLayout controlsLayout; // Section to contain the joystick.
	private TextView connectionStatus; // Displays connection status (Connected or Disconnected).
	private Joystick joystick; // Joystick object.
	private WebView webV;
	private Switch connSwitch;
	int cameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
	Camera camera = Camera.open(cameraIndex);
	private SurfaceHolder previewHolder;
	private LocalHttpService mBoundService;
	private boolean mIsBound;
	
	private AudioManager audioManager = null;

	public static SharedPreferences iSettings = null;
	DriveClass driveClass;

	//ACTIVE used when user is touching app, IDLE for when there is no touch.
	private enum StateMachine {
		ACTIVE, IDLE
	};
	

	private StateMachine state;
	
	SendMic sendMic;

	/**
	 * Instantiating all of the necessary variables.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

			// This is used to determine whether or not the On/Off switch for the camera and audio
			// streaming from the app has been clicked.
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (isChecked) {
					//Start camera
					doBindService();
					//Start audio
					Thread streamAudio = new Thread(new Runnable(){

						@Override
						public void run() {
							sendMic.start();
						}
						
					});
					streamAudio.start();
					
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
		
		state = StateMachine.IDLE;
		driveClass = new DriveClass();

		SetupCamera();
		SetupCameraFeed();
	}
	
	/**
	 *  Setting up the camera feed from the robot. The IP address of the feed is taken from the settings screen.
	 */
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
	}

	/**
	 *  Setting up the Android camera. This will prepare the cameras content so that it can be displayed on a section 
	 *  of the screen so that it can be streamed from the NanaHTTPD server. The section of the screen it will 
	 *  be displayed on will be invisible to the user.
	 */
	private void SetupCamera() {

		final SurfaceView preview = (SurfaceView) findViewById(R.id.cameraView);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setKeepScreenOn(true);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Needed for Android devices pre 3.0.

		camera.setPreviewCallback(this);
		
	}
	
	// I had encountered a problem in the app, when the images streamed from the Android devices camera were being displayed
	// in the browser on the FitPC3, they were being shown correctly, but rotated at 90 degrees to the right. I created the
	// code below to change the orientation of the camera, in the hope that they would be rotated in the browser. This code
	// did not seem to fix the problem, despite it working in other tests. I managed to solve this problem by rotating the 
	// canvas element in the HTML page by 270 degrees. This allowed the images to appear correctly.
	//
	// I have left in the code below in the hope that it may work for some devices.
	
//	public static void setCameraDisplayOrientation(Activity activity,
//			int cameraId, android.hardware.Camera camera) {
//		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//		android.hardware.Camera.getCameraInfo(cameraId, info);
//		
//		int rotation = activity.getWindowManager().getDefaultDisplay()
//				.getRotation();
//		int degrees = 0;
//		switch (rotation) {
//		case Surface.ROTATION_0:
//			degrees = 0;
//			break;
//		case Surface.ROTATION_90:
//			degrees = 90;
//			break;
//		case Surface.ROTATION_180:
//			degrees = 180;
//			break;
//		case Surface.ROTATION_270:
//			degrees = 270;
//			break;
//		}
//
//		int result;
//		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//			result = (info.orientation + degrees) % 360;
//			result = (360 - result) % 360; // compensate the mirror
//		} else { // back-facing
//			result = (info.orientation - degrees + 360) % 360;
//		}
//		camera.setDisplayOrientation(result);
//	}

	/**
	 *  Creating the surface to be used for the Android cameras content
	 * @param width
	 * @param height
	 */
	private void initPreview(int width, int height) {
		try {
			camera.setPreviewDisplay(previewHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *  Displaying the content from the Android camera on the phone. This content will not be visible to users
	 *  of the app as the size of the preview screen is set to 1dp.
	 */
	private void startPreview() {
		Parameters params = camera.getParameters();

		/**
		 * This is for HTC One S (Smartphone's).
		 */
		// params.setPreviewFpsRange(1, 150);

		/**
		 * For larger tablet screens use the values
		 */
		params.setPreviewFpsRange(5, 31); // setPreviewFrameRate() depreciated for setPreviewFpsRange();

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
	
	/**
	 *  This method is called when a touch event occurs on the joystick. Based on the type of event
	 *  that occurs, an action is carried out. The joystick state is considered to be active while this method
	 *  is being called. If the user removes their finger from the joystick then the state will be set to idle.
	 * @param event
	 */
	private void activeEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;

		case MotionEvent.ACTION_UP:
			// Stopping motors.
			state = StateMachine.IDLE;
			
			break;

		case MotionEvent.ACTION_MOVE:
			// Calling drive commands based on x and y values.
			driveClass.driveRegion((int) event.getX(),(int) event.getY());

			break;

		default:
			break;

		}
	}

	/**
	 *  This method is called when the user is not touching the joystick. When the user touches the joystick
	 *  again, the state will be set to active and the activeEvent method will be used instead.
	 * @param event
	 */
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
			break;

		}
	}

	/**
	 *  This method is used to determine whether the joystick is in an active or idle state.
	 */
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
			}
		}
		return result;
	}

	
	/**
	 * Creating the options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	/**
	 *  When the settings option is selected from the options menu, the preferences screen will be opened up.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			goToPrefs();
		}
		return true;
	}
	
	/**
	 *  Creates the preferences screen, from which the user can enter the IP address/ports of the Robot
	 */
	private void goToPrefs() {
		startActivity(new Intent(DROIDGO.this, Preferences.class));
		finish();
	}

	/**
	 *  Used to create preview frames of the Android devices camera content. These frames are then converted to
	 *  JPGs before being sent to the NanoHTTPD server on the Android device.
	 */
	public void onPreviewFrame(final byte[] data, final Camera camera) {
		
		Thread yuvThread = new Thread (new Runnable() {
			public void run(){
				Camera.Parameters params = camera.getParameters();
				Size size = params.getPreviewSize();
				final YuvImage image = new YuvImage(data, params.getPreviewFormat(),
						size.width, size.height, null);
				mBoundService.setImage(image);
			}
		});
		yuvThread.start();
	}

	/**
	 *  This is used in the creation of the connection to the local server.
	 */
	private ServiceConnection conn = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((LocalHttpService.LocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};

	/**
	 *  Starting the stream of the content to the NanoHTTPD server.
	 */
	public void doBindService() {
		final Intent mServiceIntent = new Intent(this, LocalHttpService.class);
		
		Thread bindService = new Thread(new Runnable (){
			public void run(){
				if(camera == null)
				{
					camera = Camera.open(cameraIndex);
				}
				
				
				bindService(mServiceIntent, conn, Context.BIND_AUTO_CREATE);
				mIsBound = true;
			}
		});
		bindService.start();
		
	}

	/**
	 *  Stopping the stream of the content to the NanoHTTPD server.
	 */
	public void doUnbindService() {
		Thread unbindService = new Thread(new Runnable() {
			public void run(){
				if (mIsBound) {
					unbindService(conn);

					camera.release();
					mIsBound = false;
				}
			}
		});
		unbindService.start();
		
	}

	/**
	 *  The Android devices default destroy method. This has been modified to relenquish the apps hold
	 *  on the devices camera and stop the NanoHTTPD server.
	 */
	@Override
	protected void onDestroy() {
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