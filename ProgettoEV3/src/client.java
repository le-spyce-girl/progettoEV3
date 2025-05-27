import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/*
w -> avanti
a -> sinistra
s -> indietro
d -> destra
1 -> marcia 1
2 -> marcia 2 
  
 */

public class client extends JFrame implements KeyListener 
{

    private Socket socket;
    private DataOutputStream dos;
    private Map<Integer, KeyPressThread> keyPressThreads = new HashMap<>();
    private int currentSpeed = 1100; // Velocità predefinita
    private int speedturn = 500;

    public client() 
    {
        // Configura la finestra
        setTitle("EV3 Control");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 3));

        // Crea i pulsanti
        JButton btnForward = new JButton("W (Avanti)");
        JButton btnLeft = new JButton("A (Sinistra)");
        JButton btnBackward = new JButton("S (Indietro)");
        JButton btnRight = new JButton("D (Destra)");
        JButton btnStop = new JButton("Stop"); // Stop button

        //TODO inserire velocita nell'interfaccia per il movimmento
     	//TODO inserire posizione nell'interfaccia per il movimmento
        
        //crea i tasti per l'interfaccia
        add(new JLabel()); //cella vuota
        add(btnForward);
        add(new JLabel()); //cella vuota
        add(btnLeft);
        add(btnStop); // Stop button added
        add(btnRight);
        add(new JLabel()); //cella vuota
        add(btnBackward);
        add(new JLabel()); //cella vuota
        add(new JLabel());
        add(new JLabel());
        add(new JLabel()); 

        // Configura i pulsanti
        btnForward.addActionListener(createButtonActionListener(KeyEvent.VK_W));
        btnLeft.addActionListener(createButtonActionListener(KeyEvent.VK_A));
        btnBackward.addActionListener(createButtonActionListener(KeyEvent.VK_S));
        btnRight.addActionListener(createButtonActionListener(KeyEvent.VK_D));
        btnStop.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                stopAllMovements();
            }
        });

        //connessione al server
        try 
        {
            socket = new Socket("10.0.1.1", 1317); //connessione tcp 
            dos = new DataOutputStream(socket.getOutputStream());
        } 
        catch (IOException ex) 
        {
            JOptionPane.showMessageDialog(this, "Errore di connessione: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }

        //crea il listener per i tasti
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        //mostra la finestra con i tasti
        setVisible(true);

        //prende il focus per usare i tasti
        requestFocusInWindow();
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
            currentSpeed = 1100;
            speedturn = 700;
            System.out.println("Velocità a: " + currentSpeed);
        } 
        else if (keyCode == KeyEvent.VK_2) 
        {
            //modalioota sport
            currentSpeed = 5000;
            speedturn = 1100;
            System.out.println("Velocità a: " + currentSpeed);
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
                switch (key) //switch invio comando al server
                {
                    case KeyEvent.VK_W:
                        sendSpeedCommand(1, currentSpeed, currentSpeed);
                        break;
                    case KeyEvent.VK_A:
                        sendSpeedCommand(3, currentSpeed, speedturn);
                        break;
                    case KeyEvent.VK_S:
                        sendSpeedCommand(2, currentSpeed, currentSpeed);
                        break;
                    case KeyEvent.VK_D:
                        sendSpeedCommand(4, speedturn, currentSpeed);
                        break;
                }
                try 
                {
                    Thread.sleep(10); //mantiene la velocita
                } catch (InterruptedException e) 
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