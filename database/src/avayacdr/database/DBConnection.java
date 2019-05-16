package avayacdr.database;

import avayacdr.http.HTTPRequest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {

    private final Connection connection;
    private final HTTPRequest httpRequest;
    private final DBConnectionListener eventListener;
    private final Thread rxThread;
    private final String sql ;

    public DBConnection(DBConnectionListener event, Connection conn, HTTPRequest httpRequest, String query, boolean result) throws SQLException {
        this.eventListener = event;
        this.connection = conn;
        this.httpRequest = httpRequest;
        this.sql = query;

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    //System.out.println("DBConnection create statement:"+sql);
                    Statement statement = connection.createStatement();
                     if (result) {
                         ResultSet resultSet  = statement.executeQuery(sql);
                         eventListener.onResultSet(DBConnection.this, DBConnection.this.httpRequest,resultSet,statement);
                     } else  statement.executeUpdate(sql);

                } catch (SQLException se) {
                    eventListener.onException(DBConnection.this,se);
                }
            }
        });

        rxThread.start();

    }
    @Override
    public String toString() {
        return "DBConnection: " + sql;
    }
}
