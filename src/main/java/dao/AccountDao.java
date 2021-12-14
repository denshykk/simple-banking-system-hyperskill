package dao;

import configuration.DBConfiguration;
import model.Account;
import model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public record AccountDao(DBConfiguration dbConfiguration) {

    private static final String GET_ACCOUNT_QUERY = "SELECT number, pin, balance FROM account WHERE number = ? AND pin = ?";
    private static final String ACCOUNT_IS_PRESENT_QUERY = "SELECT number FROM account WHERE number = ?";
    private static final String CREATE_ACCOUNT_QUERY = "INSERT INTO account (number, pin) VALUES (?, ?)";
    private static final String UPDATE_ACCOUNT_QUERY = "UPDATE account SET balance = balance + ? WHERE number = ?";
    private static final String DELETE_ACCOUNT_QUERY = "DELETE FROM account WHERE number = ?";

    public Optional<Account> get(String cardNumber, String cardPIN) {
        try (PreparedStatement statement = dbConfiguration.getConnection().prepareStatement(GET_ACCOUNT_QUERY)) {
            statement.setString(1, cardNumber);
            statement.setString(2, cardPIN);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int balance = resultSet.getInt("balance");
                Account resultAccount = new Account(new Card(cardNumber, cardPIN), balance);
                return Optional.of(resultAccount);
            }
        } catch (SQLException e) {
            System.err.println("Wrong input!");
        }
        return Optional.empty();
    }

    public boolean contains(String cardNumber) {
        try (PreparedStatement statement = dbConfiguration.getConnection().prepareStatement(ACCOUNT_IS_PRESENT_QUERY)) {
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

    public void save(Account account) {
        try (PreparedStatement statement = dbConfiguration.getConnection().prepareStatement(CREATE_ACCOUNT_QUERY)) {
            statement.setString(1, account.getCard().number());
            statement.setString(2, account.getCard().pin());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(String cardNumber, int income) {
        try (PreparedStatement statement = dbConfiguration.getConnection().prepareStatement(UPDATE_ACCOUNT_QUERY)) {
            statement.setString(1, String.valueOf(income));
            statement.setString(2, cardNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Cannot update account!");
        }
    }

    public void delete(Account account) {
        try (PreparedStatement statement = dbConfiguration.getConnection().prepareStatement(DELETE_ACCOUNT_QUERY)) {
            statement.setString(1, account.getCard().number());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Cannot delete account!");
        }
    }
}
