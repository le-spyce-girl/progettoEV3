import javax.swing.*;

import lejos.hardware.Sound;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.awt.Color;

/*
w -> avanti
a -> sinistra
s -> indietro
d -> destra
1 -> marcia 1 1000 500
2 -> marcia 2 500 250
  
 */

public class client extends JFrame implements KeyListener 
{
    private Socket socket;
    private static DataInputStream dis;
    private DataOutputStream dos;
    private Map<Integer, KeyPressThread> keyPressThreads = new HashMap<>();
    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private int currentSpeed = 500; // Velocità predefinita
    private static int velocitaSinistra = 0;
    private static int velocitaDestra = 0;
    private JTextField ipTextField = new JTextField();
    private JTextField portTextField = new JTextField();
    private static int velocita = 0;
    private static JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private static JLabel velocitaLabel = new JLabel();
    private JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));


    public client() 
    {
        // Configura la finestra
        setTitle("EV3 Control");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        
        //crea i vari panel
        
        JPanel ipPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanel = new JPanel(new GridLayout(3, 3, 5, 5));

        // Crea i pulsanti
        JButton buttonForward = new JButton("W (Avanti)");
        JButton buttonLeft = new JButton("A (Sinistra)");
        JButton buttonBackward = new JButton("S (Indietro)");
        JButton buttonRight = new JButton("D (Destra)");
        JButton buttonStop = new JButton("Stop"); // Stop button
        ipTextField = new JTextField("10.0.1.1");
        portTextField = new JTextField("1317");
        JButton connectButton = new JButton("Connetti");
        
        Color nero = new Color(0, 0, 0);
        Color viola = new Color(255, 0, 255);
        Color rosso = new Color(255, 0, 0);
        Color grigio = new Color(50, 50, 50);
        
        buttonForward.setBackground(nero);
        buttonForward.setForeground(viola);
        buttonLeft.setBackground(nero);
        buttonLeft.setForeground(viola);
        buttonBackward.setBackground(nero);
        buttonBackward.setForeground(viola);
        buttonRight.setBackground(nero);
        buttonRight.setForeground(viola);
        buttonStop.setBackground(rosso);
        buttonStop.setForeground(nero);
        connectButton.setBackground(nero);
        connectButton.setForeground(viola);
        
        ipPortPanel.setBackground(grigio);
        ipPortPanel.setForeground(viola);
        controlPanel.setBackground(grigio);
        controlPanel.setForeground(viola);
        
        ipTextField.setBackground(grigio);
        ipTextField.setBackground(viola);
        portTextField.setBackground(grigio);
        portTextField.setBackground(viola);
        
        JLabel ipServer = new JLabel("IP Server:");
        ipServer.setForeground(viola);
        JLabel portaServer = new JLabel("Porta:");
        portaServer.setForeground(viola);
        
        //crea i tasti per l'interfaccia
        ipPortPanel.add(ipServer);
        ipPortPanel.add(ipTextField);
        ipPortPanel.add(portaServer);
        ipPortPanel.add(portTextField);
        ipPortPanel.add(connectButton);
        
        topPanel.add(ipPortPanel);
        
        
        JLabel velocitaLabel = new JLabel("Velocità: ");
        velocitaLabel.setForeground(viola);
        speedPanel.add(velocitaLabel);
        
        velocitaLabel = new JLabel(String.valueOf(velocita));
        velocitaLabel.setForeground(viola);
        
        speedPanel.add(velocitaLabel);
        topPanel.add(speedPanel);
        
        speedPanel.setBackground(grigio);
        speedPanel.setForeground(viola);
        
        add(topPanel, BorderLayout.NORTH);
        
        controlPanel.add(new JLabel()); //cella vuota
        controlPanel.add(buttonForward);
        controlPanel.add(new JLabel()); //cella vuota
        controlPanel. add(buttonLeft);
        controlPanel.add(buttonStop); //bottone per lo stop
        controlPanel.add(buttonRight);
        controlPanel.add(new JLabel()); //cella vuota
        controlPanel.add(buttonBackward);
        controlPanel.add(new JLabel()); //cella vuota
        
        add(controlPanel, BorderLayout.CENTER);
        setBackground(grigio);

        // Configura i pulsanti
        buttonForward.addActionListener(createButtonActionListener(KeyEvent.VK_W));
        buttonLeft.addActionListener(createButtonActionListener(KeyEvent.VK_A));
        buttonBackward.addActionListener(createButtonActionListener(KeyEvent.VK_S));
        buttonRight.addActionListener(createButtonActionListener(KeyEvent.VK_D));
        buttonStop.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                stopAllMovements();
            }
        });

        //crea il listener per i tasti
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        //mostra la finestra con i tasti
        setVisible(true);

        //prende il focus per usare i tasti
        requestFocusInWindow();
        
        connectButton.addActionListener(e -> connectToServer());
    }
    
    private void connectToServer() 
    {
        String ip = ipTextField.getText().trim();
        int port;
        try 
        {
            port = Integer.parseInt(portTextField.getText().trim());
        } 
        catch (NumberFormatException ex) 
        {
            JOptionPane.showMessageDialog(this, "Porta non valida");
            return;
        }

        try 
        {
            if (socket != null && !socket.isClosed()) 
            {
                socket.close();
            }
            socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            JOptionPane.showMessageDialog(this, "Connesso a " + ip + ":" + port);
            System.out.println("Connesso al server " + ip + ":" + port);
            
            velocitaThread.setDaemon(true);
            velocitaThread.start();
            
            salvaVelocitaThread.setDaemon(true);
            salvaVelocitaThread.start();
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, "Errore connessione: " + e.getMessage());
            System.err.println("Errore connessione: " + e.getMessage());
            socket = null;
            dos = null;
        }
        
        try 
    	{
			File myObj = new File("velocita.txt");
			if (myObj.createNewFile()) 
			{
				System.out.println("File created: " + myObj.getName());
			} 
			else 
			{
				System.out.println("File already exists.");
			}
		} 
		catch (IOException e) 
		{
		  e.printStackTrace();
		}
    }

    private ActionListener createButtonActionListener(final int keyCode) 
    {
        return new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                startKeyPressThread(keyCode);
            }
        };
    }

    private void sendSpeedCommand(int command, int speed1, int speed2)
    {
        try 
        {
            dos.writeInt(command);
            dos.writeInt(speed1);
            dos.writeInt(speed2);
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_1) 
        {
            //modalita normale
            currentSpeed = 500;
            //System.out.println("Velocità a: " + currentSpeed);
        } 
        else if (keyCode == KeyEvent.VK_2) 
        {
            //modalioota sport
            currentSpeed = 750;
            //System.out.println("Velocità a: " + currentSpeed);
        } 
        else if (keyCode == KeyEvent.VK_3) 
        {
            //modalioota sport
            currentSpeed = 1100;
            //System.out.println("Velocità a: " + currentSpeed);
        } 
        else 
        {
            startKeyPressThread(keyCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) 
    {
        int keyCode = e.getKeyCode();
        if (keyCode != KeyEvent.VK_1 && keyCode != KeyEvent.VK_2 && keyCode != KeyEvent.VK_3) 
        {
            stopKeyPressThread(keyCode);
        }
    }

    private void startKeyPressThread(int key) 
    {
        if (!keyPressThreads.containsKey(key)) 
        {
            KeyPressThread thread = new KeyPressThread(key);
            keyPressThreads.put(key, thread);
            thread.start();
        }
    }

    private void stopKeyPressThread(int key) 
    {
        KeyPressThread thread = keyPressThreads.remove(key);
        if (thread != null) 
        {
            thread.stopRunning();
            sendSpeedCommand(0, 0, 0); //ferma il commando quando si alza il tasto
        }
    }

    @Override
    public void keyTyped(KeyEvent e) 
    {}

    private void stopAllMovements() 
    {
        for (KeyPressThread thread : keyPressThreads.values()) 
        {
            thread.stopRunning();
        }
        keyPressThreads.clear();
        sendSpeedCommand(0, 0, 0); //ferma tutti i comandi
    }

    private class KeyPressThread extends Thread 
    {
        private final int key;
        private boolean running = true;

        KeyPressThread(int key) 
        {
            this.key = key;
        }

        @Override
        public void run() 
        {
            while (running) 
            {
            	boolean avanti;
            	boolean sinistra;
            	boolean dietro;
            	boolean destra;
            	if(key == KeyEvent.VK_W) 
            	{
            		avanti = true;
            	}
            	else
            	{
            		avanti = false;
            	}
            	if(key == KeyEvent.VK_A) 
            	{
            		sinistra = true;
            	}
            	else
            	{
            		sinistra = false;
            	}
            	if(key == KeyEvent.VK_S) 
            	{
            		dietro = true;
            	}
            	else
            	{
            		dietro = false;
            	}
            	if(key == KeyEvent.VK_D) 
            	{
            		destra = true;
            	}
            	else
            	{
            		destra = false;
            	}
            	
            	if(avanti)//avanti
            	{
            		 sendSpeedCommand(1, currentSpeed, currentSpeed); //destra , sinistra
                     velocitaSinistra = currentSpeed;
                     velocitaDestra = currentSpeed;
            	}
            	else if(sinistra) // Sinistra
            	{
            		sendSpeedCommand(3, currentSpeed, currentSpeed/2);//destra , sinistra
                    velocitaSinistra = currentSpeed/2;
                    velocitaDestra = currentSpeed;
            	}
            	else if(dietro)//indietro
            	{
            		sendSpeedCommand(2, currentSpeed, currentSpeed);//destra , sinistra
                    velocitaSinistra = currentSpeed;
                    velocitaDestra = currentSpeed;
            	}
            	else if(destra)//  Destra
            	{
            		sendSpeedCommand(4, currentSpeed/2, currentSpeed);//destra , sinistra
                    velocitaSinistra = currentSpeed;
                    velocitaDestra = currentSpeed/2;
            	}
            	
                try 
                {
                    Thread.sleep(10); //mantiene la velocita
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }
        }

        void stopRunning() 
        {
            running = false;
        }
    }

    static Thread salvaVelocitaThread = new Thread(new Runnable() {
		@Override
		public void run() 
		{
			FileWriter myWriter = null;
        	try 
			{
				myWriter = new FileWriter("velocita.txt");
			} 
			catch (IOException e2) 
			{
				e2.printStackTrace();
			}
			while(true)
			{
				try 
            	{
            		LocalDateTime ora = LocalDateTime.now();
            	    myWriter.write(velocita + "::::" + ora +  ";\n");
            	    myWriter.flush();
            	    //System.out.println("scrittura avvenuta");
					
					try 
					{
						Thread.sleep(250);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				} 
                catch (IOException e1) 
                {
					e1.printStackTrace();
				}
			}
		}
    });
    
    static Thread velocitaThread = new Thread(new Runnable() {
		@Override
		public void run() 
		{
			while(true)
			{
				try 
                {
					velocitaSinistra = dis.readInt();
					velocitaDestra = dis.readInt();
					
					//System.out.println(velocitaSinistra);
					//System.out.println(velocitaDestra);
					
					velocita = ( velocitaSinistra + velocitaDestra ) / 2;
					
					//System.out.println(velocita);
					velocitaLabel.setText(String.valueOf(velocita));
					speedPanel.repaint();
					
					try 
					{
						Thread.sleep(1000);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				} 
                catch (IOException e1) 
                {
					e1.printStackTrace();
				}
			}
		}
    });
    
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                final client client = new client();
                client.addWindowFocusListener(new WindowAdapter() 
                {
                    public void windowGainedFocus(WindowEvent e) 
                    {
                        client.requestFocusInWindow();
                    }
                });
            }
        });
    }
}