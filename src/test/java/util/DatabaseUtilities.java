package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static config.DatabaseConfig.*;

public class DatabaseUtilities {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void cleanDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Disable foreign key checks
            stmt.execute("SET session_replication_role = 'replica'");

            // Clean tables in correct order to respect foreign keys
            stmt.execute("DELETE FROM visits");
            stmt.execute("DELETE FROM pets");
            stmt.execute("DELETE FROM owners");
            stmt.execute("DELETE FROM specialties");
            stmt.execute("DELETE FROM types");
            stmt.execute("DELETE FROM vets");

            // Re-enable foreign key checks
            stmt.execute("SET session_replication_role = 'origin'");
        }
    }
}
