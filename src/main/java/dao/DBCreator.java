package dao;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCreator {

    private static final String CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS account(
            id INTEGER PRIMARY KEY,
            number TEXT,
            pin TEXT,
            balance INTEGER DEFAULT 0)""";

    private final SQLiteDataSource dataSource;
    private Connection connection;

    public DBCreator(String dbPath) {
        dataSource = new SQLiteDataSource();

        if (dbPath.contains(".s3db")) {
            dataSource.setUrl("jdbc:sqlite:" + dbPath);
            dbConnectionAttempt();
            createTable();
        } else {
            throw new IllegalArgumentException("Wrong file extension: " + dbPath);
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
}
