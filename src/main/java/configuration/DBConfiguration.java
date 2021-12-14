package configuration;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConfiguration {

    private static final String CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS account(
            id INTEGER PRIMARY KEY,
            number TEXT,
            pin TEXT,
            balance INTEGER DEFAULT 0)""";

    private final SQLiteDataSource dataSource;
    private Connection connection;

    public DBConfiguration(String dbName) {
        dataSource = new SQLiteDataSource();

        if (dbName.contains(".s3db")) {
            dataSource.setUrl("jdbc:sqlite:" + dbName);
            dbConnectionAttempt();
            createTable();
        } else {
            throw new IllegalArgumentException("Wrong file extension: " + dbName);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void dbConnectionAttempt() {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Cannot connect to database!");
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            System.err.println("Cannot create table in the database!");
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing the connection with database!");
        }
    }
}
