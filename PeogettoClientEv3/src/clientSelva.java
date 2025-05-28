import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class clientSelva extends JFrame {

    private JTextField ipTextField;
    private JTextField portTextField;
    private JLabel speedLabel;
    private int speed = 0; // velocità letta dal robot

    private Socket socket;
    private OutputStream outStream;

    public clientSelva() {
        setTitle("Telecomando EV3");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 450);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Pannello superiore con IP, porta e bottone affiancati
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel ipPortPanel = new JPanel();
        ipPortPanel.setLayout(new BoxLayout(ipPortPanel, BoxLayout.X_AXIS));
        ipPortPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        ipPortPanel.add(new JLabel("IP Server:"));
        ipPortPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        ipTextField = new JTextField("192.168.0.1", 15);
        ipPortPanel.add(ipTextField);

        ipPortPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        ipPortPanel.add(new JLabel("Porta:"));
        ipPortPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        portTextField = new JTextField("12345", 6);
        ipPortPanel.add(portTextField);

        ipPortPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        JButton connectButton = new JButton("Connetti");
        ipPortPanel.add(connectButton);

        topPanel.add(ipPortPanel);

        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        speedPanel.add(new JLabel("Velocità: "));
        speedLabel = new JLabel(String.valueOf(speed));
        speedPanel.add(speedLabel);
        topPanel.add(speedPanel);

        add(topPanel, BorderLayout.NORTH);

        // Pannello controllo movimento
        JPanel controlPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        JButton btnUp = new JButton("▲ Avanti");
        JButton btnDown = new JButton("▼ Indietro");
        JButton btnLeft = new JButton("◄ Sinistra");
        JButton btnRight = new JButton("► Destra");
        JButton btnStop = new JButton("■ Stop");

        controlPanel.add(new JLabel());
        controlPanel.add(btnUp);
        controlPanel.add(new JLabel());
        controlPanel.add(btnLeft);
        controlPanel.add(btnStop);
        controlPanel.add(btnRight);
        controlPanel.add(new JLabel());
        controlPanel.add(btnDown);
        controlPanel.add(new JLabel());

        add(controlPanel, BorderLayout.CENTER);

        // Azioni pulsanti movimento
        btnUp.addActionListener(e -> sendCommand('F'));
        btnDown.addActionListener(e -> sendCommand('B'));
        btnLeft.addActionListener(e -> sendCommand('L'));
        btnRight.addActionListener(e -> sendCommand('R'));
        btnStop.addActionListener(e -> sendCommand('S'));

        // Listener tastiera
        setupKeyListener();

        // Bottone connetti
        connectButton.addActionListener(e -> connectToServer());
    }

    private void connectToServer() {
        String ip = ipTextField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portTextField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Porta non valida");
            return;
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new Socket(ip, port);
            outStream = socket.getOutputStream();
            JOptionPane.showMessageDialog(this, "Connesso a " + ip + ":" + port);
            System.out.println("Connesso al server " + ip + ":" + port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Errore connessione: " + e.getMessage());
            System.err.println("Errore connessione: " + e.getMessage());
            socket = null;
            outStream = null;
        }
    }

    private void sendCommand(char command) {
        if (outStream == null) {
            JOptionPane.showMessageDialog(this, "Non connesso al server.");
            System.err.println("Non connesso al server.");
            return;
        }
        try {
            outStream.write(command);
            outStream.flush();
            System.out.println("Inviato comando: " + command);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Errore invio comando: " + e.getMessage());
            System.err.println("Errore invio comando: " + e.getMessage());
        }
    }

    private void setupKeyListener() {
        this.setFocusable(true);
        this.requestFocusInWindow();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char cmd;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> cmd = 'F';
                    case KeyEvent.VK_DOWN -> cmd = 'B';
                    case KeyEvent.VK_LEFT -> cmd = 'L';
                    case KeyEvent.VK_RIGHT -> cmd = 'R';
                    case KeyEvent.VK_P -> {
                        System.out.println("Pausa / Riprendi comando inviato");
                        return;
                    }
                    case KeyEvent.VK_R -> {
                        System.out.println("Restart comando inviato");
                        return;
                    }
                    default -> {
                        return;
                    }
                }
                sendCommand(cmd);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        	clientSelva remote = new clientSelva();
            remote.setVisible(true);
        });
    }
}