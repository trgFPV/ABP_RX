package abp_rx.nw.cs.hm.edu;

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
		
		byte[] head = input.getData();
		int ack = head[0];
		int sequence = payload.byteToInt(Arrays.copyOfRange(head, 1, 5));
		long checksum = payload.byteToLong(Arrays.copyOfRange(head, 5, 13));
		byte[] content = Arrays.copyOfRange(head, 13, inData.length - 1);
		if (generateChecksum(content) == checksum) {
			System.out.println("test ok");
		}
		System.out.println("Ack: "+ack+" Sequence :"+sequence+" checksum:"+ checksum);
		System.out.println(generateChecksum(content));
		return ack;
	}

	private long generateChecksum(byte[] field) {
		CRC32 crc32 = new CRC32();
		crc32.update(field);
		return crc32.getValue();
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
