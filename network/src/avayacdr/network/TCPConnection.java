package avayacdr.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;
    private final TCPConnectionListener eventListener;
    private final Thread rxThread;
    private final BufferedReader in;
    private final OutputStream out;


    public TCPConnection(TCPConnectionListener eventListener,String ipAddr,int port) throws IOException {
    this(eventListener,new Socket(ipAddr,port));
    }

    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.socket = socket;
        this.eventListener = eventListener;

        in =  new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("windows-1251")));
        //out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        out = socket.getOutputStream();


        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                     eventListener.onConnectionReady(TCPConnection.this);
                     while (!rxThread.isInterrupted()) {
                         String value = in.readLine();
                         if (value != null )eventListener.onReceiveString(TCPConnection.this, value);
                     }

                    } catch (IOException e) {

                        eventListener.onException(TCPConnection.this, e);
                    } finally {
                    eventListener.onDisconnection(TCPConnection.this);
                    }
            }
        });
        rxThread.start();
    }
    public  synchronized void sendString(String value) {

        try {
            sendBytes(value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            eventListener.onException(TCPConnection.this,e);
        }


    }
    public  synchronized void sendBytes(byte[] value) {

        if (value == null) return;

        try {
            out.write(value);
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this,e);
            disconnected();
        }

    }
    public  synchronized void disconnected() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this,e);
        }
    }

    @Override
    public String toString() {
        return "TCPConnection:"+socket.getInetAddress()+":"+socket.getPort();
    }
}
