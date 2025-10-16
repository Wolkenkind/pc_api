package config;

import static util.ConfigUtils.getValue;

public class DatabaseConfig {
    public static final String URL;
    public static final String HOST;
    public static final String PORT;
    public static final String USERNAME;
    public static final String PASSWORD;
    public static final String VENDOR;

    private static final String PG = "postgres";
    private static final String MSQL = "mysql";

    static {
        USERNAME = getValue("DB_USER", "petclinic");
        PASSWORD = getValue("DB_PASSWORD", "petclinic");
        HOST = getValue("DB_HOST", "localhost");
        PORT = getValue("DB_PORT", "5432");
        VENDOR = getValue("DB_VENDOR", PG);
        try {
            URL = switch (VENDOR) {
                case PG -> "jdbc:postgresql://" + HOST + ":" + PORT + "/petclinic";
                case MSQL -> "not_implemented";
                default -> throw new IllegalArgumentException("Unknown DB vendor!");
            };
        } catch (IllegalArgumentException e) {
            System.err.println("FATAL: " + e.getMessage());
            System.err.println("Supported vendors: " + PG + ", " + MSQL);
            throw e;
        }
    }
}
