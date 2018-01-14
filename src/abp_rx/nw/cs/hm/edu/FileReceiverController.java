package abp_rx.nw.cs.hm.edu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import javax.swing.Timer;

public class FileReceiverController implements Runnable {

	public enum State {
		GET_PACKAGE
	}

	public enum Msg {
		TIMEOUT, CHECKSUM_UNSUCCESFULL, CHECKSUM, ALL_PACKAGES_RECEIVED
	}

	// current state of the FSM
	private State currentState;
	// 2D array defining all transitions that can occur
	private Transition[][] transition;
	private Payload pay;
	private RX receiver;
	private int ack = 1;
	private boolean process = true;
	private int TIME_TO_WAIT = 10;
	private int timeoutCounter = 0;

	private Timer timer;

	public FileReceiverController() {
		currentState = State.GET_PACKAGE;
		transition = new Transition[State.values().length][Msg.values().length];
		transition[State.GET_PACKAGE.ordinal()][Msg.CHECKSUM.ordinal()] = new Checksum();
		transition[State.GET_PACKAGE.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new Checksum();
		transition[State.GET_PACKAGE.ordinal()][Msg.ALL_PACKAGES_RECEIVED.ordinal()] = new BuildFile();
		transition[State.GET_PACKAGE.ordinal()][Msg.TIMEOUT.ordinal()] = new Fail();
		timer = new Timer(1000, e->countUp());
	}

	private void countUp() {
		timeoutCounter += 1;
		if (timeoutCounter == TIME_TO_WAIT) {
			this.processMsg(Msg.TIMEOUT);
		}
	}

	public static void main(String[] args) {
		new Thread(new FileReceiverController()).start();
	}

	public void processMsg(Msg input) {
		System.out.println("INFO Received " + input + " in state " + currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if (trans != null) {
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: " + currentState);
	}

	public void run() {
		try {
			pay = new Payload();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		receiver = new RX(pay);
		while (process) {

			int newack = receiver.waitForPacket();
			timeoutCounter = 0;

			switch (ack) {
			case 0:
				System.out.println("0");
				if (ack == newack) {
					receiver.sendConnection(0);
					processMsg(Msg.CHECKSUM);
				} else if (ack == 1) {
					receiver.sendConnection(1);
					processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				} else if (newack == 2) {
					processMsg(Msg.ALL_PACKAGES_RECEIVED);
				}
				break;
			case 1:

				System.out.println("1");
				if (ack == newack) {
					receiver.sendConnection(1);
					processMsg(Msg.CHECKSUM);
				} else if (ack == 0) {
					receiver.sendConnection(0);
					processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				} else if (newack == 2) {
					processMsg(Msg.ALL_PACKAGES_RECEIVED);
				}
				break;
			}

			if (ack == 1) {
				ack = 0;
			} else {
				ack = 1;
			}
		}
	}

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class Checksum extends Transition {
		@Override
		public State execute(Msg input) {
			return State.GET_PACKAGE;
		}
	}
	
	class Fail extends Transition {
		@Override
		public State execute(Msg input) {
			receiver.bytes.clear();
			receiver.sendConnection(3);
			return State.GET_PACKAGE;
		}
	}

	class BuildFile extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("build File");
			try (FileOutputStream file = new FileOutputStream("test.txt")) {
				for (int i = 0; i < receiver.bytes.size(); i++) {
					file.write(receiver.bytes.get(i));
					file.flush();
				}
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			process = false;
			return State.GET_PACKAGE;
		}
	}

}
