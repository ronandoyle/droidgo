package com.droidgo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.droidgo.HandleMovedListener;

//import com.droidgo.comm.CarDroidConnection;

/**
 * The joystick used to control the NXT brick.
 * 
 * @author Ronan Doyle
 * 
 */

public class Joystick extends View {
	String TAG = "DrawJoystick";
	Canvas canvas = new Canvas();

	public static int CIRCLE_RESTRICTION = 1;
	public static final int INVALID_TOUCH_ID = -1;
	private float rangeOfMotion; // The range of the movement.
	private float userX, userY; // User touch points.
	private int lastUserX, lastUserY;
	private int userTouchId = INVALID_TOUCH_ID;
	private int motionRadius; // The radius of the movement.
	private int sizeX; // Size of the view coordinates.
	private int centerX, centerY; // Center of the view coordinates.
	private int offsetX; // Coordinates used when the touch events are received
							// from a parents oirgin.
	private float handleX, handleY; // Center of handle.
	private float movementDetection; // The number of pixels moved across before
										// the listener picks up the movement.
	private float knownX, knownY; // The last known position of x and y in the
									// view coordinates.
	private int backgroundRadius; // Radius of joysticks background circle.
	private double radius;
	private int handleRadius; // Radius of handle.
	private int innerPadding; // Padding between edge of handle and background.
	private HandleMovedListener handleMovedListener;
	private Paint paint1;
	private Paint handle;
	private Paint background;
	private Paint stick;
	private boolean jumpToCenter;
	private boolean swapY;
	private float motionRestriction; // The restriction of the movement.

	public Joystick(Context context) {
		super(context);
		setupJoystickView();
		this.setId(R.id.joystick_object);
		setClickable(true);
	}

	/**
	 * Setting up the joystick view.
	 */

	private void setupJoystickView() {
		userX = 0;
		userY = 0;
		sizeX = 0;
		centerX = 0;
		centerY = 0;
		offsetX = 0;
		backgroundRadius = 0;
		handleRadius = 0;

		paint1 = new Paint(Paint.ANTI_ALIAS_FLAG); // Not actually seen, no
													// color needed.

		handle = new Paint(Paint.ANTI_ALIAS_FLAG); // Antialiasing makes the
													// paint images smoother &
													// crisper.
		handle.setColor(Color.RED);

		background = new Paint(Paint.ANTI_ALIAS_FLAG);
		background.setColor(Color.BLACK);

		stick = new Paint(Paint.ANTI_ALIAS_FLAG); // The stick of the joystick.
		stick.setColor(Color.GRAY);
		stick.setStrokeWidth(20);

		innerPadding = 10; // Possibly smaller?
		setRangeOfMotion(10); // Possibly bigger?
		setJumpToCenter(true);
		setFocusable(true);
		setSwapY(true);
	}

	public boolean isSwapY() {
		return swapY;
	}

	public void setSwapY(boolean swapY) {
		this.swapY = swapY;
	}

	public void setRangeOfMotion(float rangeOfMotion) {
		this.rangeOfMotion = rangeOfMotion;
	}

	public float getRangeOfMotion() {
		return rangeOfMotion;
	}

	public void setOnHandleMovedListener(HandleMovedListener movedListener) {
		this.handleMovedListener = movedListener;
	}

	public void setJumpToCenter(boolean jumpToCenter) {
		this.jumpToCenter = jumpToCenter;
	}

	public boolean getJumpToCenter() {
		return jumpToCenter;
	}

	/**
	 * The following methods deal with the drawing of the joystick, and how it
	 * can be moved.
	 */

