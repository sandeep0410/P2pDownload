	
	import java.io.BufferedInputStream;
	import java.io.BufferedOutputStream;
	import java.io.BufferedReader;
	import java.io.DataInputStream;
	import java.io.DataOutputStream;
	import java.io.File;
	import java.io.FileInputStream;
	import java.io.FileNotFoundException;
	import java.io.FileOutputStream;
	import java.io.FileReader;
	import java.io.IOException;
	import java.io.UnsupportedEncodingException;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.net.UnknownHostException;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.HashSet;
	import java.util.Scanner;
	/**
	 * 
	 * @author sandeep
	 * 
	 * MainClientThread  is the Class that is responsible for all the client server interaction.
	 * The user can connect with the registration server to register, login and obtain directory server ip for download.
	 * Also, after logging in the registration server the user can connect to the directory server 
	 *
	 */
	
	public class MainClientThread {
	
		//directory server details
		public static String dirServerIp = new String();
		public static int dirServerPort = -1;
		public static Socket dirSocket = null;
		// record of the files and  that this variable has
		public static HashMap<String, HashSet<String>> mClientFiles = new HashMap<String, HashSet<String>>();
		//reg server details
		public static String regServerIp = getConfigValue("regServerIp");
		public static int regServerPort = Integer.parseInt(getConfigValue("regServerPort"));
	
	
	
		public static void main(String[] args) {
	
			try {
				//starts file download thread that would send fileparts to other clients or same host if that case arises.
				startFileSenderThread();			
				Socket regSocket = new Socket(regServerIp, regServerPort);
	
				//Wait for the input client command.
				while(true){
					String input = (new Scanner(System.in)).nextLine().toLowerCase();
					DataInputStream in = new DataInputStream(regSocket.getInputStream());
					DataOutputStream out = new DataOutputStream(regSocket.getOutputStream());
					runCommandFromClient(in, out, input);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		//executes the find a file command in the directory Server
		private static void runFindCommand(DataInputStream in,
				DataOutputStream out, String input) {
			try {
				if(input.split(" ").length != 2) {
					System.out.println("405 Invalid arguments.");
					return;
				}
				dirSocket = new Socket(dirServerIp, dirServerPort);
				DataOutputStream dout = new DataOutputStream(dirSocket.getOutputStream());
				DataInputStream din = new DataInputStream(dirSocket.getInputStream());
				dout.writeUTF(input);			
				String output = din.readUTF();
				System.out.println(output);
				boolean firstLine = true;
				String file = new String();
				HashSet<String> clients = new HashSet<String>();
				for(String l: output.split("\n")) {
					if(!firstLine) {
						String[] params = l.split(" ");
						clients.add(params[0]);
						file = params[1];
					}
					firstLine = false;
				}
	
				System.out.println("FileName = " + file + " ClientsHoldingFile = " + clients.toString());
				mClientFiles.put(file, clients);
	
				dout.close();
				din.close();
				dirSocket.close();
	
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		//runs the serversharecommand
		private static void runServerShareCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length != 1) {
				System.out.println("Server Response: 405 Invalid Arguments");
				return;
			}
	
			try {
				dirSocket = new Socket(dirServerIp, dirServerPort);
				DataOutputStream dout = new DataOutputStream(dirSocket.getOutputStream());
				DataInputStream din = new DataInputStream(dirSocket.getInputStream());
				dout.writeUTF(input);
				String output = din.readUTF();
				System.out.println(output);
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		private static void startFileSenderThread() {
			ServerSocket fastFileSocket;
			try {
				fastFileSocket = new ServerSocket(40000);
				new FileSenderThread(fastFileSocket).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	
		}
	
		private static String getConfigValue(String key) {
	
			try {
				File file = new File("clientconfig.cfg");
				FileReader fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					if(line.startsWith(key)) {
						String[] params = line.split("=");
						if(params.length != 2) {
							continue;
						}
						return params[1];
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			return new String();
		}
	
		private static void runCommandFromClient(DataInputStream in,
				DataOutputStream out, String input) {
			try {
				if(input.startsWith("share ")){
					runShareCommand(in, out, input);
				}else if(input.startsWith("servershare")){
					runServerShareCommand(in, out, input);
				}else if(input.startsWith("fastfiledownload ")){
					runFastFileDownloadCommand(in, out, input);
				} else if(input.startsWith("login ")) {
					runLoginCommand(in, out, input);
				}else if(input.startsWith("find ")){
					runFindCommand(in, out, input);
				}else if(input.startsWith("remove ")){
					runRemoveCommand(in, out, input);
				} else{
					out.writeUTF(input);
					String output = in.readUTF();
					System.out.println(output);
				} 
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	
		private static void runLoginCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			try {
				out.writeUTF(input);
				String response = in.readUTF();
				System.out.println(response);		
			if(response.startsWith("Server Response: 200")){
				response = response.substring(response.indexOf('\n')+1);
				String[] directoryServer = response.split(":");
				dirServerIp = directoryServer[0];
				dirServerPort = Integer.parseInt(directoryServer[1]);	
	}
			} catch (IOException e) {
				//e.printStackTrace();
			}
	
		}
	// This method executes the share command. After this command is executed the file is shared with other clients.
		private static void runShareCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length <= 1) {
				System.out.println("405 Invalid arguments.");
				return;
			}
	
			try {
				dirSocket = new Socket(dirServerIp, dirServerPort);
				DataOutputStream dout = new DataOutputStream(dirSocket.getOutputStream());
				DataInputStream din = new DataInputStream(dirSocket.getInputStream());
				dout.writeUTF(input);
				String output = din.readUTF();
				System.out.println(output);
				dout.close();
				din.close();
				dirSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
		//This function is executed to run the fast filedownload command. 
		private static void runFastFileDownloadCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length != 2) {
				System.out.println("Server Response: 405 Invalid arguments.");
				return;
			}
	
			String[] params = input.split(" ");
			if(!mClientFiles.containsKey(params[1])) {
				System.out.println("No file can be downloaded currently. Please execute a find command first.");
				return;
			}
	
			int filePart = 1;
			int clientSize = mClientFiles.get(params[1]).size();
			ArrayList<FileDownloaderThread> threads = new ArrayList<FileDownloaderThread>();
	
			for(String s: mClientFiles.get(params[1])) {
				String[] prms = s.split(" ");
				Socket socket;
				try {
					socket = new Socket(prms[0].split(":")[0], 40000);
					FileDownloaderThread t = new FileDownloaderThread(socket, clientSize, params[1], filePart++);
					t.start();
					threads.add(t);
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
	
	
			for(FileDownloaderThread t: threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	
	
			//Merging of files starts here. This starts after all the threads finish downloading.
			try {			
				File foo = new File(new StringBuffer().append("./shared_dir/").append("ffd_").append(params[1]).toString() );
				FileOutputStream fos = new FileOutputStream(foo);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				filePart = 1;
				for(int j=1;  j<= clientSize; j++) {
	
					File fin = new File(
							new StringBuffer().append("./shared_dir/").append("part").append(filePart++).toString());
					FileInputStream fis = new FileInputStream(fin);
					BufferedInputStream bis = new BufferedInputStream(fis);
					long length = fin.length();
					byte[] bytes = new byte[(int) length];
	
					while ((bis.read(bytes)) > 0) {
						bos.write(bytes);
					}
	
					fis.close();
					bis.close();							}
				bos.close();
				fos.close();
				//closing all the readers ad writers
	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
		// This function helps us to execute the remove command. On execution of this command the file is removedfrom the shared list.
		private static void runRemoveCommand(DataInputStream in,
				DataOutputStream out, String input) {
	
			if(input.split(" ").length <= 1) {
				System.out.println("Server Response: 405 Invalid arguments.");
				return;
			}
	
			try {
				dirSocket = new Socket(dirServerIp, dirServerPort);
				DataOutputStream dout = new DataOutputStream(dirSocket.getOutputStream());
				DataInputStream din = new DataInputStream(dirSocket.getInputStream());
				dout.writeUTF(input);
				String output = din.readUTF();
				System.out.println(output);
				dirSocket.close();
				dout.close();
				din.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	
	
	
	
	
	}
	
