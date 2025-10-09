package util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static config.DatabaseConfig.*;

public class DatabaseUtils {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static JdbcTemplate createTemplate() {
        DataSource dataSource = createDataSource();
        return new JdbcTemplate(dataSource);
    }

    public static TransactionTemplate createTransactionTemplate() {
        DataSource dataSource = createDataSource();
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        return new TransactionTemplate(transactionManager);
    }

    private static DataSource createDataSource() {
        return new SingleConnectionDataSource(URL, USERNAME, PASSWORD, true);
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
            stmt.execute("DELETE FROM vet_specialties");
            stmt.execute("DELETE FROM specialties");
            stmt.execute("DELETE FROM types");
            stmt.execute("DELETE FROM vets");

            // Re-enable foreign key checks
            stmt.execute("SET session_replication_role = 'origin'");
        }
    }
}
