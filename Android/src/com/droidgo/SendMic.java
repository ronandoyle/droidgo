package com.droidgo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
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
	private EditText target;
	private TextView streamingLabel;
	private Button startButton,stopButton;

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

	                final InetAddress destination = InetAddress.getByName("10.20.102.100");
	                Log.d("DROIDGO", "Address retrieved");


	                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
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

