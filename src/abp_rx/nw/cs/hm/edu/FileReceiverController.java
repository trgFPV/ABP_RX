package abp_rx.nw.cs.hm.edu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class FileReceiverController implements Runnable {

	public enum State {
		GET_PACKAGE, CHECKFIRSTSUM, GETACK0, GETACK1, GETANOTHERPACKAGEack1, GETANOTHERPACKAGEack0, BUILD_FILE
	}

	public enum Msg {
		READ_HEADER, TIMEOUT, CHECKSUM_UNSUCCESFULL, CHECKSUM_SUCCESSFULL, READ, CHECKSUM, BUILD_SUCCESSFULL, ALL_PACKAGES_RECEIVED
	}

	// current state of the FSM
	private State currentState;
	// 2D array defining all transitions that can occur
	private Transition[][] transition;

	private Payload pay;
	private RX receiver;
	private int ack = 1;
	private boolean process = true;

	public FileReceiverController() {
		currentState = State.GET_PACKAGE;
		transition = new Transition[State.values().length][Msg.values().length];
		transition[State.GET_PACKAGE.ordinal()][Msg.CHECKSUM.ordinal()] = new Checksum();
		transition[State.GET_PACKAGE.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new Checksum();
		transition[State.GET_PACKAGE.ordinal()][Msg.ALL_PACKAGES_RECEIVED.ordinal()] = new BuildFile();
		
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
		while(process) {
			
			int newack = receiver.waitForPacket();
			
			switch(ack) {
			case 0:
				System.out.println("0");
				if (ack == newack) {
					receiver.sendConnection(0);
					processMsg(Msg.CHECKSUM);
				} else if (ack == 1) {
					receiver.sendConnection(1);
					processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				} else if(newack==2) {
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
				} else if(newack==2) {
					processMsg(Msg.ALL_PACKAGES_RECEIVED);
				}
				break;
			}
			
			if(ack==1) {
				ack = 0;
			} else {
				ack = 1;
			}
		}
//		switch (currentState) {
//		case GET_PACKAGES:
//			processMsg(Msg.READ_HEADER);
//			break;
//
//		case CHECKFIRSTSUM:
//			if (ack == 0) {
//				receiver.sendConnection(0);
//				processMsg(Msg.TIMEOUT);
//			} else if (ack == 1) {
//				System.out.println("juhu");
//				receiver.sendConnection(1);
//				processMsg(Msg.CHECKSUM_SUCCESSFULL);
//			} else {
//				processMsg(Msg.ALL_PACKAGES_RECEIVED);
//			}
//			break;
//
//		case GETANOTHERPACKAGEack0:
//			if (ack == 0) {
//				receiver.sendConnection(0);
//				processMsg(Msg.CHECKSUM);
//			} else if (ack == 1) {
//				receiver.sendConnection(1);
//				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
//			} else {
//				processMsg(Msg.ALL_PACKAGES_RECEIVED);
//			}
//			break;
//
//		case GETANOTHERPACKAGEack1:
//			if (ack == 1) {
//				receiver.sendConnection(1);
//				processMsg(Msg.CHECKSUM);
//			} else if (ack == 0) {
//				receiver.sendConnection(0);
//				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
//			} else {
//				processMsg(Msg.ALL_PACKAGES_RECEIVED);
//			}
//			break;
//
//		case GETACK0:
//			ack = receiver.waitForPacket();
//			if (ack == 0) {
//				processMsg(Msg.READ);
//			} else {
//				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
//			}
//			break;
//
//		case GETACK1:
//			if (currentState != State.GETACK1) {
//				System.out.println("ALDA WAS LOS HIER -.-");
//			}
//			ack = receiver.waitForPacket();
//			if (ack == 1) {
//				processMsg(Msg.READ);
//			} else {
//				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
//			}
//			break;
//
//		default:
//			break;
//		}
	}

	abstract class Transition {
		abstract public State execute(Msg input);
	}

	class Checksum extends Transition {
		@Override
		public State execute(Msg input) {
//			try {
//				pay = new Payload();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			receiver = new RX(pay);
//			ack = receiver.waitForPacket();
//			return State.CHECKFIRSTSUM;
			return State.GET_PACKAGE;
		}
	}

	class SendAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			System.out.println("now returning GETANOTHERPACKAGEack1");
			return State.GETANOTHERPACKAGEack1;
		}
	}

	class SendAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GETANOTHERPACKAGEack0;
		}
	}

	class GoBackToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GET_PACKAGE;
		}
	}

	class GetNextPackageACK1 extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GETACK1;
		}
	}

	class GetNextPackageACK0 extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GETACK0;
		}
	}

	class GetLastPackageAgain1 extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GETANOTHERPACKAGEack1;
			// oder return State.SENDACK0;
		}
	}

	class GetLastPackageAgain0 extends Transition {
		@Override
		public State execute(Msg input) {
			// System.out.println("Package Received!");
			return State.GETANOTHERPACKAGEack0;
			// oder return State.SENDACK0;
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
				System.out.println("CANT WRITE TO FILE!!!");
				e.printStackTrace();
			}
			process = false;
			return State.GET_PACKAGE;
		}
	}

}
