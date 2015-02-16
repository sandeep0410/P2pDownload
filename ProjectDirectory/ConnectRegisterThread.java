import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
/**
 * This class is responsible for pinging the registering server every 30 seconds saying that it is active for interaction.
 * @author sandeep
 *
 */

public class ConnectRegisterThread extends Thread{

	Socket connection;

	public ConnectRegisterThread(Socket socket) {
		connection = socket;
	}

	public ConnectRegisterThread() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {

		try {

			if(connection.isClosed()){
				connection= new Socket(connection.getInetAddress(), connection.getPort());
			}
			OutputStream outToClient = connection.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToClient);	
			String ipAddress = connection.getLocalAddress().toString();
			ipAddress = ipAddress.substring(ipAddress.indexOf('/')+1);
			String input = "DirServ " + ipAddress +" 50000";
			out.writeUTF(input);
			DataInputStream in = new DataInputStream(connection.getInputStream());
			outToClient.close();
			out.close();
			in.close();
			interrupt();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
