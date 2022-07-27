package configuration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteDataSource;

import lombok.Getter;

/**
 * This is configuration class for database.
 */
public class DBConfiguration {

  private static final String CREATE_TABLE_QUERY = """
                                                   CREATE TABLE IF NOT EXISTS account(
                                                   id INTEGER PRIMARY KEY,
                                                   number TEXT,
                                                   pin TEXT,
                                                   balance INTEGER DEFAULT 0)""";

  private final SQLiteDataSource dataSource;
  @Getter
  private       Connection       connection;

  public DBConfiguration(final SQLiteDataSource dataSource, final String dbName) {
    this.dataSource = dataSource;

    if (dbName.contains(".s3db")) {
      dataSource.setUrl("jdbc:sqlite:" + dbName);
      dbConnectionAttempt();
      createTable();
    } else {
      throw new IllegalArgumentException("Wrong file extension: " + dbName);
    }
  }

  /**
   * Try to get a connection from the data source, and if it fails, print an error message.
   */
  public void dbConnectionAttempt() {
    try {
      connection = dataSource.getConnection();
    } catch (SQLException e) {
      System.err.println("Cannot connect to database!");
    }
  }

  /**
   * It creates a table in the database
   */
  public void createTable() {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(CREATE_TABLE_QUERY);
    } catch (SQLException e) {
      System.err.println("Cannot create table in the database!");
    }
  }

  /**
   * It closes the connection with the database
   */
  public void closeConnection() {
    try {
      connection.close();
    } catch (SQLException e) {
      System.err.println("Error closing the connection with database!");
    }
  }

}
