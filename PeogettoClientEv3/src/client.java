import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

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
    private DataInputStream dis;
    private DataOutputStream dos;
    private Map<Integer, KeyPressThread> keyPressThreads = new HashMap<>();
    private int currentSpeed = 500; // Velocità predefinita
    private int velocitaSinistra = 0;
    private int velocitaDestra = 0;
    private JTextField ipTextField = new JTextField();
    private JTextField portTextField = new JTextField();
    private JLabel speedLabelSinistra;
    private JLabel speedLabelDestra;

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
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel ipPortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel controlPanel = new JPanel(new GridLayout(3, 3, 5, 5));

        // Crea i pulsanti
        JButton buttonForward = new JButton("W (Avanti)");
        JButton buttonLeft = new JButton("A (Sinistra)");
        JButton buttonBackward = new JButton("S (Indietro)");
        JButton buttonRight = new JButton("D (Destra)");
        JButton buttonStop = new JButton("Stop"); // Stop button
        JLabel velocitaSinistra = new JLabel();
        JLabel velocitaDestra = new JLabel();
        ipTextField = new JTextField("10.0.1.1");
        portTextField = new JTextField("1317");
        JButton connectButton = new JButton("Connetti");
        
        //crea i tasti per l'interfaccia
        ipPortPanel.add(new JLabel("IP Server:"));
        ipPortPanel.add(ipTextField);
        ipPortPanel.add(new JLabel("Porta:"));
        ipPortPanel.add(portTextField);
        ipPortPanel.add(connectButton);
        
        topPanel.add(ipPortPanel);
        
        speedPanel.add(new JLabel("Velocità: "));
        speedLabelSinistra = new JLabel(String.valueOf(velocitaSinistra));
        speedPanel.add(speedLabelSinistra);
        speedLabelDestra = new JLabel(String.valueOf(velocitaDestra));
        speedPanel.add(speedLabelDestra);
        topPanel.add(speedPanel);
        
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
    
    private void connectToServer() {
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
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, "Errore connessione: " + e.getMessage());
            System.err.println("Errore connessione: " + e.getMessage());
            socket = null;
            dos = null;
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
            currentSpeed = 1000;
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
        if (keyCode != KeyEvent.VK_1 && keyCode != KeyEvent.VK_2) 
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
            	try 
            	{
					velocitaSinistra = dis.readInt();
					velocitaDestra = dis.readInt();
				} 
            	catch (IOException e1) 
            	{
					e1.printStackTrace();
				}
            	//TODO modifica val della velocita nella interfaccia
                switch (key) //switch invio comando al server
                {
                    case KeyEvent.VK_W:
                        sendSpeedCommand(1, currentSpeed, currentSpeed); //destra , sinistra
                        velocitaSinistra = currentSpeed;
                        velocitaDestra = currentSpeed;
                        //System.out.println("motore sx : " + velocitaSinistra + "\nmotore dx : " + velocitaDestra);
                        break;
                    case KeyEvent.VK_A:
                        sendSpeedCommand(3, currentSpeed, currentSpeed/2);//destra , sinistra
                        velocitaSinistra = currentSpeed/2;
                        velocitaDestra = currentSpeed;
                        break;
                    case KeyEvent.VK_S:
                        sendSpeedCommand(2, currentSpeed, currentSpeed);//destra , sinistra
                        velocitaSinistra = currentSpeed;
                        velocitaDestra = currentSpeed;
                        break;
                    case KeyEvent.VK_D:
                        sendSpeedCommand(4, currentSpeed/2, currentSpeed);//destra , sinistra
                        velocitaSinistra = currentSpeed;
                        velocitaDestra = currentSpeed/2;
                        break;
                }
                try 
                {
                    Thread.sleep(100); //mantiene la velocita
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