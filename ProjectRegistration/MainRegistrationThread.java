import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is Main Registration thread that is used to handle interaction with the client and the directory servers.
 * This has the list of all the available directory servers.
 * @author sandeep
 *
 */
public class MainRegistrationThread {


	// Obtains the port number from the config file.
	private static int getPortNumber(){

		try {
			File file = new File("server.cfg");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
			}
			fileReader.close();			
			String servercfg = stringBuffer.toString();
			String[] arr = servercfg.split(":");
			String substring  = arr[1].substring(0, arr[1].length()-1);
			if(arr.length == 2)
				return Integer.parseInt(substring);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 60000;

	}


	public static void main(String[] args) {

		try {
			//gets the port number from th cofig.
			int port = getPortNumber();
			ServerSocket server = new ServerSocket(port);
			//Keeps listening on the port. Accepts connections fro directory server from both client and directory on this port.
			while(true){
				Socket conn = server.accept();
				(new ProcessRegistrationThread(conn)).start();
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}

	}



}
