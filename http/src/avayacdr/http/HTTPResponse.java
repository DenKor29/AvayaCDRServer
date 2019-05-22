package avayacdr.http;


import java.util.HashMap;
import java.util.LinkedHashMap;

public class HTTPResponse {
    private LinkedHashMap< String, String> headers;
    private String status;



    public HTTPResponse() {
        this.headers = new LinkedHashMap< String, String> ();
        this.status = "HTTP/1.1 200 OK\n";
    }


    protected void SetResponseCode(String code){
        this.status = "HTTP/1.1 " + code + "\n";
    }


    protected String GetHeaders(){
    StringBuilder Response = new StringBuilder();
    Response.append(status);
        for (HashMap.Entry<String, String> header : headers.entrySet()) {
             Response.append(header.getKey()+": "+header.getValue()+"\n");
        }
        Response.append("\n");
        return Response.toString();
    }

     protected void SetHeaders(String key, String value){

        headers.put(key,value);
    }
    protected void ClearHeaders(){

        headers.clear();
    }
}
