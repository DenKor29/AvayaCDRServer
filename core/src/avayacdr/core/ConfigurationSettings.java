package avayacdr.core;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationSettings {
    private String propertiesFile;
    private Properties properties;
    private String commentText;

    public ConfigurationSettings(String fileName, String name) {
        this.propertiesFile = fileName;
        this.commentText = name;
        this.properties = new Properties();
        loadSettings();
    }

    public void loadSettings()
    {

        try {
            properties.loadFromXML(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public String get(String key){
        return get(key,"");
    }

    public String get(String key, String defvalue)
    {
        String value = properties.getProperty(key);

        if (value == null) value = defvalue;
        else value = properties.getProperty(key);

        return value.trim();
    }

    public int getInt(String key, int defvalue)  {

        return Util.GetIntFromString(key,defvalue);

    }

}
