package avayacdr.http;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class HTTPResponse {
    private LinkedHashMap< String, String> headers;
    private String status;


    private String body;




    public HTTPResponse() {
        this.headers = new LinkedHashMap< String, String> ();
        this.status = "HTTP/1.1 200 OK\n";
        this.body = "";

    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }


    public void SetResponseCode(String code){
        this.status = "HTTP/1.1 " + code + "\n";
    }


    private String GetHeaders(){
    StringBuilder Response = new StringBuilder();
    Response.append(status);
        for (HashMap.Entry<String, String> header : headers.entrySet()) {
             Response.append(header.getKey()+": "+header.getValue()+"\n");
        }
        Response.append("\n");
        return Response.toString();
    }

    public String GetResponse(){
    return GetHeaders();
    }

    public void SetHeaders(String key,String value){

        headers.put(key,value);
    }
    public void ClearHeaders(){

        headers.clear();
    }
}
