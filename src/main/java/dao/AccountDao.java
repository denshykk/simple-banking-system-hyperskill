package dao;

import card.Card;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class AccountDao implements Dao<Account> {

    private static final String CREATE_TABLE_QUERY = """
            CREATE TABLE IF NOT EXISTS card(
            id INTEGER PRIMARY KEY,
            number TEXT,
            pin TEXT,
            balance INTEGER DEFAULT 0)""";
    private static final String GET_ACCOUNT_QUERY = "SELECT number, pin, balance FROM card WHERE number = ? AND pin = ?";
    private static final String ACCOUNT_IS_PRESENT_QUERY = "SELECT number FROM card WHERE number = ?";
    private static final String CREATE_ACCOUNT_QUERY = "INSERT INTO card (number, pin) VALUES (?, ?)";
    private static final String UPDATE_ACCOUNT_QUERY = "UPDATE card SET balance = balance + ? WHERE number = ?";
    private static final String DELETE_ACCOUNT_QUERY = "DELETE FROM card WHERE number = ?";

    private final SQLiteDataSource dataSource;
    private Connection connection;

    public AccountDao(String dbPath) {
        dataSource = new SQLiteDataSource();

        if (dbPath.contains(".s3db")) {
            dataSource.setUrl("jdbc:sqlite:" + dbPath);
            dbConnectionAttempt();
            createTable();
        } else {
            throw new IllegalArgumentException("Wrong file extension: " + dbPath);
        }
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

    @Override
    public Optional<Account> get(String cardNumber, String cardPIN) {
        try (PreparedStatement statement = connection.prepareStatement(GET_ACCOUNT_QUERY)) {
            statement.setString(1, cardNumber);
            statement.setString(2, cardPIN);
            ResultSet resultSet = statement.executeQuery();

            int balance = resultSet.getInt("balance");

            if (resultSet.next()) {
                Account resultAccount = new Account(new Card(cardNumber, cardPIN), balance);
                return Optional.of(resultAccount);
            }
        } catch (SQLException e) {
            System.err.println("Wrong input!");
        }
        return Optional.empty();
    }

    @Override
    public boolean contains(String cardNumber) {
        try (PreparedStatement statement = connection.prepareStatement(ACCOUNT_IS_PRESENT_QUERY)) {
            statement.setString(1, cardNumber);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void save(Account account) {
        try (PreparedStatement statement = connection.prepareStatement(CREATE_ACCOUNT_QUERY)) {
            statement.setString(1, account.card().number());
            statement.setString(2, account.card().pin());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String cardNumber, int income) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ACCOUNT_QUERY)) {
            statement.setString(1, String.valueOf(income));
            statement.setString(2, cardNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Cannot update account!");
        }
    }

    @Override
    public void delete(Account account) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_ACCOUNT_QUERY)) {
            statement.setString(1, account.card().number());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Cannot delete account!");
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing the connection with database!");
        }
    }
}
