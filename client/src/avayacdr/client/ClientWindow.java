package avayacdr.client;

import avayacdr.network.TCPConnection;
import avayacdr.network.TCPConnectionListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IPP_ADDR = "127.0.0.1";
    private static final int PORT = 9000;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new ClientWindow();
        }
    });
    }
    private final JTextArea log = new JTextArea();
    private final JTextField nickname = new JTextField("denkor");
    private final JTextField textinput = new JTextField();

    private TCPConnection tcpConnection;

    private ClientWindow(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        log.setEnabled(false);
        log.setLineWrap(true);
        JScrollPane scroll= new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setSize(250, 150);
        scroll.setLocation(10,10);
        add(scroll,BorderLayout.CENTER);

        add(nickname,BorderLayout.NORTH);

        textinput.addActionListener(this);
        add(textinput,BorderLayout.SOUTH);

        setVisible(true);
        pack();
        setIconImage(getImage("icon"));
        setLocationRelativeTo(null);

        try {
            tcpConnection = new TCPConnection(this,IPP_ADDR,PORT);
        } catch (IOException e) {
            printMessage("Client Connected Exeption:"+e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    String value = textinput.getText();
    String nick = nickname.getText();
    if (value.equals("") || nick.equals("")) return;

    textinput.setText(null);
    tcpConnection.sendString(nick+": "+value);
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Client Connected Ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnection(TCPConnection tcpConnection) {
        printMessage("Connected Close...");

    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Client Connected Exeption:"+e);
    }

    private synchronized  void printMessage(String value){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(value+"\r\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
    private Image getImage (String name){
        String filename = "img/" + name + ".png";
        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(filename)));
        return icon.getImage();

    }
}
