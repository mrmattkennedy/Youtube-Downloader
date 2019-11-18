package Java_Eclipse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class CentralClient {
	public static void main(String[] args)
	{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket ControlSocket = null;
		DataOutputStream outToServer = null;
		DataInputStream inFromServer = null;
		try {
			int port1 = 12345;
			//String ip = "192.168.0.7";
			String ip = "localhost";
			ControlSocket = new Socket(ip, port1);
			outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
			
			System.out.println("You are connected to the raspberry pi!");
			System.out.println(ip);
			
			// Set port equal to port + 2, as per project guidelines.
			int port = port1 + 2;
			// Send the command to the server.
			outToServer.writeBytes(port + " test + " + '\n');
			// Read status code with readInt(), which blocks until all 3 bytes read.
			//Need only 3 bytes, as the status is 3 characters long
			byte[] statusBytes = new byte[3];
			inFromServer.read(statusBytes);
			String temp = new String(statusBytes, "UTF-8");
	        int statusCode = Integer.parseInt(new String(statusBytes, "UTF-8"));
			
			// If status code is 550 (error).
			if (statusCode == 550) {
				System.out.println("Did not work.");
			} 
			else if (statusCode == 200)
			{
				DataInputStream inData = new DataInputStream(
						new BufferedInputStream(ControlSocket.getInputStream()));
				byte[] tempSizeBuffer = new byte[10];
				inFromServer.read(tempSizeBuffer);
				String tempSize = new String(tempSizeBuffer, "UTF-8").trim();
		        int size = Integer.parseInt(tempSize);
				
		        byte[] dataIn = new byte[size];
				// Reads bytes from the inData stream and places them in dataIn byte array.
				inData.readFully(dataIn);

				// Use FileOutputStream to write byes to new file.
				String filePath = System.getProperty("user.dir") + "/" + "test.mp4";
				try (FileOutputStream fos = new FileOutputStream(filePath)) {
					fos.write(dataIn);
				}
				
			}
	
			ControlSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
