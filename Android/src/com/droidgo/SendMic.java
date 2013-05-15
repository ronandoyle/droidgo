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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



//Need to look into reducing lag in audio. It has something to do with the buffer sizes.


public class SendMic extends Activity {
	
	public byte[] buffer;
	public static DatagramSocket socket;
	private int port=50005;
	AudioRecord recorder;

	private int sampleRate = 44100;	//Working.
//	private int sampleRate = 88200;
	private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;    
	private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;       
	int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
	private boolean status = true;




	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    minBufSize += 2048;
//		System.out.println("minBufSize: " + minBufSize);
	}


	
	public void start()
	{
		status = true;
		startStreaming();
		 Log.d("DROIDGO","Streaming started");
	}
	
	public void stop()
	{
		status = false;
		recorder.release();
		Log.d("DROIDGO","Recorder released");
	}

	public void startStreaming() {


	    Thread streamThread = new Thread(new Runnable() {

	        @Override
	        public void run() {
	            try {
	            	
	                DatagramSocket socket = new DatagramSocket();
	                Log.d("DROIDGO", "Socket Created");

	                byte[] buffer = new byte[minBufSize];

	                Log.d("DROIDGO","Buffer created of size " + minBufSize);
	                DatagramPacket packet;

	                // Pulling IP address of server from the settings menu in the app. This allows users to modify settings if the IP address of
	                // destination changes.
	                final InetAddress destination = InetAddress.getByName(DROIDGO.iSettings.getString(Preferences.IP_ADDRESS, ""));
	                Log.d("DROIDGO", "Address retrieved");

	                // Multiplying the buffer value by 35 increases sound quality. Optimal range is between 30 - 40. Any higher and lower and the quality drops.
	                // THe principle should be the bigger the buffer the better the quality. Going above 40 makes the buffer too big.
	                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*35);
	                Log.d("DROIDGO", "Recorder initialized");

	                recorder.startRecording();


	                while(status == true) {


	                    //reading data from MIC into buffer
	                    minBufSize = recorder.read(buffer, 0, buffer.length);

	                    //putting buffer in the packet
	                    packet = new DatagramPacket (buffer,buffer.length,destination,port);

	                    socket.send(packet);
//	                    System.out.println("MinBufferSize: " +minBufSize);

	                }
	                
	            } catch(UnknownHostException e) {
	                Log.e("DROIDGO", "UnknownHostException");
	            } catch (IOException e) {
	            	e.printStackTrace();
	                Log.e("DROIDGO", "IOException");
	            } 

	        }

	    });
	    streamThread.start();
	 }
}

