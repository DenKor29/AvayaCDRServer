package avayacdr.database;

import avayacdr.core.AvayCDRData;
import avayacdr.http.HTTPRequest;

import java.util.ArrayList;

public interface DBServerListener {
    void onRecivedCDR(DBServer dbServer, HTTPRequest httpRequest, ArrayList <AvayCDRData> cdrData);
}
