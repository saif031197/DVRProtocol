import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class RtStart {

	public static void main(String[] args) throws SocketException, ClassNotFoundException {
		//We are using Ipv4 addresses for multicast
		System.setProperty("java.net.preferIPv4Stack", "true");

		if(args.length < 2) {
	      System.out.println("Type :-JAVA Mainclass(RtStart) portnumber filepath");
	      System.exit(0);
	    }
		
		ObjectInputStream objectinstrm;
		ByteArrayInputStream byteinstrm;
		//Used for sending and receiving multi cast IP packets.It is an extension of the Datagram socket class.
		InetAddress multicastAddr;
		DatagramSocket osock = new DatagramSocket();
		byte[] byteBuffer = new byte[10240];
		MulticastSocket msock = null;
		//Used for sending and receiving broadcast messages.
		DatagramPacket dgramPacket = new DatagramPacket(byteBuffer, byteBuffer.length);;
		
		

		try {
			//So that multiple hosts can attach themselves
			msock = new MulticastSocket(Integer.parseInt(args[0]));
			multicastAddr = InetAddress.getByName("224.0.0.251");
			msock.joinGroup( multicastAddr);
			 //Multicast address to which each host sends routing info.So that they can be distributed to all host's connected to
			//that socket.
			//New routing object is created and sent.
			RTable rObj = new RTable();
			//This is the method that gets called to create an initial routing table out of the text data.
			rObj.firstRouteTable(args[1]);
			//After table construction the process of sending out starts.
			//We are calling a thread.start method so that routing info can be continuously multicasted.
			new RtSend(osock, rObj, Integer.parseInt(args[0])).start();
			//When we call a start method a thread gets created and its run method implementation is execution. 
			while(true) {
				msock.receive(dgramPacket);
				byte[] data = dgramPacket.getData();
				byteinstrm = new ByteArrayInputStream(data);
				objectinstrm = new ObjectInputStream(byteinstrm);
				//Reading of the router table for updataion
				RTable objrcv = (RTable) objectinstrm.readObject();
				//Our orgin router also exists in the neighbour lists so it mustbe removed
				ArrayList<String> nbour = new ArrayList<String>(rObj.getImmediateNeighbors());
				//Our neoigbour list has all immediate neighbors
				nbour.remove(rObj.getRouterName());
				//Only if packet is from immediate neighbours packets should be accepted.
				if(nbour.contains(objrcv.getRouterName())) {
					rObj.updRtrTbl(rObj, objrcv);
				}
			}
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
}
