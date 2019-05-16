package avayacdr.application;

import avayacdr.network.TCPConnection;

public interface ApplicationServerListener {
    void onConnectionServer(ApplicationServer applicationServer);
    void onConnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection);
    void onDisconnectionReady(ApplicationServer applicationServer, TCPConnection tcpConnection);
    void onDisconnection(ApplicationServer applicationServer);
    void onException(ApplicationServer applicationServer,Exception e);
    void onMessageString(ApplicationServer applicationServer,TCPConnection tcpConnection,String value);

}
