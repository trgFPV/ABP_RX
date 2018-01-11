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
	private byte[] inData = new byte[1440];

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
		
		byte[] head = Arrays.copyOfRange(input.getData(), 0,20);
		int ack = head[0];
		int sequence = payload.byteToInt(Arrays.copyOfRange(head, 4, 8));
		long checksum = payload.byteToLong(Arrays.copyOfRange(head, 8, 16));
		int conlength = payload.byteToInt(Arrays.copyOfRange(head, 16, 20)) + 20;

		System.out.println("Ack: "+ack+" Sequence :"+sequence+" checksum:"+ checksum + " conlength: " + conlength);
		byte[] content = Arrays.copyOfRange(input.getData(), 20, conlength);
		System.out.println(content.length);
		if (generateChecksum(content) == checksum) {
			System.out.println("test ok");
		} else {
			
		}
		System.out.println(generateChecksum(content));
		return ack;
	}

	private long generateChecksum(byte[] field) {
//		int checksum = 0;
//		for(byte b : field) {
//			checksum += b;
//		}
//		
//		return checksum;
		CRC32 crc32 = new CRC32();
		crc32.update(field);
		return crc32.getValue();
	}

	public void sendConnection(int i) {
		//get ack 0 first.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try {
			output.write(Payload.storeIntInToByte(i));
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
