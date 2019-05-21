package avayacdr.http;

import java.time.LocalDateTime;

public interface CDRHttpServerListener {
    void onFindDBDateZapros(HTTPRequest httpRequest,LocalDateTime BeginTime,LocalDateTime EndTime, String Key, String Value);
}
