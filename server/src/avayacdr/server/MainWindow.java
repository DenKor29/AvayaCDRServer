package avayacdr.server;

import avayacdr.application.ApplicationServer;
import avayacdr.application.ApplicationServerListener;
import avayacdr.core.BaseCDRData;
import avayacdr.core.AvayaCDRData;
import avayacdr.core.ConfigurationSettings;
import avayacdr.database.DBServer;
import avayacdr.database.DBServerListener;
import avayacdr.http.CDRHttpServer;
import avayacdr.http.CDRHttpServerListener;
import avayacdr.http.HTTPRequest;
import avayacdr.network.TCPConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class MainWindow extends JFrame  implements ApplicationServerListener, DBServerListener , CDRHttpServerListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }
    private JButton buttonstartServer ;
    private JButton buttonstopServer;
    private JTextArea log;

    private ApplicationServer app;
    private DBServer dbServer;
    private CDRHttpServer cdrHttpServer;

    private ArrayList<BaseCDRData> connectionsCDR = new ArrayList<>();


    private boolean Running = false;

    private int port;
    private int  timeoutacept;
    private int httpport;
    private int  httptimeoutacept;
    private int appserverstart;
    private static final int WIDTH = 350;
    private static final int HEIGHT = 95;

    private MainWindow(){


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        JPanel button_panel = new JPanel();
        button_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Сервер"));
        button_panel.setLayout(new BorderLayout());

        buttonstartServer = new JButton("Старт");
        button_panel.add(buttonstartServer, BorderLayout.NORTH);

        buttonstopServer = new JButton("Стоп");
        button_panel.add(buttonstopServer,BorderLayout.CENTER);


        add(button_panel,BorderLayout.WEST);

        JPanel text_panel = new JPanel();
        text_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Мониторинг"));
        text_panel.setLayout(new BorderLayout());

        log = new JTextArea();
        log.setEnabled(false);
        log.setLineWrap(true);
        JScrollPane scroll= new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setSize(250, 150);
        scroll.setLocation(10,10);
        text_panel.add(scroll,BorderLayout.CENTER);

        add(text_panel,BorderLayout.CENTER);

        enableButtonServer(true);


        setVisible(true);
        pack();
        setIconImage(getImage("icon"));
        setLocationRelativeTo(null);

        ConfigurationSettings configurationSettings = new ConfigurationSettings("application.xml", "avaya");

         String dbname = configurationSettings.get("dbname");
         String nametable = configurationSettings.get("tbname");
         String user = configurationSettings.get("user");
         String password = configurationSettings.get("password");
         String url = "jdbc:mysql://"+ configurationSettings.get("dburl")+"/";
         String urlParam = "?serverTimezone=GMT%2B3";


         port = configurationSettings.getInt("appport",9100);
         timeoutacept = configurationSettings.getInt("apptimeoutacept",30000);
         httpport = configurationSettings.getInt("httpport",8000);
         httptimeoutacept = configurationSettings.getInt("httptimeoutacept",30000);
         appserverstart = configurationSettings.getInt("appserverstart",1);

        dbServer = new DBServer(this,url,urlParam,dbname,nametable,user,password);
        app = new ApplicationServer(this);
        cdrHttpServer = new CDRHttpServer(this,this);


        buttonstartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (!Running) {
                   MainWindow.this.enableButtonServer(false);
                   app.start(port, timeoutacept);
                   cdrHttpServer.start(httpport, httptimeoutacept);
                   Running = true;


               }
            }
        });

        buttonstopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (Running) {
                   Running = false;
                   printMessage(app.getNameServer()+" Interrupt ...");
                   app.interrupt();
               }
            }
        });

            if (appserverstart == 1)  buttonstartServer.doClick();


    }
    private void enableButtonServer(boolean status){

        buttonstartServer.setEnabled(status);
        buttonstopServer.setEnabled(!status);

    }

    @Override
    public void onConnectionServer(ApplicationServer applicationServer) {
        String message = applicationServer.getNameServer() + " Start.";

        System.out.println(message );
        printMessage(message);

    }

    @Override
    public void onConnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {

    }

    @Override
    public void onDisconnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {

    }

    @Override
    public void onMessageString(ApplicationServer applicationServer, TCPConnection tcpConnection,String value) {

        String nameServer = applicationServer.getNameServer();

        if (nameServer.startsWith("Application")) {
            AvayaCDRData baseCDRData = new AvayaCDRData();
            baseCDRData.SetPropertyCDR(dbServer.getNameTable(), value);
            dbServer.AppendTableString(baseCDRData);
        };

        if (nameServer.startsWith("Http")) {
            printMessage(value);
            System.out.println(value );

        };

    }



    @Override
    public void onDisconnection(ApplicationServer applicationServer) {
        String message = applicationServer.getNameServer() + " Stop.";

        System.out.println(message );
        printMessage(message);

        Running = false;
        enableButtonServer(true);
    }

    @Override
    public void onException(ApplicationServer applicationServer, Exception e) {
        System.out.println(app.getNameServer() + " Exeption:" + e);
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

    @Override
    public synchronized void onRecivedCDR(DBServer dbServer, HTTPRequest httpRequest,ArrayList <AvayaCDRData> cdrData) {
            cdrHttpServer.SendResponseConnection(httpRequest,cdrData);

    }

    @Override
    public void onFindDBDateZapros(HTTPRequest httpRequest, LocalDateTime BeginTime, LocalDateTime EndTime, String Key, String Value, int opKey) {
     dbServer.FindDateTimeTable(httpRequest,BeginTime,EndTime,Key,Value,opKey);
    }

    @Override
    public boolean onStatusServer(int server) {

        switch (server) {
            case 0: return Running;
            case 1: return Running;
            case 2: return Running;
        }
        return false;
    }


}
