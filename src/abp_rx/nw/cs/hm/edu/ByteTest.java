package abp_rx.nw.cs.hm.edu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteTest {
	
	private int dataPkgSize = 0;
	private static long sequenceNrSize = 1;
	private int checkSumNrSize = 0;
	private static long ack = 0;

	public static void main(String[] args) throws IOException {
		Payload payload = new Payload();
		ByteArrayInputStream output = new ByteArrayInputStream();
//		output.write(storeLongInToByte(ack));
		output.read(storeLongInToByte(sequenceNrSize));
//		try {
//			output.write(storeLongInToByte(generateChecksum(data)));
//			output.write(data);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		System.out.println(output.read());
	}
	
	public static int byteToInt(byte[] input) {
		final ByteBuffer buff = ByteBuffer.wrap(input);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		return buff.getInt();
	}
	
	public static byte[] storeLongInToByte(Long data) {
//		// bytes needed to store data
//		int n = 1;
//
//		// if we need to store a 0 we still need atleast one byte
//		if (data != 0l) {
//			n = (int) Math.ceil((Math.log(data) / Math.log(2)) / 8);
//		}
//
//		byte dataArray[] = new byte[n];
//
//		for (int i = 0; n > i; i++) {
//			int bitmask = 0x0000FF;
//			byte valueToStore = (byte) (data & bitmask);
//
//			dataArray[i] = valueToStore;
//
//			System.out.println("bitmask: " + bitmask);
//			System.out.println("value: " + valueToStore);
//
//			for (int x = 0; x <= 7; x++) {
//				data >>>= data;
//			}
//		}
//
//		// bytes needed
//		System.out.println(n);
//		return dataArray;
		final ByteBuffer bb = ByteBuffer.allocate(Long.SIZE / Byte.SIZE);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(data);
		return bb.array();
	}
}
