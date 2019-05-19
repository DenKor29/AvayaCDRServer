package avayacdr.http;

import avayacdr.core.BaseCDRData;
import avayacdr.network.TCPConnection;

import java.util.ArrayList;


public class HTTPConnection  {
    private HTTPConnectionListener event;
    private StringBuilder requestSB;
    private ArrayList<BaseCDRData> ListCDRData = new ArrayList<>();



    private String request;




    TCPConnection tcpConnection;

    public HTTPConnection(HTTPConnectionListener eventListener,TCPConnection tcpConnection) {
        this.requestSB = new StringBuilder();
        this.tcpConnection = tcpConnection;
        this.event = eventListener;

    }

    public synchronized TCPConnection getTcpConnection() {
        return tcpConnection;
    }


    public synchronized void sendString(String value){

        tcpConnection.sendString(value);
    }
    public synchronized void sendBytes(byte[] value){

        tcpConnection.sendBytes(value);
    }
    public synchronized void CloseConnection()
    {
        getTcpConnection().disconnected();
    }

    public  synchronized void RequestAppendString(String value) {



            //Заглушка
            if (value == null) {
                return;
            }

        requestSB.append(value);
        requestSB.append("\n");


        //Проверка на конец заголовка запроса - 2 пустые строки подряд или приход null
            if (value.isEmpty()){
                this.request = requestSB.toString();
                requestSB.setLength(0);
                event.onReciveRequest(this);
                return;
            };


    }

    public String  GetRequest() {

        return request;
    }


}
