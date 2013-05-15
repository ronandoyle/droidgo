import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

class Server {

	AudioInputStream audioInputStream;
	static AudioInputStream ais;
	static AudioFormat format;
	static boolean status = true;
	static int port = 50005;
	static int sampleRate = 44100;
	static DataLine.Info dataLineInfo;
	static SourceDataLine sourceDataLine;

	public static void main(String args[]) throws Exception {

		DatagramSocket serverSocket = new DatagramSocket(50005);

		/**
		 * Formula for lag = (byte_size/sample_rate)*2 Byte size 9728 will
		 * produce ~ 0.45 seconds of lag. Voice slightly broken. Byte size 1400
		 * will produce ~ 0.06 seconds of lag. Voice extremely broken. Byte size
		 * 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken
		 * then 9728.
		 */

		byte[] receiveData = new byte[9728];

		format = new AudioFormat(sampleRate, 16, 1, true, false);
		dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		initSpeaker();

		// ////////////////////////////////////////////////////////////////////////////////
		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);
		System.out.println("Datagram packet set up: " + receivePacket);
		while (status == true) {
			

			serverSocket.receive(receivePacket);
			System.out.println("Packet received.");
			
			toSpeaker(receivePacket.getData());
			System.out.println("Audio sent to speaker.");
			// Toolkit.getDefaultToolkit().beep();

		}
	}

	public static void initSpeaker() {
		try {

			sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

			sourceDataLine.open(format);

			sourceDataLine.start();
			System.out.println("Speaker ready to receive audio data");
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public static void toSpeaker(byte soundbytes[]) {

		sourceDataLine.write(soundbytes, 0, soundbytes.length);
		System.out.println(soundbytes.toString());

	}

	public static void closeSpeaker() {
		sourceDataLine.drain();
		sourceDataLine.close();
	}
}
