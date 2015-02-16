import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class FileDownloaderThread extends Thread {

	int mPartIndex;
	int mTotalParts;
	String mFileName;
	Socket mSocketClient;


	//Constructor that initializes all the values.
	public FileDownloaderThread(Socket clientSocket, int totalParts, String filename, int filePart) {
		mSocketClient = clientSocket;
		mPartIndex = filePart;
		mTotalParts = totalParts;
		mFileName = filename;
	}

	public FileDownloaderThread() {
		//default constructor
	}

	public void run() {

		super.run();

		try {

			InputStream is = mSocketClient.getInputStream();			
			FileOutputStream fos = new FileOutputStream(new StringBuffer().append("./shared_dir/part")
					.append(mPartIndex).toString());
			BufferedOutputStream bos = new BufferedOutputStream(fos);			
			DataOutputStream outStream = new DataOutputStream(mSocketClient.getOutputStream());
			outStream.writeUTF(new StringBuffer().append("fastfiledownload ").append(mFileName)
					.append(" ").append(mPartIndex).append(" ").append(mTotalParts).toString());						
			System.out.println("Download started part " +mPartIndex + " out of " + mTotalParts +" parts.");
			int bufferSize = 0;
			bufferSize = mSocketClient.getReceiveBufferSize();
			byte[] bytes = new byte[bufferSize];
			int count;
			System.out.println("Starting download part " +mPartIndex +" out of " +mTotalParts +" parts.");
			while ((count = is.read(bytes)) > 0) {
				bos.write(bytes, 0, count);
			}
			System.out.println("Download finished part " +mPartIndex + " out of " + mTotalParts +" parts.");
			is.close();
			outStream.close();
			bos.flush();
			bos.close();			
			mSocketClient.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
