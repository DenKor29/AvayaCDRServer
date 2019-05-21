package avayacdr.http;

import avayacdr.application.ApplicationServer;
import avayacdr.application.ApplicationServerListener;
import avayacdr.core.AvayaCDRData;
import avayacdr.network.TCPConnection;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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

        HTTPRequest httpRequest = new HTTPRequest(httpConnection) ;
        String path = httpRequest.GetPatch() ;



        if (path.endsWith("finddate.html"))
        {
            int day = httpRequest.getDay();
            int month = httpRequest.getMonth();
            int year = httpRequest.getYear();
            String key = httpRequest.getKey().trim();
            String value = httpRequest.getValue().trim();
            LocalDateTime EndTime = LocalDateTime.now();
            if ((day != 0) && (month !=0) && (year !=0)) EndTime=LocalDateTime.of(year,month,day,0,0).minusDays(-1);
            eventcdr.onFindDBDateZapros(httpRequest,EndTime.minusDays(1),EndTime.minusSeconds(1),key,value);

        } else SendResponseConnection(httpRequest,null);




    }

    public synchronized void SendResponseConnection(HTTPRequest httpRequest,ArrayList <AvayaCDRData> cdrData){

        HTTPConnection httpConnection = httpRequest.getConnection();
        String ResponseTextBody = "";
        byte[] ResponseBody = null;
        int countBody = 0;


        String path = httpRequest.GetPatch() ;
        if (path.equals("www"+File.separator )) path += "index.html";

        File file = new File(path);
        HTTPResponse httpResponse = new HTTPResponse();

        if (path.endsWith(".ico")|| path.endsWith(".png")) {
            ResponseBody = GetFileResponse(path);
            if (ResponseBody != null) {
                countBody = ResponseBody.length;
            } else
            {
                ResponseTextBody  = "File " + path +" not found!!!\n";
                httpResponse.SetResponseCode("404 Not Found");
                countBody = GetBytesResponse(ResponseTextBody);
            }
        }
        else    {

            ResponseTextBody = GetFileTextResponse(path);


        if (ResponseTextBody == null || ResponseTextBody.isEmpty()){

            ResponseTextBody  = "File " + path +" not found!!!\n";
            httpResponse.SetResponseCode("404 Not Found");
            countBody = GetBytesResponse(ResponseTextBody);

        } else {

            if (path.endsWith(".html"))
            {


                if (cdrData != null) {
                    ResponseTextBody = ResponseTextBody.replace("$COUNTCDR$", "" + cdrData.size());
                    ResponseTextBody = ResponseTextBody.replace("$BaseCDRList$", GetCDRResponse(cdrData));
                }

                    ResponseTextBody = ResponseTextBody.replace("$finddate.list$" ,""
                            +GetFiedListResponse(httpRequest.getKey(),httpRequest.getValue()));


                ResponseTextBody = ResponseTextBody.replace("$finddate.day$" ,""+httpRequest.getDay());
                ResponseTextBody = ResponseTextBody.replace("$finddate.month$",""+httpRequest.getMonth());
                ResponseTextBody = ResponseTextBody.replace("$finddate.year$",""+httpRequest.getYear());
                ResponseTextBody = ResponseTextBody.replace("$finddate.key$",""+httpRequest.getKey());
                ResponseTextBody = ResponseTextBody.replace("$finddate.value$",""+httpRequest.getValue());
            };
            httpResponse.setBody(ResponseTextBody);
            countBody = GetBytesResponse(ResponseTextBody);
        }
        }





        LocalDateTime lastModifed = Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();

        httpResponse.SetHeaders("Server","HTTP Server Avaya S8500");
        httpResponse.SetHeaders("Date",GetLocalTimeHttpServer(LocalDateTime.now()));
        httpResponse.SetHeaders("Cache-Control","no-cache");
        httpResponse.SetHeaders("Content-Type",httpRequest.getMimeType());
        httpResponse.SetHeaders("Content-Length",""+countBody);
        httpResponse.SetHeaders("Last-Modified",GetLocalTimeHttpServer(lastModifed));
        httpResponse.SetHeaders("Connection","close");



        String response = httpResponse.GetResponse();

        httpConnection.sendString(response);
        if (ResponseTextBody.isEmpty()) httpConnection.sendBytes(ResponseBody);
        else httpConnection.sendString(ResponseTextBody);
        file = null;
        httpResponse = null;
    }

    private int GetBytesResponse(String value){
        int count = 0;
        try {
            count = value.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            count = 0;
        };
        return count;
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





    private String GetFileTextResponse(String file) {

        BufferedReader reader = null;


        try {
            reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file), "UTF8"));
        } catch (UnsupportedEncodingException e) {
           event.onException(app,e);
            return null;
        } catch (FileNotFoundException e) {
            event.onException(app,e);
            return null;
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

        return null;
    }


    private byte[] GetFileResponse(String filename) {

        File file = new File(filename);
        Path path = file.toPath();

        try {
            return Files.readAllBytes(path);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String GetFiedListResponse(String Key,String Value) {
        StringBuilder stringBuilder = new StringBuilder();
        LinkedHashMap <String,String> list = new LinkedHashMap <String,String>();
        list.put("CalledNumber","Иходящий");
        list.put("CallingNumber","Входящий");
        list.put("InTrkCode","Иходящий TAC");
        list.put("CodeUsed","Входящий TAC");


        for (HashMap.Entry<String, String> entry : list.entrySet()) {
                String strSelect = "";
                if (entry.getKey().equals(Key)) strSelect = "selected ";
                stringBuilder.append("<option "+strSelect+ "value='"+entry.getKey()+"'>" +entry.getValue()+"</option>");
            }

        return stringBuilder.toString();
    }
    private String GetCDRResponse(ArrayList <AvayaCDRData> cdrData) {
        StringBuilder stringBuilder = new StringBuilder();
        if (cdrData == null) return "";

        int cnt = cdrData.size();
        for (int i = 0; i < cnt; i++) {

            stringBuilder.append(" <tr> ");
            stringBuilder.append("<th>"+(i+1)+"</th> ");
            stringBuilder.append("<th>"+GetLocalTimeHttpClient(cdrData.get(i).date)+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).calledNumber.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).callingNumber.trim()+"</th> ");
            stringBuilder.append("<th>"+(""+cdrData.get(i).duration*6).trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).cond_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_dial.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_trk_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_used.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).acct_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).auth_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).frl.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).ixc_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_crt_id.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).out_crt_id.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).feat_flag.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_return.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).line_feed.trim()+"</th> ");
            stringBuilder.append(" </tr> ");
        };
        return stringBuilder.toString();
    }
        private String GetBodyResponse(HTTPRequest httpRequest, ArrayList <AvayaCDRData> cdrData) {

        String filename=httpRequest.GetPatch();

        if (filename.equals("www"+File.separator)) filename += "index.html";
        System.out.println("Filename = " +filename);

        String Response = GetFileTextResponse(filename);

        if (filename.endsWith("finddate.html")) {
        StringBuilder stringBuilder = new StringBuilder();

        int cnt = cdrData.size();
        for (int i = 0; i < cnt; i++) {
            stringBuilder.append(" <tr> ");
            stringBuilder.append("<th>"+(i+1)+"</th> ");
            stringBuilder.append("<th>"+GetLocalTimeHttpClient(cdrData.get(i).date)+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).calledNumber.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).callingNumber.trim()+"</th> ");
            stringBuilder.append("<th>"+(""+cdrData.get(i).duration*6).trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).cond_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_dial.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_used.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_trk_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).acct_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).auth_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).frl.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).ixc_code.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).in_crt_id.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).out_crt_id.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).feat_flag.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).code_return.trim()+"</th> ");
            stringBuilder.append("<th>"+cdrData.get(i).line_feed.trim()+"</th> ");
            stringBuilder.append(" </tr> ");
        };
            Response = Response.replace("$COUNTCDR$",""+cnt);
            Response = Response.replace("$BaseCDRList$",stringBuilder.toString());
            Response = Response.replace("$finddate.day$" ,""+httpRequest.getDay());
            Response = Response.replace("$finddate.month$",""+httpRequest.getMonth());
            Response = Response.replace("$finddate.year$",""+httpRequest.getYear());
            Response = Response.replace("$findnumber.key$",""+httpRequest.getKey());
            Response = Response.replace("$findnumber.value$",""+httpRequest.getValue());
        }

        return Response;
    }


}
