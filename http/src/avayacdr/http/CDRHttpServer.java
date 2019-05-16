package avayacdr.http;

import avayacdr.application.ApplicationServer;
import avayacdr.application.ApplicationServerListener;
import avayacdr.core.AvayCDRData;
import avayacdr.core.BaseCDRData;
import avayacdr.network.TCPConnection;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class CDRHttpServer  implements ApplicationServerListener,HTTPConnectionListener{

    private ApplicationServer app;
    private ApplicationServerListener event;
    private CDRHttpServerListener eventcdr;
    private ArrayList<HTTPConnection> connectionsRequest = new ArrayList<>();

    private int day;
    private int mounth;
    private int year;


    public CDRHttpServer(ApplicationServerListener eventListener,CDRHttpServerListener httpServerListener) {
       this.event = eventListener;
       this.eventcdr = httpServerListener;
        app = new ApplicationServer(this,"Http Server");

    }

    public void interrupt(){
        app.interrupt();
    }
    public void start(int port,int timeoutacept) {
        app.start(port,timeoutacept);
    }

    private int GetIndexTCPConnection(TCPConnection tcpConnection)
    {
        int index = -1;
        int cnt = connectionsRequest.size();
        for (int i = 0; i < cnt; i++) {
            if (connectionsRequest.get(i).getTcpConnection() == tcpConnection) {
                index = i;
                break;
            };
        }
        return index;
    }
    @Override
    public synchronized void onConnectionServer(ApplicationServer applicationServer) {
        event.onConnectionServer(applicationServer);
    }

    @Override
    public synchronized void onConnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {
        connectionsRequest.add(new HTTPConnection(this,tcpConnection));
        event.onConnectionReady(applicationServer,tcpConnection);
    }

    @Override
    public synchronized void onDisconnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection) {
        event.onDisconnectionReady(applicationServer,tcpConnection);

        int index = GetIndexTCPConnection(tcpConnection);
        if (index != -1) connectionsRequest.remove(index);
    }

    @Override
    public synchronized void onDisconnection(ApplicationServer applicationServer) {
     event.onDisconnection(applicationServer);

    }

    @Override
    public synchronized void onException(ApplicationServer applicationServer, Exception e) {
    event.onException(applicationServer,e);
    }

    @Override
    public synchronized void onMessageString(ApplicationServer applicationServer, TCPConnection tcpConnection,String value) {

        int index = GetIndexTCPConnection(tcpConnection);
        if (index != -1) connectionsRequest.get(index).RequestAppendString(value);

    }

    @Override
    public synchronized void onReciveRequest(HTTPConnection httpConnection) {

        System.out.println(httpConnection.GetRequest()+"\r\n");
        HTTPRequest httpRequest = new HTTPRequest(httpConnection) ;
        int day = httpRequest.getDay();
        int month = httpRequest.getMonth();
        int year = httpRequest.getYear();

        LocalDateTime EndTime = LocalDateTime.now();

        if ((day != 0) && (month !=0) && (year !=0)) EndTime=LocalDateTime.of(year,month,day,0,0).minusDays(-1);



        this.eventcdr.onFindDBDateZapros(httpRequest,EndTime.minusDays(1),EndTime.minusSeconds(1));


    }

    public synchronized void SendResponseConnection(HTTPRequest httpRequest,ArrayList <AvayCDRData> cdrData){

        HTTPConnection httpConnection = httpRequest.getConnection();
        String path = httpRequest.GetPatch() ;
        String filetext = GetCDRResponse(path, cdrData);

        String codeResponse = "200 OK";

        if (filetext.isEmpty()){

            codeResponse = "404 Not Found";
            filetext = "File " + path +" not found!!!\n";

        }

        int count = 0;

        try {
            count = filetext.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String header = GetDefaultHeader(httpRequest);
        header = header + GetFileHeader(count);

        String response = GetStatusResponse(codeResponse)
                + header+ "\n\n"
                + filetext ;

        httpConnection.sendString(response);
        System.out.println(response);
        cdrData.clear();
        cdrData = null;



    }

    private String GetLocalTimeHttpServer(LocalDateTime dateTime){

        String result="";
        try {
            result = dateTime.format(DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss",Locale.US))+" GMT+3";
        } catch (DateTimeParseException e) {
            System.out.println("HTTPServer DateTime Exeption: " + e);
        }
        return result;
    }
    private String GetLocalTimeHttpClient(LocalDateTime dateTime){

        String result="";
        try {
            result = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        } catch (DateTimeParseException e) {
            System.out.println("HTTPServer DateTime Exeption: " + e);
        }
        return result;
    }

    private String GetDefaultHeader(HTTPRequest httpRequest){

        String mimetype ="text/plain";
        String filename = httpRequest.GetPatch();
        if (filename.endsWith(".html")) mimetype = "text/html; charset=utf-8";
        if (filename.endsWith(".js")) mimetype = "text/javascript; charset=utf-8";
        if (filename.equals("www"+File.separator)) mimetype = "text/html; charset=utf-8";

        String response = "Date: " + GetLocalTimeHttpServer(LocalDateTime.now()) + "\n"
                    + "Content-Type: "+ mimetype+"\n"
                    + "Connection: close\n"
                    + "Server: HTTP Server Avaya S8500\n";
                    //+ "Pragma: no-cache\n";
        return response;
    }

    private String GetFileHeader(int count)
    {
        String     response = "Last-Modified: " + GetLocalTimeHttpServer(LocalDateTime.now()) + "\n"
                    +  "Content-Length: " + count + "\n";
        return response;
    }

    private String GetStatusResponse(String value) {
        String response = "HTTP/1.1 " + value + "\n";
        return response;
    }



    private String GetFileResponse(String file) {

        BufferedReader reader = null;


        try {
            reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file), "UTF8"));
        } catch (UnsupportedEncodingException e) {
           event.onException(app,e);
            return "";
        } catch (FileNotFoundException e) {
            event.onException(app,e);
            return "";
        }


        ;

        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = "\n";

        try {
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) stringBuilder.append(line+ls);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            event.onException(app, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                event.onException(app, e);

            }
        }

        return "";
    }

    private String GetCDRResponse(String path, ArrayList <AvayCDRData> cdrData) {

        String filename=path;

        if (filename.equals("www"+File.separator)) filename += "index.html";
        System.out.println("Filename = " +filename);

        String Response = GetFileResponse(filename);

        if (filename.endsWith("index.html")) {
        StringBuilder stringBuilder = new StringBuilder();

        int cnt = cdrData.size();
        for (int i = 0; i < cnt; i++) {
            stringBuilder.append(" <tr> ");
            stringBuilder.append("<th>"+(i+1)+"</th> ");
            stringBuilder.append("<th>"+GetLocalTimeHttpClient(cdrData.get(i).date)+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).calledNumber+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).callingNumber+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).duration*6+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).cond_code+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_dial+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_used+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_trk_code+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).acct_code+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).auth_code+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).frl+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).ixc_code+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_crt_id+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).out_crt_id+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).feat_flag+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_return+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).line_feed+"</th> ");
            stringBuilder.append(" </tr> ");
        };
            Response = Response.replace("$BaseCDRList$",stringBuilder.toString());

            int day;
            int month;
            int year;

            LocalDateTime valuetime = LocalDateTime.now();

            if (cnt > 0) valuetime = cdrData.get(0).date;

                day = valuetime.getDayOfMonth();
                month = valuetime.getMonthValue();
                year = valuetime.getYear();


            Response = Response.replace("$finddate.day$" ,""+day);
            Response = Response.replace("$finddate.month$",""+month);
            Response = Response.replace("$finddate.year$",""+year);
        }

        return Response;
    }


}
