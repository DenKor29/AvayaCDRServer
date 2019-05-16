package avayacdr.database;

import avayacdr.core.AvayCDRData;
import avayacdr.core.BaseCDRData;
import avayacdr.http.HTTPRequest;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class DBServer implements DBConnectionListener {

    public static final String SQLDATATIMEFORMAT = "yyyy-MM-dd HH:mm:ss";

    private Connection connection;
    private DBServerListener eventListener;
    private String nameDB;
    private String nameTable;
    private boolean status;


    public DBServer(DBServerListener event,String url, String urlParam,String nameDB,String NameTable,String user, String password) {

        this.nameDB = nameDB;
        this.nameTable = NameTable;
        this.status = false;
        this.eventListener = event;




        try {
            connection = DriverManager.getConnection(url + nameDB + urlParam, user, password);
            System.out.println("DBServer Start..." );
            if (isExistTable(connection,nameTable)) {
                System.out.println("DBServer Table:"+NameTable+" is exist." );
            } else CreateTable();
        } catch (SQLException e) {
            System.out.println("DBServer Fault Start..." );
            System.out.println("DBServer Exeption: " + e);
        }
        status = true;
    }

    public String getNameTable() {
        return nameTable;
    }

    private boolean isExistTable(Connection connection, String name){

        boolean isExist = false;
        try {
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, name, null);
            if (rs.next())  isExist =true;
        } catch (SQLException e) {
            System.out.println("DBServer Exeption: " + e);
        }
        return isExist;
    }

    private synchronized void sendQuery(HTTPRequest httpRequest, String query, boolean update) {

        try {
            new DBConnection(this, connection,httpRequest, query,!update);
        } catch (SQLException e) {
            System.out.println("DBServer Query Exeption: " + e);
        }

    }



    public void CreateTable(){

        //Не запускаем общие методы без полной инициализации класса
        if (!status) return;

        System.out.println("DBServer Create Table " + nameTable + " ...");
        String query = "CREATE TABLE IF NOT EXISTS "+ nameTable + " (\n" +
                "    Id int(11) NOT NULL AUTO_INCREMENT,\n" +
                "    Value varchar(255),\n" +
                "    Date datatime,\n" +
                "    CallingNumber varchar(15),\n" +
                "    CalledNumber varchar(18), \n" +
                "    Duration int(4), \n" +
                "    CondCode varchar(4), \n" +
                "    CodeUsed varchar(4), \n" +
                "    CodeDial varchar(4), \n" +
                "    InTrkCode varchar(4), \n" +
                "    AcctCode varchar(5), \n" +
                "    AuthCode varchar(5), \n" +
                "    Frl varchar(4), \n" +
                "    IxcCode varchar(4), \n" +
                "    InCrtId varchar(4), \n" +
                "    OutCrtId varchar(4), \n" +
                "    FeatFlag varchar(4), \n" +
                "    CodeReturn varchar(4), \n" +
                "    LineFeed varchar(4), \n" +
                "    PRIMARY KEY (Id));";
        sendQuery(null,query,true);


    }

    private String LocalDateTimeToString(LocalDateTime localDateTime){

        String result = "";
        LocalDateTime dateTime;
        if (localDateTime == null) dateTime =LocalDateTime.now(); else dateTime = localDateTime;

        try {
            result = dateTime.format(DateTimeFormatter.ofPattern(SQLDATATIMEFORMAT));
        } catch (DateTimeParseException e) {
            System.out.println("DBServer SQLDateTime Exeption: " + e);
        }

        return  result;
    }

    public void AppendTableString(AvayCDRData baseCDRData)
    {
        //Не запускаем общие методы без полной инициализации класса
        if (!status) return;

        String BeginDate = LocalDateTimeToString(baseCDRData.date);



        String query = "INSERT INTO " + nameTable +" (Value,Date,Duration,CondCode,CodeDial,CodeUsed,InTrkCode,CallingNumber,CalledNumber,AcctCode,AuthCode,Frl,IxcCode,InCrtId,OutCrtId,FeatFlag,CodeReturn,LineFeed)  \n" +
                "VALUES ('"+baseCDRData.value+"',\n" +
                "'" + BeginDate +"'," +
                "'" + baseCDRData.duration +"'," +
                "'" + baseCDRData.cond_code +"'," +
                "'" + baseCDRData.code_dial +"'," +
                "'" + baseCDRData.code_used +"'," +
                "'" + baseCDRData.in_trk_code +"'," +
                "'" + baseCDRData.callingNumber +"'," +
                "'" + baseCDRData.calledNumber +"'," +
                "'" + baseCDRData.acct_code +"'," +
                "'" + baseCDRData.auth_code +"'," +
                "'" + baseCDRData.frl +"'," +
                "'" + baseCDRData.ixc_code +"'," +
                "'" + baseCDRData.in_crt_id +"'," +
                "'" + baseCDRData.out_crt_id +"'," +
                "'" + baseCDRData.feat_flag +"'," +
                "'" + baseCDRData.code_return +"'," +
                "'" + baseCDRData.line_feed +"');";

        sendQuery(null,query,true);
    }

    public void FindDateTimeTable(HTTPRequest httpRequest,LocalDateTime BeginTime, LocalDateTime EndTime)
    {
        //Не запускаем общие методы без полной инициализации класса
        if (!status) return;


        String BeginDate = LocalDateTimeToString(BeginTime);
        String EndDate = LocalDateTimeToString(EndTime);

        System.out.println("DBServer Find String " + nameTable + " ...");
        String query = "SELECT Value,Date,Duration,CondCode,CodeDial,CodeUsed,InTrkCode,CallingNumber,CalledNumber,AcctCode,AuthCode,Frl,IxcCode,InCrtId,OutCrtId,FeatFlag,CodeReturn,LineFeed FROM " + nameTable + " WHERE Date BETWEEN '" +
                BeginDate +"' AND '" + EndDate +"';";

        sendQuery(httpRequest,query,false);
    }

    @Override
    public synchronized void onConnectionReady(DBConnection dbConnection) {
       System.out.println("DBConnection  Ready.");

    }

    @Override
    public synchronized void onDisconnection(DBConnection dbConnection) {
        System.out.println("DBConnection  Disconnect.");
    }

    @Override
    public synchronized void onException(DBConnection dbConnection, Exception e) {
        System.out.println("DBServer Exeption: " + e);

    }
    @Override
    public synchronized void onResultSet(DBConnection dbConnection,HTTPRequest httpRequest, ResultSet resultSet, Statement statement) {
        try {

            ArrayList <AvayCDRData> listCDRData = new ArrayList<>();

            while (resultSet.next()) {

                AvayCDRData baseCDRData = new AvayCDRData();

                baseCDRData.name = nameTable;
                baseCDRData.value = resultSet.getString("Value");
                baseCDRData.date = resultSet.getTimestamp("Date").toLocalDateTime();
                baseCDRData.calledNumber = resultSet.getString("CalledNumber");
                baseCDRData.callingNumber = resultSet.getString("CallingNumber");
                baseCDRData.duration = resultSet.getInt("Duration");
                baseCDRData.cond_code = resultSet.getString("CondCode");
                baseCDRData.code_dial = resultSet.getString("CodeDial");
                baseCDRData.code_used = resultSet.getString("CodeUsed");
                baseCDRData.in_trk_code = resultSet.getString("InTrkCode");
                baseCDRData.acct_code = resultSet.getString("AcctCode");
                baseCDRData.auth_code = resultSet.getString("AuthCode");
                baseCDRData.frl = resultSet.getString("Frl");
                baseCDRData.ixc_code = resultSet.getString("IxcCode");
                baseCDRData.in_crt_id = resultSet.getString("InCrtId");
                baseCDRData.out_crt_id = resultSet.getString("OutCrtId");
                baseCDRData.feat_flag = resultSet.getString("FeatFlag");
                baseCDRData.code_return = resultSet.getString("CodeReturn");
                baseCDRData.line_feed = resultSet.getString("LineFeed");

                listCDRData.add(baseCDRData);
            };

            eventListener.onRecivedCDR(this,httpRequest, listCDRData);


            resultSet.close(); resultSet = null;
            statement.close(); statement = null;
            }
            catch (SQLException e) {
            System.out.println("DBServer Exeption: " + e);
            };

    }

}
