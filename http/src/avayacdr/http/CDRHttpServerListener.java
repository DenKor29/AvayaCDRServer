package avayacdr.http;

import java.time.LocalDateTime;

public interface CDRHttpServerListener {
    void onFindDBDateZapros(HTTPRequest httpRequest,LocalDateTime BeginTime,LocalDateTime EndTime);
    void onFindDBFieldZapros(HTTPRequest httpRequest,String Key,String Value);
}
