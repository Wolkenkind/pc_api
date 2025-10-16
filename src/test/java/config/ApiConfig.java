package config;

import static util.ConfigUtils.getValue;

public class ApiConfig {
    public static final String BASE_URL;
    public static final String BASE_API_PATH;
    public static final long RESPONSE_TIME_THRESHOLD;

    static {
        BASE_URL = getValue("BASE_URL", "http://localhost:9966/petclinic");
        BASE_API_PATH = getValue("BASE_API_PATH", "/api");
        RESPONSE_TIME_THRESHOLD = Long.parseLong(getValue("API_RESPONSE_TIME", "5000"));
    }
}
