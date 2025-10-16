package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtils {
    private static final Properties props = new Properties();
    private static boolean useProps = true;
    private static final Object loadLock = new Object();

    public static String getValue(String envVar, String defaultValue) {
        // 1. Check environment variable
        String envValue = System.getenv(envVar);
        if (envValue != null) {
            return envValue;
        }

        // 2. Check system property (command line)
        String sysValue = System.getProperty(envVar);
        if (sysValue != null) {
            return sysValue;
        }

        // 3. Check properties or use default
        return getFromProps(envVar, defaultValue);
    }

    private static String getFromProps(String keyName, String defaultValue) {
        if (!useProps) {
            return defaultValue;
        } else {
            synchronized (loadLock) {
                if (useProps) {
                    if (props.isEmpty()) {
                        try {
                            loadProperties();
                        } catch (Exception e) {
                            System.out.println("Error during test initialisation: " + e.getMessage());
                            useProps = false;
                        }
                    }
                }
            }
        }
        return getProperty(keyName, defaultValue);
    }

    private static String getProperty(String keyName, String defaultValue) {
        return switch (keyName) {
            case "DB_USER" -> props.getProperty("db.username", defaultValue);
            case "DB_PASSWORD" -> props.getProperty("db.password", defaultValue);
            case "DB_VENDOR" -> props.getProperty("db.vendor", defaultValue);
            case "DB_HOST" -> props.getProperty("db.host", defaultValue);
            case "DB_PORT" -> props.getProperty("db.port", defaultValue);
            case "BASE_URL" -> props.getProperty("base.url", defaultValue);
            case "BASE_API_PATH" -> props.getProperty("base.api.path", defaultValue);
            case "API_RESPONSE_TIME" -> props.getProperty("base.response.time.ms", defaultValue);
            default -> defaultValue;
        };
    }

    private static void loadProperties() throws IOException {
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Could not load application.properties file from classpath, falling to default");
                throw new RuntimeException("Unable to find application.properties in classpath");
            }
            props.load(input);
        }
    }
}
