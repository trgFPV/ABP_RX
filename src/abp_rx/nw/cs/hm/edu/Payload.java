package abp_rx.nw.cs.hm.edu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Payload {
	private ArrayList<Byte> dataArray = new ArrayList<>();
	private int idx;
	private int size;

	/*
	 * create a new payload object using a existing file on the hard disk this
	 * will populate the byte array list
	 */
	public Payload() throws IOException {

	}

	public byte[] getCompleteDataArray() {
		byte[] array = new byte[this.size];
		for (int i = 0; i < size; i++) {
			array[i] = dataArray.get(i);
		}
		return array;
	}

	/*
	 * returns the size of the payload in bytes
	 */
	public int getSize() {
		return size;
	}

	public long byteToLong(byte[] input) {
		final ByteBuffer buff = ByteBuffer.wrap(input);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		return buff.getLong();
	}
	
	public int byteToInt(byte[] input) {
		final ByteBuffer buff = ByteBuffer.wrap(input);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		return buff.getInt();
	}
	
	public static byte[] storeIntInToByte(int data) {
		
		final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(data);
		return bb.array();
	}
	
	public static byte[] storeLongInToByte(Long data) {
		// // bytes needed to store data
		// int n = 1;
		//
		// // if we need to store a 0 we still need atleast one byte
		// if (data != 0l) {
		// n = (int) Math.ceil((Math.log(data) / Math.log(2)) / 8);
		// }
		//
		// byte dataArray[] = new byte[n];
		//
		// for (int i = 0; n > i; i++) {
		// int bitmask = 0x0000FF;
		// byte valueToStore = (byte) (data & bitmask);
		//
		// dataArray[i] = valueToStore;
		//
		// System.out.println("bitmask: " + bitmask);
		// System.out.println("value: " + valueToStore);
		//
		// for (int x = 0; x <= 7; x++) {
		// data >>>= data;
		// }
		// }
		//
		// // bytes needed
		// System.out.println(n);
		// return dataArray;
		final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(data);
		return bb.array();
	}


	
}
