package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConfig {
    public static final String URL;
    public static final String USERNAME;
    public static final String PASSWORD;

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("src/test/resources/application.properties"));
        } catch (IOException e) {
            System.out.println("Critical error during test initialisation:");
            throw new RuntimeException(e);
        }

        URL = props.getProperty("db.url");
        USERNAME = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");
    }
}
