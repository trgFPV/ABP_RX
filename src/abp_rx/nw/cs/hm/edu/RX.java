package abp_rx.nw.cs.hm.edu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class RX {

	private static final int INPORT = 8087;
	DatagramSocket Inputsocket;
	Payload payload;
	private byte[] inData = new byte[1400];

	public RX(Payload pay) {
		this.payload = pay;
		try {
			Inputsocket = new DatagramSocket(INPORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public int waitForPacket() {

		System.out.println("wait");

		DatagramPacket input = null;
		try {
			input = new DatagramPacket(inData, inData.length,InetAddress.getByName("192.168.178.137"),8086);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Inputsocket.receive(input);
			System.out.println("package received");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] head = Arrays.copyOfRange(input.getData(), 0,13);
		int ack = head[0];
		int sequence = payload.byteToInt(Arrays.copyOfRange(head, 1, 5));
		int checksum = payload.byteToInt(Arrays.copyOfRange(head, 5, 9));
		int conlength = payload.byteToInt(Arrays.copyOfRange(head, 9, 13)) + 13;
		System.out.println(conlength);
		byte[] content = Arrays.copyOfRange(input.getData(), 13, conlength);
		if (generateChecksum(content) == checksum) {
			System.out.println("test ok");
		}
		System.out.println("Ack: "+ack+" Sequence :"+sequence+" checksum:"+ checksum);
		System.out.println(generateChecksum(content));
		return ack;
	}

	private int generateChecksum(byte[] field) {
		int checksum = 0;
		for(byte b : field) {
			checksum += b;
		}
		
		return checksum;
	}

	public void connectionFailed() {
		//get ack 0 first.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try {
			output.write(payload.storeIntInToByte(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Inputsocket.send(new DatagramPacket(
			output.toByteArray(),output.toByteArray().length,InetAddress.getByName("192.168.178.137"),8086));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	// public DatagramPacket preparePacket(int index) {
	// // package data = payload + 4 bytes sequence + sequenceNrSize +
	// checkSumSize
	//
	// // copy the dataFrame from the payload
	// byte[] dataFrame = payload.getCompleteDataArray();
	// dataFrame = Arrays.copyOfRange(dataFrame, index, index + dataPkgSize);
	//
	// return new DatagramPacket(dataFrame, completePkgSize, RX_IP, PORT);
	// }

}
