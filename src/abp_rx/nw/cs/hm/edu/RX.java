package abp_rx.nw.cs.hm.edu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;

public class RX {

	private static final int INPORT = 8087;
	DatagramSocket Inputsocket;
	Payload payload;
	private byte[] inData = new byte[1440];
	public ArrayList <byte[]> bytes = new ArrayList<>();
	public boolean allReceived = false;
	private int sequence = 0;

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

		DatagramPacket input = new DatagramPacket(inData, inData.length,InetAddress.getLoopbackAddress(),50000);
		try {
			Inputsocket.receive(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		byte[] head = Arrays.copyOfRange(input.getData(), 0,20);
		int ack = head[0];
		if(ack==2) {
			return ack;
		}
		int sequence = payload.byteToInt(Arrays.copyOfRange(head, 4, 8));
		long checksum = payload.byteToLong(Arrays.copyOfRange(head, 8, 16));
		int conlength = payload.byteToInt(Arrays.copyOfRange(head, 16, 20)) + 20;

		System.out.println("Ack: "+ack+" Sequence :"+sequence+" checksum:"+ checksum + " conlength: " + conlength);
		byte[] content = Arrays.copyOfRange(input.getData(), 20, conlength);
		if ((generateChecksum(content) == checksum) && (sequence == this.sequence)) {
			System.out.println("fully received");
			bytes.add(content);
			this.sequence++;
		} else if(ack == 2){
			allReceived = true;
		} else {
			System.out.println("package false");
		}
		return ack;
	}

	private long generateChecksum(byte[] field) {
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
			output.toByteArray(),output.toByteArray().length,InetAddress.getLoopbackAddress(),50000));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
