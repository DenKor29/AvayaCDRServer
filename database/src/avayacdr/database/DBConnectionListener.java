package avayacdr.database;

import avayacdr.http.HTTPRequest;

import java.sql.ResultSet;
import java.sql.Statement;

public interface DBConnectionListener {
    void onConnectionReady(DBConnection dbConnection);
    void onDisconnection(DBConnection dbConnection);
    void onException(DBConnection dbConnection, Exception e);
    void onResultSet(DBConnection dbConnection, HTTPRequest httpRequest, ResultSet resultSet, Statement statement);
}
