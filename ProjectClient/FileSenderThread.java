import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class FileSenderThread extends Thread {

	ServerSocket mSocketServer;

	//constructor thats gets the connection.
	public FileSenderThread(ServerSocket sendersocket) {
		mSocketServer = sendersocket;
	}
	public FileSenderThread() {
		//default constructor
	}
	//download Process is started
	private void startDownload(Socket socket, DataInputStream instream, DataOutputStream outstream) {

		String input = new String();
		try {
			input = instream.readUTF();
			String[] params = input.split(" "); 
			if(params.length != 4) {
				outstream.writeUTF("Server Response: 405 Invalid Arguments");
				return;
			}
			//defining the directory of the shared files.
			File file = new File("./shared_dir/" + params[1]);
			long flen = file.length();
			byte[] bytes = new byte[(int) flen];
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			System.out.println("Sending file ...");
			//If the number of clients is equal to one then this piece of code runs. The full file is downloaded from a sigle client
			if(Integer.parseInt(params[2]) == -1 || Integer.parseInt(params[3]) == 1) {
				if (flen > Integer.MAX_VALUE) {
					System.out.println("Server Response: Can't be downloaded due to large size");
					return;
				}
				int count;
				while ((count = bis.read(bytes)) > 0) {
					out.write(bytes, 0, count);					
				}
				fis.close();
				bis.close();
				instream.close();
				outstream.close();
				out.close();
				socket.close();

				return;
			}else{
				//If the number of clients is more than one then this piece of code runs. The full file is downloaded from a more than one clients
				int filePartIndex = Integer.parseInt(params[2]);
				int numberOfClients = Integer.parseInt(params[3]);
				int count = bis.read(bytes);
				int noBytesDownload = count / numberOfClients;
				int startByteIndex = (filePartIndex-1) * noBytesDownload;
				int endByteIndex;
				if(filePartIndex == numberOfClients){
					endByteIndex = count-startByteIndex;
				}else 
					endByteIndex = noBytesDownload;
				if (count  > 0) {
					out.write(bytes, startByteIndex, endByteIndex);					
				}
				fis.close();
				bis.close();
				outstream.flush();
				outstream.close();
				instream.close();
				socket.close();

			}
			System.out.println("File sent: ");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		super.run();
		while(true) {
			Socket conn = null;
			try {
				conn = mSocketServer.accept();
				DataInputStream in = new DataInputStream(conn.getInputStream());
				DataOutputStream out = new DataOutputStream(conn.getOutputStream());				
				startDownload(conn, in, out);
			} catch (IOException e) {

			}
		}

	}




}
