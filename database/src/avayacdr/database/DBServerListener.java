package avayacdr.database;

import avayacdr.core.AvayaCDRData;
import avayacdr.http.HTTPRequest;

import java.util.ArrayList;

public interface DBServerListener {
    void onRecivedCDR(DBServer dbServer, HTTPRequest httpRequest, ArrayList <AvayaCDRData> cdrData);
}
