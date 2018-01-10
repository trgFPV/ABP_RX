package abp_rx.nw.cs.hm.edu;

import java.io.IOException;

public class RxTest implements Runnable {
	
	public static void main(String[] args) {
		Payload pay = null;
		try {
			pay = new Payload();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RX test = new RX(pay);
		
		new Thread(new RxTest()).start();
		test.waitForPacket();
	}
	
	public void run() {
		
	}
}
