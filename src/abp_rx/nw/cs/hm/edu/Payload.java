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

	public int byteToInt(byte[] input) {
		final ByteBuffer buff = ByteBuffer.wrap(input);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		return buff.getInt();
	}
}
