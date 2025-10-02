package config;

import io.restassured.specification.RequestSpecification;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiConfig {
    public static final String BASE_URL;
    public static final String BASE_API_PATH;
    //public static final String BASE_URL;

    static {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("src/test/resources/application.properties"));
        } catch (IOException e) {
            System.out.println("Critical error during test initialisation:");
            throw new RuntimeException(e);
        }

        BASE_URL = props.getProperty("base.url");
        BASE_API_PATH = props.getProperty("base.api.path");
    }
}
