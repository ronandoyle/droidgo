package com.droidgo.comm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

/**
 * The following class establishes a connection between the car and the Android
 * device.
 * 
 * @author Ronan Doyle
 * 
 */

public class CarDroidConnection {

	// These variables are used to control the motors. Taken from tutorial.
	public static final int MOTOR_A_C_STOP = 0;
	public static final int MOTOR_A_FORWARD = 1;
	public static final int MOTOR_A_BACKWARD = 2;
	public static final int MOTOR_C_FORWARD = 3;
	public static final int MOTOR_C_BACKWARD = 4;
	public static final int MOTOR_B_FORWARD = 5;
	public static final int MOTOR_B_BACKWARD = 6;
	public static final int MOTOR_B_CENTER = 7;
	public static final int DISCONNECT = 99;

	BluetoothAdapter bluetoothAdapter; // Android bluetooth adapter.
	BluetoothDevice car;
	public BluetoothSocket socket;
	DataOutputStream dOut;

	private final static String TAG = "DROIDComms";

	String carAddress = "00:16:53:1A:49:A0";

//	public static enum CONN_TYPE {
//		LEJOS_PACKET, LEGO_LCP
//	}
	
	public CarDroidConnection(){
		
	}

	public CarDroidConnection(Context context) {

	}
	
	// Possibly needed for inital device connection?
	
	// public static NXTConnector connect(final CONN_TYPE connection_type) {
	// Log.d(TAG, " about to add LEJOS listener ");
	//
	// NXTConnector conn = new NXTConnector();
	// conn.setDebug(true);
	// conn.addLogListener(new NXTCommLogListener() {
	//
	// public void logEvent(String arg0) {
	// Log.e(TAG + " NXJ log:", arg0);
	// }
	//
	// public void logEvent(Throwable arg0) {
	// Log.e(TAG + " NXJ log:", arg0.getMessage(), arg0);
	// }
	// });
	//
	// switch (connection_type) {
	// case LEGO_LCP:
	// conn.connectTo("btspp://NXT", NXTComm.LCP);
	// break;
	// case LEJOS_PACKET:
	// conn.connectTo("btspp://");
	// break;
	// }
	//
	// return conn;
	//
	// }

	/**
	 * Establishes a bluetooth connection between the Android device and the
	 * NXT.
	 */
	public void configBluetooth() {
		try {
			BluetoothAdapter adap = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> pairedDevices = adap.getBondedDevices();
			BluetoothDevice nxt = null;

			for (BluetoothDevice blueDev : pairedDevices) {
				if (blueDev.getName().equals("NXT".toUpperCase())) {
					nxt = blueDev;
					break;
				}
			}

			if (nxt == null) {
				Log.e(TAG, "The NXT cannot be found.");
				return;
			}

//			Method method = car.getClass().getMethod("createRfcommSocket",
//					new Class[] { int.class });
//			socket = (BluetoothSocket) method.invoke(car, 1);
			
			socket = nxt.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

			socket.connect();
			dOut = new DataOutputStream(socket.getOutputStream());

		} catch (Exception e) {
			Log.e(TAG, "The connection cannot be made.");
		}
	}

	public void issueCommand(int command, int commandValue) {
		if (dOut == null) {
			return; // Do nothing.
		}
		try {
			dOut.writeInt(command);
			dOut.writeInt(commandValue);
			dOut.flush();
		} catch (IOException e) {
			Log.e(TAG, "The command was not issued correctly.");
		}
	}

	public void closeBluetoothConnection() {
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (Exception e) {
			Log.e(TAG, "The connection could not be closed");
		}

	}
}
