import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * This is the Main Working part of the Registration thread. This Registration thread is responsible for executing all the command from 
 * the client and the directory servers.
 * @author sandeep
 *
 */
public class ProcessRegistrationThread extends Thread{

	Socket connection;
	boolean isLoggedin;

	public ProcessRegistrationThread(Socket conn) {
		connection = conn;
	}

	public ProcessRegistrationThread() {

	}
	//runs all the commands from the client i.e. register login and exit, also this is responsible for updating the list of directory servers.
	private int runCommand(String commandFromClient) {
		HashMap<String, String> registeredClients = Utils.registeredClients;
		if(commandFromClient.startsWith("DirServ")){
			String[] arr = commandFromClient.split(" ");
			if(arr.length != 4 && arr.length !=3)
				return Constants.RESPONSE_INVALID_COMMANDS;

			if(arr[0].equals("DirServ") && (arr[1].split("\\.")).length == 4){
				int port = Integer.parseInt(arr[2]);
				if(port>1024 && port <65536){
					String serverDetails = arr[1]+":" +arr[2];
					if(Utils.validserverList.containsKey(serverDetails)){
						Utils.validserverList.remove(serverDetails);
					}
					Utils.validserverList.put(serverDetails, new MyTimer());
					return Constants.RESPONSE_SUCCESS;
				}else
					return Constants.RESPONSE_INVALID_ARGUMENTS;
			}else
				return Constants.RESPONSE_INVALID_COMMANDS;

		} else if(commandFromClient.startsWith("register")){
			//this is the command from the client to register itself.
			String[] arr = commandFromClient.split(" ");
			if(arr.length != 4)
				return Constants.RESPONSE_INVALID_COMMANDS;
			if((!registeredClients.containsKey(arr[1])) && arr[2].equals(arr[3]) ){
				registeredClients.put(arr[1], arr[2]);
				return Constants.RESPONSE_SUCCESS;
			}else if(registeredClients.containsKey(arr[1])){
				return Constants.RESPONSE_USERNAME_EXISTS;
			}else if(!arr[2].equals(arr[3]))
				return Constants.RESONSE_PASSWORD_MISMATCH;
			return Constants.RESPONSE_INVALID_COMMANDS;

		}else if(commandFromClient.startsWith("login")){
			//This is the command from the client to login itself.
			String[] arr = commandFromClient.split(" ");
			if(arr.length != 3)
				return Constants.RESPONSE_INVALID_ARGUMENTS;
			if(registeredClients.containsKey(arr[1])){
				if(arr[2].equals(registeredClients.get(arr[1]))){
					if(isLoggedin)
						return Constants.RESPONSE_ALREADY_LOGGED_IN;
					isLoggedin = true;
					return Constants.RESPONSE_SUCCESS;
				}else
					return Constants.RESPONSE_INCORRECT_CREDENTIALS;
			}else
				return Constants.RESPONSE_INCORRECT_CREDENTIALS;

		}else if(commandFromClient.equals("exit")){

			if(isLoggedin){
				isLoggedin = false;
				return Constants.RESPONSE_SUCCESS;
			}else
				return Constants.RESPONSE_NOT_LOGGED_IN;
		}
		return 0;

	}	

	@Override
	public void run() {
		super.run();
		try{
			OutputStream outToClient = connection.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToClient);
			DataInputStream in = new DataInputStream(connection.getInputStream());
			String commandFromClient = in.readUTF();			

			int response = runCommand(commandFromClient);
			String output = "Server Response: " +Utils.getResponseFromCode(response);

			if(commandFromClient.startsWith("DirServ") && commandFromClient.split(" ").length ==3){				

				if(response ==Constants.RESPONSE_SUCCESS){
					for(String s: Utils.getServerList()){
						output= output +"\n"+s;
					}					
					out.writeUTF(output);
									}
				out.writeUTF(output);
				in.close();
				outToClient.close();
				out.close();
			} else if(commandFromClient.startsWith("list")) {
				//this part of the code will list all the directories that are registered with this server.
				StringBuffer value = new StringBuffer();

				for(String s : Utils.getServerList()) {
					if(!connection.getInetAddress().toString().startsWith("/"+s.substring(0, s.indexOf(":")))) {
						//System.out.println("Adding dir server");
						value.append(s).append(" ");
					}
				}
				out.writeUTF(value.toString());
				in.close();
				outToClient.close();
				out.close();
			} else{

				while(true){

					if(commandFromClient.startsWith("login") && response ==Constants.RESPONSE_SUCCESS){						
						output= output +"\n"+Utils.getServerforClient();						
					}
					out.writeUTF(output);
					commandFromClient = in.readUTF();
					response = runCommand(commandFromClient);
					output = "Server Response: " +Utils.getResponseFromCode(response);
				}
			}
		} catch (IOException e){
			//e.printStackTrace();
		}
	}




}
