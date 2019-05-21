package avayacdr.core;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BaseCDRData {
    public String name;
    public LocalDateTime date;
    public String callingNumber;
    public String calledNumber;
    public int duration;
    public String value;

    public void SetPropertyCDR(String name, String value) {

       this.value = value;
       this.name = name;
       this.callingNumber = "";
       this.calledNumber = "";
       this.date = LocalDateTime.now();
       this.duration = 0;
    }

    protected final String GetFieldCDR(String value, int begin, int end)
    {
        int length = value.length();
        if (length == 0) return "";

        int BeginField = begin > length ? length-1 : begin;
        int EndField = length < end ? length-1 : end;

        return value.substring(BeginField,EndField).trim();
    }
    protected final int GetFieldCDRInt(String value, int begin, int end, int defvalue)
    {
        return Util.GetIntFromString(GetFieldCDR(value,begin,end));
    }

    protected final void SetFieldCDRTime(String value, String dateformat,int begin, int end)
    {
        String fieldCDR = GetFieldCDR(value,begin,end);

        try{
            this.date = LocalDateTime.parse(fieldCDR, DateTimeFormatter.ofPattern(dateformat));
        }
        catch(DateTimeParseException e){
            System.out.println("AvayaCDR DateTime Exeption: " + e);
        }
    }

    public String GetPropertyCDR() {

        return value;
    }

    @Override
    public String toString() {
        return name + " : " + value;
    }
}
