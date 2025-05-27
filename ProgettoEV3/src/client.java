import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.awt.*;
import java.awt.event.*;

public class client implements KeyListener
{
	
	addKeyListener(this);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
	
    try 
    {
    	Socket socket = new Socket();
		socket.connect(new InetSocketAddress("10.0.1.1", 1317), 1000);
		System.out.println("Connection Successful!");
	} 
    catch (IOException e) 
    {
		e.printStackTrace();
	}
    
    public static void main(String[] args) 
	{
	}
}
