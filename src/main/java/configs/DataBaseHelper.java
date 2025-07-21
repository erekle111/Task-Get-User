package configs;


import java.sql.*;
import java.time.LocalDateTime;


public class DataBaseHelper {
    private static final String DB_URL = "jdbc:sqlite:test_results.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS test_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                test_name TEXT UNIQUE NOT NULL,
                status TEXT NOT NULL,
                execution_time DATETIME NOT NULL
            )
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void saveTestResult(String testName, String status) {
        String upsertSQL = """
            INSERT INTO test_results (test_name, status, execution_time) 
            VALUES (?, ?, ?)
            ON CONFLICT(test_name) DO UPDATE SET 
                status = excluded.status,
                execution_time = excluded.execution_time
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {

            pstmt.setString(1, testName);
            pstmt.setString(2, status);
            pstmt.setString(3, LocalDateTime.now().toString());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save test result", e);
        }
    }

    public static void clearTestResults() {
        String deleteSQL = "DELETE FROM test_results";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(deleteSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear test results", e);
        }
    }
}