package com.droidgo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.droidgo.settings.Preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class is used to stream audio from the Android device to a Java Server on the Fit-PC3.
 * 
 * @author Ronan Doyle
 * 
 */
public class SendMic extends AsyncTask<Void, Integer, Void>{
	
	public byte[] buffer;
	public static DatagramSocket socket;
	private int port=50005;
	AudioRecord recorder;

	private int sampleRate = 44100;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;    
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
	private boolean status = true;

	/**
	 * Default constructor for the class. Sets the buffer size to 9728. This is the same size as the buffer
	 * on the Java server. For optimal quality keep this value at 9728 and ensure the Server buffer size matches.
	 */
	public SendMic(){
		
		Log.i("BUFFERSIZE", "" + minBufSize);
		
		minBufSize += 2048;
	}

	/**
	 * Starting the streaming to the Java server.
	 */
	public void start()
	{
		status = true;
		doInBackground();
		 Log.d("DROIDGO","Streaming started");
	}
	
	/**
	 * Stopping the streaming to the Java server.
	 */
	public void stop()
	{
		status = false;
		recorder.release();
		Log.d("DROIDGO","Recorder released");
	}

	/**
	 * Performing streaming of audio content on a background thread/process. This is done to take
	 * pressure and stress away from the main UI, allowing the app to perform more efficiently.
	 * This class will take in live audio from the Android devices microphone and stream this raw 
	 * audio via UDP packets to the Java server. The Java server will then take this raw audio
	 * and push it to the speakers connected to the Fit-PC3. Using the buffer size laid out above,
	 * the sound quality is near perfect, with a slight delay of 1-1.5 seconds. This thread is set
	 * to a high priority to increase performance.
	 */
	@Override
	protected Void doInBackground(Void... params) {
				
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
	            try {
	            	
	                DatagramSocket socket = new DatagramSocket();

	                byte[] buffer = new byte[minBufSize];

	                DatagramPacket packet;

	                // Pulling IP address of server from the settings menu in the app. This allows users to modify settings if the IP address of
	                // destination changes.
	                final InetAddress destination = InetAddress.getByName(DROIDGO.iSettings.getString(Preferences.IP_ADDRESS, ""));

	                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);

	                recorder.startRecording();
	                
	              // Putting buffer in the packet
                    packet = new DatagramPacket (buffer,buffer.length,destination,port);

	                while(status == true) {

	                    // Reading data from MIC into buffer
	                    minBufSize = recorder.read(buffer, 0, buffer.length);

	                    socket.send(packet);
	                    
	                    minBufSize = 0;

	                }
	                
	            } catch(UnknownHostException e) {
	                Log.e("DROIDGO", "UnknownHostException");
	                e.printStackTrace();
	            } catch (IOException e) {
	                Log.e("DROIDGO", "IOException");
	                e.printStackTrace();
	            } 
		return null;
	}

}

