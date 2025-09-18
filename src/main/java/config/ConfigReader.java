package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader{
    private static Properties properties = new Properties();

    static{
        try{
            FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
            properties.load(fis);
        }catch (IOException e){
            throw new RuntimeException("failed to load confi.properties", e);
        }
    }
    public static String get(String Key){
        return properties.getProperty(Key);
    }
}
