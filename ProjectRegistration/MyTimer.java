import java.util.Timer;
import java.util.TimerTask;
/**
 * 
 * @author sandeep
 *	This class is used to determine if the 30 seconds is over and the directory server has to be updated .
 */

public class MyTimer {
	boolean valid = true;

	public MyTimer() {
		Timer timer = new Timer();
		//Scheduling NextTask() call in 30 second. 
		timer.schedule(new ChangeValueTask(), 30 * 1000);

	}
//This class changes the value of the dir server false, so that if its not updated again it is removed from the list.
	class ChangeValueTask extends TimerTask {
		@Override
		public void run() {
			valid=false;
		}
	} 

	public boolean isValid(){
		return valid;
	}
}
