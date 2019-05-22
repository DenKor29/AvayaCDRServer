package avayacdr.core;

public class Util {



    public static String GetOperandName(int i)
    {
        String result = "";
        switch (i){
            case 0: {result =  "=";break;}
            case 1: {result =   ">";break;}
            case 2: {result =   "<";break;}
            case 3: {result =   ">=";break;}
            case 4: {result =   "<=";break;}
        }
        return result;
    }
    public static int GetIntFromString(String value) {
        return GetIntFromString(value,0);
    }
        public static int GetIntFromString(String value,int defvalue) {
        int result;

        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            result = defvalue;
        }

        return result;
    }

    public static String GetStatusServer(boolean status)
    {
        if (status) return "<span class='greentext'> Запущен. </span>";
        return "<span class='redtext'> Остановлен. </span>";
    }
}
