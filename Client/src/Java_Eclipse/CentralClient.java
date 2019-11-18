package Java_Eclipse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class CentralClient {
	public static void main(String[] args)
	{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket ControlSocket = null;
		DataOutputStream outToServer = null;
		DataInputStream inFromServer = null;
		try {
			int port1 = 12345;
			String ip = "192.168.0.7";
			ControlSocket = new Socket(ip, port1);
			outToServer = new DataOutputStream(ControlSocket.getOutputStream());
			inFromServer = new DataInputStream(new BufferedInputStream(ControlSocket.getInputStream()));
			
			System.out.println("You are connected to the raspberry pi!");
			System.out.println(ip);
			
			ControlSocket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
