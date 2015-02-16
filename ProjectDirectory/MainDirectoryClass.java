	import java.io.BufferedReader;
	import java.io.DataInputStream;
	import java.io.DataOutputStream;
	import java.io.File;
	import java.io.FileReader;
	import java.io.IOException;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.net.UnknownHostException;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.concurrent.ScheduledThreadPoolExecutor;
	import java.util.concurrent.TimeUnit;
	
	/**
	 * This class contains the main thread of the Directory project. This thread is reposnible for executing all the commands from the client.
	 * This also takes care of the registering itself with the registration server and then constatntly updating every 30 seconds.
	 * @author sandeep
	 *
	 */
	public class MainDirectoryClass {
	
		public static HashMap<String, HashSet<String>> mClientFiles = new HashMap<String, HashSet<String>>();
		public static String regIpaddress = getValueFromConfig("regIpaddress");
		public static int regPort = Integer.parseInt(getValueFromConfig("regPort"));
		public static int dirPort = Integer.parseInt(getValueFromConfig("dirPort")); 
	
		private static void executeFindCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length != 2) {
				System.out.println("Server Response: 405 Invalid arguments.");
				return;
			}
	
			//Searching the file locally before looking up at other clients.
			HashSet<String> clients = new HashSet<String>();
			String[] toks = input.split(" ");
	
			for(String s: mClientFiles.keySet()) {
				if(mClientFiles.get(s).contains(toks[1])) {
					clients.add(s);
				}
			}
	
			//Searching othe directory servers for the file, if directory are linked with other clients that have the files then the directory server is returnes.
			HashSet<String> servers = new HashSet<String>();
			try {
				Socket regSocket = new Socket(regIpaddress, regPort);
				DataInputStream inS = new DataInputStream(regSocket.getInputStream());
				DataOutputStream outS = new DataOutputStream(regSocket.getOutputStream());
				outS.writeUTF("list");
	
				String resp = inS.readUTF();
	
				for(String s: resp.split(" ")) {
					if(!s.isEmpty()) {
						servers.add(s);
					}
				}
				regSocket.close();
				inS.close();
				outS.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			for(String server: servers) {
				try {
					System.out.println("Connecting to " + server);
					String[] tokens = server.split(":");
	
					if(tokens.length != 2) {
						continue;
					}
	
	
					Socket s = new Socket(tokens[0], Integer.parseInt(tokens[1]));
					DataInputStream inS = new DataInputStream(s.getInputStream());
					DataOutputStream outS = new DataOutputStream(s.getOutputStream());
					outS.writeUTF("lookup " + toks[1]);
	
					String resp = inS.readUTF();				
					for(String str: resp.split(" ")) {
						if(!str.isEmpty()) { 
							clients.add(str);
						}
					}
					s.close();
					inS.close();
					outS.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
	
			StringBuffer output = new StringBuffer();
			output.append("Server Response: 200 SUCCESS").append("\n");
	
			for(String c: clients) {
				if(!c.isEmpty()) {
					output.append(c).append(" ").append(toks[1]).append("\n");
				}
			}
	
	
			try {
				out.writeUTF(output.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private static void executeServerShareCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length != 1) {
				try {
					out.writeUTF("405 Invalid arguments.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	
			HashSet<String> files = new HashSet<String>();
	
			// first get all the files available locally
			for(String s: mClientFiles.keySet()) {
				files.addAll(mClientFiles.get(s));
			}		
	
			// query register server for rest of the directory servers
			HashSet<String> servers = new HashSet<String>();
			try {
				Socket regSocket = new Socket(regIpaddress, regPort);
				DataInputStream inS = new DataInputStream(regSocket.getInputStream());
				DataOutputStream outS = new DataOutputStream(regSocket.getOutputStream());
				outS.writeUTF("list");
	
				String resp = inS.readUTF();
				for(String s: resp.split(" ")) {
					servers.add(s);
				}
				regSocket.close();
				inS.close();
				outS.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			// query each directory server for the list of files
			for(String server: servers) {
				try {
					String[] tokens = server.split(":");
					if(tokens.length != 2) {
						continue;
					}
					Socket s = new Socket(tokens[0], Integer.parseInt(tokens[1]));
					DataInputStream inS = new DataInputStream(s.getInputStream());
					DataOutputStream outS = new DataOutputStream(s.getOutputStream());
					outS.writeUTF("filelist");
	
					String resp = inS.readUTF();
					for(String str: resp.split(" ")) {
						files.add(str);
					}
					s.close();
					inS.close();
					outS.close();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
			// aggregate all files info and send response
			StringBuffer output = new StringBuffer();
			output.append("200 SUCCESS").append("\n");
			for(String f: files) {
				output.append(f).append("\n");
			}
	
			try {
				out.writeUTF(output.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		public static void main(String[] args) {
	
			// directory server socket which responds to all other client requests
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(dirPort);
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			try {
				Socket regSocket = new Socket(regIpaddress, regPort);
				ScheduledThreadPoolExecutor s = new ScheduledThreadPoolExecutor(1);
				s.scheduleWithFixedDelay(new ConnectRegisterThread(regSocket),
						0, 30000, TimeUnit.MILLISECONDS);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	System.out.println("Directory Server Started ");
			while(true) {
				Socket clientSocket = null;
				try {				
					clientSocket = serverSocket.accept();
					DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
					DataInputStream in = new DataInputStream(clientSocket.getInputStream());
					serveClientRequest(in, out, clientSocket);
				} catch (IOException e) {
					System.err.println("Accept failed.");
					System.exit(1);
				}
			}
	
		}
	
		private static String getValueFromConfig(String key) {
	
			try {
				File file = new File("server.cfg");
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if(line.startsWith(key)) {
						String[] toks = line.split("=");
						if(toks.length != 2) {
							continue;
						}
						return toks[1];
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			return new String();
		}
	
		private static void serveClientRequest(DataInputStream in,
				DataOutputStream out, Socket clientSocket) {
	
			String input = new String();
			try {
				input = in.readUTF();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	
			if(input.startsWith("share ")){
				executeShareCommand(clientSocket, in, out, input);
			} else if(input.startsWith("find ")){
				executeFindCommand(in, out, input);
			} else if(input.startsWith("remove ")){
				executeRemoveCommand(clientSocket, in, out, input);
			} else if(input.startsWith("lookup ")) {
				executeLookupCommand(out, input);
			} else if(input.startsWith("filelist")) {
				executeFileListCommand(out, input);
			} else if(input.startsWith("servershare")){
				executeServerShareCommand(in, out, input);
			} else{
				try {
					out.writeUTF("Server Response: 400 INVALID COMMANDS");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	
		}
	
		private static void executeFileListCommand(DataOutputStream out,
				String input) {
	
			if(input.split(" ").length != 1) {
				try {
					out.writeUTF("");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	
			HashSet<String> files = new HashSet<String>();
			for(String s: mClientFiles.keySet()) {
				files.addAll(mClientFiles.get(s));
			}
	
			StringBuffer output = new StringBuffer();
			for(String f: files) {
				output.append(f).append(" ");
			}
	
			try {
				out.writeUTF(output.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		private static void executeLookupCommand(DataOutputStream out, String input) {
	
			if(input.split(" ").length != 2) {
				try {
					out.writeUTF("");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	
	
			String[] toks = input.split(" ");
			StringBuffer output = new StringBuffer();
	
			for(String s: mClientFiles.keySet()) {
				if(mClientFiles.get(s).contains(toks[1])) {
					output.append(s).append(" ");
				}
			}
	
	
			try {		
				out.writeUTF(output.toString());			
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		private static void executeShareCommand(Socket clientSocket, DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length <= 1) {
				try {
					out.writeUTF("405 Invalid arguments.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	
			try {
				StringBuffer output = new StringBuffer();
				output.append("200 SUCCESS").append("\n");
				String[] toks = input.split(" ");
				HashSet<String> hs = new HashSet<String>();
				for(int i = 1; i < toks.length; ++i) {
					hs.add(toks[i]);
					output.append(toks[i]).append("\n");
				}
	
				//get the client IP and port for records
				StringBuffer s = new StringBuffer();
				s.append(clientSocket.getInetAddress().toString().substring(1)).append(":").append("40000");
				mClientFiles.put(s.toString(), hs);
	
				out.writeUTF(output.toString());
	
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		private static void executeRemoveCommand(Socket clientSocket, DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length <= 1) {
				try {
					out.writeUTF("405 Invalid arguments.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
	
			try {
	
				//get the client IP and port for records
				StringBuffer s = new StringBuffer();
				s.append(clientSocket.getInetAddress().toString().substring(1)).append(":").append("40000");
	
				StringBuffer output = new StringBuffer();
				if(!mClientFiles.containsKey(s.toString())) {
					output.append(Constants.CLIENT_NOT_FOUND).append(" CLIENT_NOT_FOUND");
					out.writeUTF(output.toString());
					return;
				} else {
					HashSet<String> h = mClientFiles.get(s.toString());
					String[] toks = input.split(" ");
					if(!h.contains(toks[1])) {
						output.append(Constants.REPONSE_FILE_NOT_FOUND).append(" FILE_NOT_FOUND");
						out.writeUTF(output.toString());
						return;
					}
					// remove the file from clients list and update the records
					h.remove(toks[1]);
					mClientFiles.put(s.toString(), h);
					out.writeUTF("200 SUCCESS");
					return;
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
	
	}
	
