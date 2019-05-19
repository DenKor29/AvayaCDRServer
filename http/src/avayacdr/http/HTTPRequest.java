package avayacdr.http;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;

public class HTTPRequest {
    HashMap< String, String> parameters;
    String path;


    private HTTPConnection connection;
    private int day;
    private int month;
    private int year;

    private String key;
    private String value;


    public String getMimeType() {

        String mimetype="text/plain";

        if (path.endsWith(".html")) mimetype = "text/html";
        if (path.endsWith(".js")) mimetype = "text/javascript";
        if (path.endsWith(".css")) mimetype = "text/css";
        if (path.endsWith(".png")) mimetype = "image/png";
        if (path.endsWith(".ico")) mimetype = "image/x-icon";
        if (path.equals("www"+File.separator)) mimetype = "text/html";

        if (mimetype.startsWith("text")) mimetype+="; charset=utf-8";

        return mimetype;
    }

    public String getKey() {
        return key;
    }


    public String getValue() {
        return value;
    }


    public int getDay() {
        return day;
    }


    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }


    public HTTPRequest(HTTPConnection connection)
    {
        String request = connection.GetRequest();
        this.parameters = new HashMap<String, String>();
        this.path = GetPath(request);
        this.connection = connection;

        SetParameters(request);

        this.day = GetParametersInt("day");
        this.month = GetParametersInt("month");
        this.year = GetParametersInt("year");
        this.key = GetParameters("key");
        this.value = GetParameters("value");

        if (day == 0 || month ==0 || year == 0) {
            LocalDateTime time = LocalDateTime.now();
            this.day = time.getDayOfMonth();
            this.month = time.getMonthValue();
            this.year = time.getYear();

        }
        if (key.isEmpty() || value.isEmpty()) {
            this.key = "CallingNumber";
            this.value = "";

        }

    }

    public HTTPConnection getConnection() {
        return connection;
    }

    private String GetParameters(String name){
        if (parameters.get(name) == null) return "";
        return  parameters.get(name).trim();
    }

    public int GetParametersInt(String name){

        int result;
        try {
           result = Integer.parseInt(GetParameters(name));
        } catch (NumberFormatException e){
            result = 0;
        }

        return result;
    }

    public  String GetPatch()
    {
        return path;
    }

    // "вырезает" из HTTP заголовка URI ресурса и конвертирует его в filepath
    // URI берётся только для GET и POST запросов, иначе возвращается null
    private String GetPath(String header)
    {
        // ищем URI, указанный в HTTP запросе
        // URI ищется только для методов POST и GET, иначе возвращается null
        String URI = extract(header, "GET ", " "), path;
        if(URI == null) URI = extract(header, "POST ", " ");
        if(URI == null) return null;

        // если URI записан вместе с именем протокола
        // то удаляем протокол и имя хоста
        path = URI.toLowerCase();
        if(path.indexOf("http://", 0) == 0)
        {
            URI = URI.substring(7);
            URI = URI.substring(URI.indexOf("/", 0));
        }
        else if(path.indexOf("/", 0) == 0)
            URI = URI.substring(1); // если URI начинается с символа /, удаляем его

        // отсекаем из URI часть запроса, идущего после символов ?
        int i = URI.indexOf("?");
        if(i > -1) URI = URI.substring(0, i);

        i = URI.indexOf("#");
        if(i > -1) URI = URI.substring(0, i);

        // конвертируем URI в путь до документов
        // предполагается, что документы лежат там же, где и сервер
        // иначе ниже нужно переопределить path
        path = "www" + File.separator;
        char a;
        for(i = 0; i < URI.length(); i++)
        {
            a = URI.charAt(i);
            if(a == '/')
                path = path + File.separator;
            else
                path = path + a;
        }

        return path;
    }
    protected void SetParameters(String header)
    {
        String URI = extract(header, "GET ", " ");
        if(URI == null) URI = extract(header, "POST ", " ");
        if(URI == null) return;


        parameters.clear();
        int i = URI.indexOf("?");
        if(i == -1) return;

        URI = URI.substring(i+1);
        String[] listParameters = URI.split("&");
        for (String param:listParameters){
            String[] listNames = param.split("=");
            for (int j=0;j<listNames.length;j=j+2) {
                if ((j + 1) >= listNames.length) break;
                parameters.put(listNames[j], listNames[j + 1]);
            }

        }


    }


    // "вырезает" из строки str часть, находящуюся между строками start и end
    // если строки end нет, то берётся строка после start
    // если кусок не найден, возвращается null
    // для поиска берётся строка до "\n\n" или "\r\n\r\n", если таковые присутствуют
    protected String extract(String str, String start, String end)
    {
        int s = str.indexOf("\n\n", 0), e;
        if(s < 0) s = str.indexOf("\r\n\r\n", 0);
        if(s > 0) str = str.substring(0, s);
        s = str.indexOf(start, 0)+start.length();
        if(s < start.length()) return null;
        e = str.indexOf(end, s);
        if(e < 0) e = str.length();
        return (str.substring(s, e)).trim();
    }
}
