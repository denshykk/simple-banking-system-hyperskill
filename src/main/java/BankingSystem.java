import java.util.Scanner;

import dao.AccountDao;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import model.Account;
import model.Card;

/**
 * A class that represents a banking system.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class BankingSystem {

  private final AccountDao    accountDao;
  private final CardGenerator cardGenerator;
  private final Scanner       scanner;
  private       Account       currentAccount;

  /**
   * While the application is running, show the welcome menu, get the user's input, and if the input is 0, stop the
   * application, if the input is 1, register an account, if the input is 2, log into an account, otherwise, show an
   * error message.
   */
  public void showMenu() {
    while (true) {
      welcomeMenu();

      String input = scanner.next();

      switch (input) {
        case "0" -> stopApplication();
        case "1" -> registerAccount();
        case "2" -> logIntoAccount();
        default -> System.out.println("You've entered invalid menu item.\n");
      }
    }
  }

  /**
   * Show the welcome menu.
   */
  public void welcomeMenu() {
    System.out.println("""
                       1. Create an account
                       2. Log into account
                       0. Exit""");
  }

  /**
   * Register an account. <br/>
   * A method that is called when the user selects the first option in the welcome menu.
   */
  public void registerAccount() {
    Card card = cardGenerator.generateCredentials();
    accountDao.save(new Account(card, 0));
    System.out.printf("""
                      Your card has been created
                      Your card number:
                      %s
                      Your card PIN:
                      %s""", card.number(), card.pin());

    System.out.print("\n".repeat(2));
  }

  /**
   * Log into an account. <br/>
   * A method that is called when the user selects the second option in the welcome menu.
   */
  public void logIntoAccount() {
    System.out.println("\nEnter your card number:");
    String inCardNum = scanner.next();
    System.out.println("Enter your PIN:");
    String inCardPIN = scanner.next();

    accountDao
        .get(inCardNum, inCardPIN)
        .ifPresentOrElse(this::logInSuccess, () -> System.out.println("\nWrong card number or PIN!\n"));
  }

  /**
   * A method that is called when the user logs in successfully. <br/>
   * Show the main menu, get the user's input, and if the input is 0, stop the application, if the input is 1, show the
   * balance, if the input is 2, make a deposit, if the input is 3, make a withdrawal, if the input is 4, log out,
   * otherwise,
   * show an error message.
   */
  public void logInSuccess(Account account) {
    currentAccount = account;
    System.out.println("\nYou have successfully logged in!\n");
    accountMenu();
  }

  /**
   * Show the main menu. <br/>
   * A method that is called when the user selects the first option in the welcome menu.
   */
  public void accountMenu() {
    while (currentAccount != null) {
      System.out.println("""
                         1. Balance
                         2. Add income
                         3. Do transfer
                         4. Close account
                         5. Log out
                         0. Exit""");

      String input = scanner.next();
      System.out.println();

      switch (input) {
        case "0" -> stopApplication();
        case "1" -> getBalance();
        case "2" -> addIncome();
        case "3" -> doTransfer();
        case "4" -> closeAccount();
        case "5" -> logOut();
        default -> System.out.println("You've entered invalid menu item.\n");
      }
    }
  }

  /**
   * Get the balance. <br/>
   * A method that is called when the user selects the first option in the main menu.
   */
  public void getBalance() {
    System.out.printf("Balance: %d\n", currentAccount.getBalance());
  }

  /**
   * Add income. <br/>
   * A method that is called when the user selects the second option in the main menu.
   */
  public void addIncome() {
    System.out.println("Enter income:");

    try {
      int income = Integer.parseInt(scanner.next());

      if (income < 0 || income == 0) {
        System.out.println("Money can't be a zero or a negative number!\n");
        return;
      }

      updateAccount(currentAccount.getCard().number(), income);
      System.out.println("Income was added!\n");
    } catch (NumberFormatException e) {
      System.out.println("You should enter the amount of money you want to add to your balance!\n");
      addIncome();
    }
  }

  /**
   * Do transfer. <br/>
   * A method that is called when the user selects the third option in the main menu.
   */
  public void doTransfer() {
    System.out.println("Transfer\n" + "Enter card number:");
    String recipientCardNumber = scanner.next();

    if (!recipientCredentialsValidation(recipientCardNumber)) {
      return;
    }

    System.out.println("Enter how much money you want to transfer:");

    try {
      int moneyToTransfer = Integer.parseInt(scanner.next());

      if (moneyToTransfer < 0 || moneyToTransfer == 0) {
        System.out.println("Money can't be a zero or a negative number!\n");
        return;
      }

      if (moneyToTransfer > currentAccount.getBalance()) {
        System.out.println("Not enough money!\n");
        return;
      }

      accountDao.update(recipientCardNumber, moneyToTransfer);

      int negateRemittance = -moneyToTransfer;
      updateAccount(currentAccount.getCard().number(), negateRemittance);

      System.out.println("Success!\n");
    } catch (NumberFormatException e) {
      System.out.println("You should enter the amount of money you want to transfer!\n");
      doTransfer();
    }
  }

  /**
   * Close account. <br/>
   * A method that is called when the user selects the fourth option in the main menu.
   */
  public void closeAccount() {
    accountDao.delete(currentAccount);
    System.out.println("The account has been closed!\n");
    currentAccount = null;
  }

  /**
   * Log out. <br/>
   * A method that is called when the user selects the fifth option in the main menu.
   */
  public void logOut() {
    currentAccount = null;
    System.out.println("You have successfully logged out!\n");
  }

  /**
   * Update account. <br/>
   * Updating the account balance.
   */
  public void updateAccount(String cardNumber, int transferredMoney) {
    accountDao.update(cardNumber, transferredMoney);
    accountDao
        .get(cardNumber, currentAccount.getCard().pin())
        .ifPresent(account -> currentAccount.setBalance(currentAccount.getBalance() + transferredMoney));
  }

  /**
   * Validate the recipient card number. <br/>
   * A method that is called when the user enters the card number of the recipient.
   */
  public boolean cardNumberValidation(String cardNumber) {
    if (cardNumber.length() != 16) {
      return false;
    }

    int bin, accountIdentifier, checkSum;

    try {
      bin = Integer.parseInt(cardNumber.substring(0, 6));
      accountIdentifier = Integer.parseInt(cardNumber.substring(6, 15));
      checkSum = Integer.parseInt(cardNumber.substring(15));
    } catch (NumberFormatException e) {
      return false;
    }

    return cardGenerator.luhnAlgorithm(bin, accountIdentifier) == checkSum;
  }

  /**
   * Validate the recipient credentials. <br/>
   * A method that is called when the user enters the credentials of the recipient.
   */
  public boolean recipientCredentialsValidation(String recipientCardNumber) {
    if (recipientCardNumber.equals(currentAccount.getCard().number())) {
      System.out.println("You can't transfer money to the same account!\n");
      return false;
    }

    if (!cardNumberValidation(recipientCardNumber)) {
      System.out.println("Probably you made a mistake in the card number. Please try again!\n");
      return false;
    }

    if (!accountDao.contains(recipientCardNumber)) {
      System.out.println("Such a card does not exist.\n");
      return false;
    }

    return true;
  }

  /**
   * Stop the application. <br/>
   * A method that is called when the user selects the last option in the main menu.
   */
  public void stopApplication() {
    scanner.close();
    accountDao.getDbConfiguration().closeConnection();
    System.out.println("Bye!");
    System.exit(0);
  }

}
