import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

/**
 * 
 * @author sandeep
 *This is the utility class that contains all the response codes and stores all the static server lists.
 */
public class Utils {

	private static ArrayList<String> serverList = new ArrayList<String>();
	public static HashMap<String, MyTimer> validserverList  = new HashMap<String, MyTimer>(); 
	public static HashMap<String, String> registeredClients = new HashMap<String, String>();
	private static HashMap<String,Integer> serverListRecords = new HashMap<String, Integer>();

	//This function performs the load balancing among directory servers. this function will divide the load equally among all the directory servers and return
	//the name of server to client accordingly
	private static String loadBalanaceServers(ArrayList<String> lServers) {
		//if none of the servers have been assigned then return the first server that is stored
		if(serverListRecords.size() == 0){
			serverListRecords.put(lServers.get(0), 1);
			return lServers.get(0);
		}

		//Just checks if any of the servers which is not assigned to any client
		//Returns the unassigned server to the client
		for(String s :  lServers){
			if(!serverListRecords.containsKey(s)){
				serverListRecords.put(s, 1);
				return s;
			}
		}

		//if all the servers are assigned then it checks for the server which is assigned to the lowest number of clients.
		int i=1;
		while(i<10){
			Set<String> keys = serverListRecords.keySet();
			for(String k:keys){		
				if(serverListRecords.get(k).equals(i) && lServers.contains(k)){
					serverListRecords.put(k, i+1);
					return k;
				}
			}
			i=i+1;
		}

		//This is just in case other conditions fail some how.
		int randomNumber = (new Random()).nextInt(lServers.size());
		return lServers.get(randomNumber);

	}

	//returns the list of directory servers
	public static ArrayList<String> getServerList() {
		serverList.clear();
		Set<String> keys = validserverList.keySet();
		for(String k:keys){		
			if(validserverList.get(k).isValid())
				serverList.add(k);
		}
		return serverList;
	}

	//returns the corresponding string value to the response code.
	public static String getResponseFromCode(int responseCode){
		String response = "Response";
		switch(responseCode){
		case 200:
			response = "200 Success";
			break;
		case 400:
			response = "400 Invalid Commands";
			break;
		case 401:
			response = "401 Username Already Exists";
			break;
		case 402:
			response = "402 Password does not match";
			break;
		case 403:
			response = "403 Invalid Username/Password";
			break;
		case 404:
			response = "404 File not found";
			break;
		case 405:
			response = "405 Invalid Arguments";
			break;
		case 406:
			response ="406 Not logged in";
			break;
		case 409:
			response ="Already Logged in. Please exit first";
			break;
		default:
			response = "400 Invalid Commands";
			break;
		}
		return response;

	}
	//returns the client a directoryserver ipaddress and port which is used to download files.
	public static String getServerforClient() {
		ArrayList<String> servers = getServerList();
		if(servers.size()==0){
			return "No Servers available";
		}else{

			String serverForClient = loadBalanaceServers(servers);
			return serverForClient;
		}
	}


}
