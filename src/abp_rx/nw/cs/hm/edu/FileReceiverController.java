package abp_rx.nw.cs.hm.edu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class FileReceiverController implements Runnable{
	
	public enum State {
		GET_PACKAGES, CHECKFIRSTSUM, GETACK0, GETACK1, GETANOTHERPACKAGEack1, GETANOTHERPACKAGEack0, BUILD_FILE
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
	private int ack;
	
	public FileReceiverController() {
		currentState = State.GET_PACKAGES;
		transition = new Transition[State.values().length] [Msg.values().length];
		transition[State.GET_PACKAGES.ordinal()][Msg.READ_HEADER.ordinal()] = new Checksum();
		transition[State.CHECKFIRSTSUM.ordinal()][Msg.CHECKSUM_SUCCESSFULL.ordinal()] = new SendAck1();
		transition[State.CHECKFIRSTSUM.ordinal()][Msg.TIMEOUT.ordinal()] = new GoBackToIdle();
		transition[State.GETACK0.ordinal()][Msg.READ.ordinal()] = new GetNextPackageACK1();
		transition[State.GETACK1.ordinal()][Msg.READ.ordinal()] = new GetNextPackageACK0();

		transition[State.GETACK0.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new GetLastPackageAgain0();
		transition[State.GETACK1.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new GetLastPackageAgain1();
		transition[State.GETANOTHERPACKAGEack0.ordinal()][Msg.CHECKSUM.ordinal()] = new GetNextPackageACK1();
		transition[State.GETANOTHERPACKAGEack1.ordinal()][Msg.CHECKSUM.ordinal()] = new GetNextPackageACK0();
		transition[State.GETANOTHERPACKAGEack0.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new GetLastPackageAgain0();
		transition[State.GETANOTHERPACKAGEack1.ordinal()][Msg.CHECKSUM_UNSUCCESFULL.ordinal()] = new GetLastPackageAgain1();
		transition[State.GETACK0.ordinal()][Msg.ALL_PACKAGES_RECEIVED.ordinal()] = new BuildFile();
		transition[State.GETACK1.ordinal()][Msg.ALL_PACKAGES_RECEIVED.ordinal()] = new BuildFile();
	}
	
	public static void main(String[] args) {
		new Thread(new FileReceiverController()).start();
	}
	
	public void processMsg(Msg input){
		System.out.println("INFO Received "+input+" in state "+currentState);
		Transition trans = transition[currentState.ordinal()][input.ordinal()];
		if(trans != null){
			currentState = trans.execute(input);
		}
		System.out.println("INFO State: "+currentState);
	}
	
	public void run() {
		switch(currentState) {
		case GET_PACKAGES:
			processMsg(Msg.READ_HEADER);
			
		case CHECKFIRSTSUM:
			switch(ack) {
			case 0:
				receiver.sendConnection(0);
				processMsg(Msg.TIMEOUT);
				
			case 1:
				receiver.sendConnection(1);
				processMsg(Msg.CHECKSUM_SUCCESSFULL);
				
			case 2:
				processMsg(Msg.ALL_PACKAGES_RECEIVED);
			}
			
		case GETANOTHERPACKAGEack0:
			System.out.println(ack);
			switch(ack) {
			case 1:
				receiver.sendConnection(1);
				processMsg(Msg.CHECKSUM);
				
			case 0:
				receiver.sendConnection(0);
				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				
//			case 2:
//				System.out.println(ack);
//				processMsg(Msg.ALL_PACKAGES_RECEIVED);
			}
			
		case GETANOTHERPACKAGEack1:
			switch(ack) {
			case 0:
				receiver.sendConnection(0);
				processMsg(Msg.CHECKSUM);
				
			case 1:
				receiver.sendConnection(1);
				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
		
			case 2:
				processMsg(Msg.ALL_PACKAGES_RECEIVED);
			}
			
		case GETACK0:
			ack = receiver.waitForPacket();
			System.out.println("Package Received!");
			if(ack == 0) {
				processMsg(Msg.READ);
			} else {
				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				}
			
		case GETACK1:
			ack = receiver.waitForPacket();
			System.out.println("Package Received!");
			if(ack == 1) {
				processMsg(Msg.READ);
			} else {
				processMsg(Msg.CHECKSUM_UNSUCCESFULL);
				}
			
		default:
			break;
		}
	}
	
	abstract class Transition {
		abstract public State execute(Msg input);
	}
	
	class Checksum extends Transition {
		@Override
		public State execute(Msg input) {
			try {
				pay = new Payload();
			} catch (IOException e) {
				e.printStackTrace();
			}
			receiver = new RX(pay);
			ack = receiver.waitForPacket();
			System.out.println("Package Received!");
			return State.CHECKFIRSTSUM;
		}
	}
	
	class SendAck0 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETANOTHERPACKAGEack1;
		}
	}
	
	class SendAck1 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETANOTHERPACKAGEack0;
		}
	}
	
	class GoBackToIdle extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GET_PACKAGES;
		}
	}
	
	class GetNextPackageACK1 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETACK1;
		}
	}
	
	class GetNextPackageACK0 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETACK0;
		}
	}
	
	class GetLastPackageAgain1 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETACK1;
			//oder return State.SENDACK0;
		}
	}
	
	class GetLastPackageAgain0 extends Transition {
		@Override
		public State execute(Msg input) {
			//System.out.println("Package Received!");
			return State.GETACK0;
			//oder return State.SENDACK0;
		}
	}
	
	class BuildFile extends Transition {
		@Override
		public State execute(Msg input) {
			try (FileOutputStream file = new FileOutputStream("input.txt")) {
				for (int i = 0; i < receiver.bytes.size(); i++) {  
					file.write(receiver.bytes.get(i));
					file.flush();
				}
				file.close();
			} catch (IOException e) {
				System.out.println("CANT WRITE TO FILE!!!");
				e.printStackTrace();
			}
			return State.GET_PACKAGES;
		}
	}

}
