package avayacdr.http;

import com.sun.deploy.nativesandbox.comm.Response;

import java.util.HashMap;

public class HTTPResponse {
    private HashMap< String, String> headers;
    private String status;
    private String body;



    public HTTPResponse(String code, String body) {
        this.headers = new HashMap< String, String> ();
        this.status = "HTTP/1.1 " + code + "\n";
        this.body = body;
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
    return GetHeaders()+body;
    }

    public void SetHeaders(String key,String value){

        headers.put(key,value);
    }
    public void ClearHeaders(){

        headers.clear();
    }
}
