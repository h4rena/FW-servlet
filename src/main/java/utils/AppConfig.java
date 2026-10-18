package main.java.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPS = new Properties();

    public static void load(InputStream in) {
        if (in == null) return;                       
        try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            PROPS.load(r);                            // UTF-8 (accents dans les chemins)
        } catch (Exception e) {
            throw new RuntimeException("Lecture de application.properties impossible", e);
        }
    }

    public static String get(String cle) { 
        return PROPS.getProperty(cle); 
    }

    public static String get(String cle, String defaut) { 
        return PROPS.getProperty(cle, defaut); 
    }
}