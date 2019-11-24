
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RtSend extends Thread {
	private DatagramSocket osock;
	private RTable rtsent;
	private int PORT;
	DatagramPacket outDgram = null;

	public RtSend(DatagramSocket updateSocket, RTable tableBeingSent, int portNumber) {
		this.osock = updateSocket;
		this.rtsent = tableBeingSent;
		this.PORT = portNumber;
	}

	public void run() {
		int updateCount = 0;
		try {
			while(true) {
				//wait 15 seconds before sending out
				Thread.sleep(15000);
				rtsent.chckLinkcst();
				//multicasting address being used, NOTE: the groupAddress is unique for all mac machines, you might have to change the group address value to your machines multicasting address
				InetAddress address = InetAddress.getByName("224.0.0.251");
		        ByteArrayOutputStream boutStream = new ByteArrayOutputStream();
		  	    ObjectOutputStream ooutStream = new ObjectOutputStream(boutStream);
		  	    ooutStream.writeObject(rtsent);
	            byte[] buff = boutStream.toByteArray();
	            //The datagram packet that is being sent out.
	            outDgram = new DatagramPacket(buff, buff.length, address, PORT);
	            osock.send(outDgram);
		        System.out.println("Output Number "+ ++updateCount+":");
		        rtsent.displayTable(rtsent.getTable());
		        System.out.println();
		        try {
		          Thread.sleep(500);
		        } catch (InterruptedException ie) {
		        }
			}
		}
		catch (IOException ioe) {
		      System.out.println(ioe);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
