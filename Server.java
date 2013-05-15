import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;




import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;


class Server {

	AudioInputStream audioInputStream;
	static AudioInputStream ais;
	static AudioFormat format;
	static boolean status = true;
	static int port = 50005;
	static int sampleRate = 44100;
	static DataLine.Info dataLineInfo;

	public static void main(String args[]) throws Exception {


		DatagramSocket serverSocket = new DatagramSocket(50005);
		
		/**
		 * Formula for lag = (byte_size/sample_rate)*2
		 * Byte size 9728 will produce ~ 0.45 seconds of lag. Voice slightly broken.
		 * Byte size 1400 will produce ~ 0.06 seconds of lag. Voice extremely broken.
		 * Byte size 4000 will produce ~ 0.18 seconds of lag. Voice slightly more broken then 9728.
		 */

		byte[] receiveData = new byte[4000];

		format = new AudioFormat(sampleRate, 16, 1, true, false);
		dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		
		
		//////////////////////////////////////////////////////////////////////////////////
		
		
		
		while (status == true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			serverSocket.receive(receivePacket);

			ByteArrayInputStream baiss = new ByteArrayInputStream(
					receivePacket.getData());
			
			ais = new AudioInputStream(baiss, format, receivePacket.getLength());
//			toSpeaker(receivePacket.getData());
			Toolkit.getDefaultToolkit().beep();

		}
	}

	public static void toSpeaker(byte soundbytes[]) {
		
		try {
			
			
			SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			
			sourceDataLine.open(format);
			
//			FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
//			volumeControl.setValue(100.0f);
			
			sourceDataLine.start();
			if(!sourceDataLine.isOpen())
			{
				sourceDataLine.open(format);
			}
			

//			sourceDataLine.start();
			
//			System.out.println("format? :" + sourceDataLine.getFormat());
			
			sourceDataLine.write(soundbytes, 0, soundbytes.length);
			System.out.println(soundbytes.toString());
			sourceDataLine.drain();
//			sourceDataLine.close();
		} catch (Exception e) {
			System.out.println("Not compatible with these speaker or no speakers dectected...");
			e.printStackTrace();
		}
	}
}