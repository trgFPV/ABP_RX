package abp_rx.nw.cs.hm.edu;

public class FileReceiver implements Runnable {

	public enum State {
		GET_PACKAGES, CHECKFIRSTSUM, SENDACK0, SENDACK1, GETANOTHERPACKAGE, BUILD_FILE
	}

	public State state;

	public FileReceiver() {
		state = State.GET_PACKAGES;
	}

	public static void main(String[] args) {
		new Thread(new FileReceiver()).start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
