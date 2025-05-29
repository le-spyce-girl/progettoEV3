import lejos.hardware.motor.Motor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.utility.Delay;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public class server 
{
	private static DataOutputStream dos;
	private static Boolean ultima_direzione = true;
    private static EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.A);
    private static EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.B);
    private static EV3UltrasonicSensor sensore = new EV3UltrasonicSensor(SensorPort.S2); 
    private static SampleProvider distanza = sensore.getDistanceMode();
    final static float[] sample = new float[distanza.sampleSize()];
    private static float paura = 0.2f;
    		
    public static void main(String[] args) 
    {
        EV3 ev3 = LocalEV3.get();
        TextLCD lcd = ev3.getTextLCD();
       
        try (ServerSocket serverSocket = new ServerSocket(1317)) 
        {
            lcd.drawString("Waiting for client...", 0, 4);
            Socket clientSocket = serverSocket.accept();
            lcd.drawString("Client connected!", 0, 5);
            dos = new DataOutputStream(clientSocket.getOutputStream());
            
            Thread bipThread = new Thread(new Runnable() {
        		@Override
        		public void run() 
        		{
        			while(true)
        			{
        				distanza.fetchSample(sample, 0);
        				float misura = sample[0];
        				if(misura < paura)
        				{
        					Sound.beep();
        				}
        				try
        				{
        					Thread.sleep(1000);
        				}
        				catch(InterruptedException e)
        				{
        					break;
        				}
        			}
        		}
            });
            
            bipThread.setDaemon(true);
            bipThread.start();
           
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            while (true) 
            {
                int command = dis.readInt();
                int speed1 = dis.readInt(); //lettura velocita motore di destra
                int speed2 = dis.readInt(); //lettura velocita motore di sinistra
                if (command == -1) 
                {
                    break;
                }
                executeCommand(command, speed1, speed2);
            }
        } 
        catch (IOException e) 
        {
            lcd.drawString("Error: " + e.getMessage(), 0, 6);
        }

        leftMotor.close();
        rightMotor.close();
        lcd.clear();
        lcd.drawString("Server stopped", 0, 4);
    }
    
    private static void executeCommand(int command, int speed1, int speed2) 
    {
    	try 
		{
			dos.writeInt(leftMotor.getSpeed());
			dos.writeInt(rightMotor.getSpeed());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
        switch (command) {
            case 1: // Avanti
                leftMotor.setSpeed(speed2);
                rightMotor.setSpeed(speed1);
                leftMotor.forward();
                rightMotor.forward();
                ultima_direzione = true;
                break;
            case 2: // Indietro
                leftMotor.setSpeed(speed2);
                rightMotor.setSpeed(speed1);
                leftMotor.backward();
                rightMotor.backward();
                ultima_direzione = false;
                break;
            case 3: //Sinistra
            	leftMotor.setSpeed(speed2);
	            rightMotor.setSpeed(speed1);
                if(ultima_direzione)
                {
                	rightMotor.forward();
                    leftMotor.forward();
                }
                else
                {
                	rightMotor.backward();
                    leftMotor.backward();
                }
                break;
            case 4: // Destra
                leftMotor.setSpeed(speed2);
                rightMotor.setSpeed(speed1);
                if(ultima_direzione)
                {
                	rightMotor.forward();
                    leftMotor.forward();
                }
                else
                {
                	rightMotor.backward();
                    leftMotor.backward();
                }
                break;
            case 0: // Stop
                leftMotor.stop(true);
                rightMotor.stop(true);
                break;
        }
    }
}