	@Override
	protected void onLayout(boolean change, int left, int top, int right,
			int bottom) {
		super.onLayout(change, left, top, right, bottom);

		// Returns the smallest values of the measured width and height.
		int diameter = Math.min(getMeasuredWidth(), getMeasuredHeight());

		sizeX = diameter;

		// Center of joystick view
		centerX = this.getWidth() / 2;
		centerY = this.getHeight() / 2;

		backgroundRadius = diameter / 2 - innerPadding;
		handleRadius = (int) (backgroundRadius / 2);
		// The radius of the handles motion
		motionRadius = Math.min(centerX, centerY) - handleRadius;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		boolean result = false;
		/**
		 * Background
		 */
		canvas.drawCircle(centerX, centerY, backgroundRadius, background);

		/**
		 * Drawing handle
		 */
		handleX = userX + centerX;
		handleY = userY + centerY;
		canvas.drawLine(centerX, centerY, handleX, handleY, stick);
		canvas.drawCircle(handleX, handleY, handleRadius, handle);

		if (result) {
			if (motionRestriction == CIRCLE_RESTRICTION) {
				canvas.drawCircle(centerX, centerY, this.motionRadius, paint1);
			} else {
				canvas.drawRect(centerX - motionRadius, centerY - motionRadius,
						centerX + motionRadius, centerY + motionRadius, paint1);
			}
		}

		canvas.restore();
	}

	// This method only allows the user to click in a certain "boxed" area.
	private void resrictUserTouch() {
		float minX = Math.min(userX, motionRadius);
		userX = Math.max(minX, - motionRadius);

		float minY = Math.min(userY, motionRadius);
		userY = Math.max(minY, - motionRadius);

	}

	// This method then creates a circle area for which the user can only
	// operate inside.
	private void restrictUserTouchToCircle() {
		float differenceX = userX;
		float differenceY = userY;

		radius = Math.sqrt((differenceX * differenceX)
				+ (differenceY * differenceY));

		if (radius > motionRadius) {
			userX = (int) ((differenceX / radius) * motionRadius);
			userY = (int) ((differenceY / radius) * motionRadius);
		}
	}

	private void setUserTouchID(int touchId) {
		this.userTouchId = touchId;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
//		System.out.println("X: " + event.getX());
//		System.out.println("Y: " + event.getY());

		switch (action & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_MOVE: {
			handleMove(event);
			break;
		}

		case MotionEvent.ACTION_UP: {
			centerHandle();
			new SendToServer("CENTER").execute();
			System.out.println("CENTER");
			setUserTouchID(INVALID_TOUCH_ID);
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			int x = (int) event.getX();
			if (x >= 0 && x < sizeX) {
				setUserTouchID(event.getPointerId(0));
				return true;
			}
			break;
		}
		}
		return false;
	}

	private boolean handleMove(MotionEvent event) {
		float x = event.getX(userTouchId);
		userX = x - centerX;

		float y = event.getY(userTouchId);
		userY = y - centerY;

		notifyMove();
		resrictUserTouch();
		invalidate();

		return true;
	}

	private void notifyMove() {
		if (motionRestriction == CIRCLE_RESTRICTION) {
			restrictUserTouchToCircle();
		} else {
			resrictUserTouch();
		}

		if (handleMovedListener != null) {
			boolean x = Math.abs(userX - knownX) >= movementDetection;
			boolean y = Math.abs(userY - knownY) >= movementDetection;
			if (x || y) {
				this.knownX = userX;
				this.knownY = userY;

				handleMovedListener.OnMoved(lastUserX, lastUserY);
			}
		}
	}

	/**
	 * The centerHandle() method is used to gradually move the joystick back to
	 * its starting position after it has been released.
	 */

	private void centerHandle() {
		if (jumpToCenter) {
			final int fps = 5;
			final double x = (0 - userX) / fps;
			final double y = (0 - userY) / fps;

			for (int i = 0; i < fps; i++) {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						userX += x;
						userY += y;
						resrictUserTouch();
						invalidate();
//						new SendToServer("CENTER").execute();

					}
				}, i * 20);
			}
			// new SendToServer("CENTER").execute();
			// System.out.println("CENTER");
		}
	}
}
