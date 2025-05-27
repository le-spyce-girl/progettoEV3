import java.net.*;
import java.io.IOException;

import lejos.hardware.*;
import lejos.hardware.motor.Motor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;
import java.util.EventListener;

public class server
{
	public static void main(String[] args)
	{
		
		//System.out.println("Hello World!!");
		
		//Motor.B.forward();
		//Motor.B.setSpeed(900);
		//Motor.A.setSpeed(900);
		//Motor.B.forward();
		//Motor.A.forward();
		//Delay.msDelay(1000);
		//Motor.B.stop();
		//Motor.A.stop();
		
		ServerSocket serverSocket;
		try 
		{
			serverSocket = new ServerSocket(1317);
			System.out.println("Listening for clients...");
		    Socket clientSocket = serverSocket.accept();
		    String clientSocketIP = clientSocket.getInetAddress().toString();
		    int clientSocketPort = clientSocket.getPort();
		    System.out.println("[IP: " + clientSocketIP + " ,Port: " + clientSocketPort +"]  " + "Client Connection Successful!");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
        Button.waitForAnyPress();
	}
}

