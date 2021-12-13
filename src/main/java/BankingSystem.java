import card.Card;
import card.CardGenerator;
import dao.Account;
import dao.AccountDao;
import dao.Dao;

import java.util.Scanner;

public class BankingSystem {

    private final Dao<Account> dao;
    private final CardGenerator cardGenerator;
    private final Scanner scanner;
    private Account currentAccount;

    public BankingSystem(String dbName) {
        dao = new AccountDao(dbName);
        cardGenerator = new CardGenerator();
        scanner = new Scanner(System.in);
    }

    public void showMenu() {
        while (true) {
            welcomeMenu();

            String input = scanner.next();

            switch (input) {
                case "0" -> System.exit(0);
                case "1" -> registerAccount();
                case "2" -> logIntoAccount();
                default -> System.out.println("You've entered invalid menu item.\n");
            }
        }
    }

    private void welcomeMenu() {
        System.out.println("""
                1. Create an account
                2. Log into account
                0. Exit""");
    }

    private void registerAccount() {
        Card card = cardGenerator.generateCredentials();
        dao.save(new Account(card, 0));
        System.out.printf("""
                Your card has been created
                Your card number:
                %s
                Your card PIN:
                %s""", card.number(), card.pin());

        System.out.println();
        System.out.println();
    }

    private void logIntoAccount() {
        System.out.println("\nEnter your card number:");
        String inputtedCardNumber = scanner.next();
        System.out.println("Enter your PIN:");
        String inputtedCardPIN = scanner.next();
        boolean isValid = accountValidation(inputtedCardNumber, inputtedCardPIN);

        if (isValid) {
            System.out.println("\nYou have successfully logged in!\n");
            dao.get(inputtedCardNumber, inputtedCardPIN).ifPresent(account -> currentAccount = account);
            accountMenu();
        } else {
            System.out.println("\nWrong card number or PIN!\n");
        }
    }

    private boolean accountValidation(String inputtedCardNumber, String inputtedCardPIN) {
        return dao.get(inputtedCardNumber, inputtedCardPIN).isPresent();
    }

    private void accountMenu() {
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
                case "0" -> {
                    dao.close();
                    System.out.println("Bye!");
                    System.exit(0);
                }
                case "1" -> getBalance();
                case "2" -> addIncome();
                case "3" -> doTransfer();
                case "4" -> closeAccount();
                case "5" -> logOut();
                default -> System.out.println("You've entered invalid menu item.\n");
            }
        }
    }

    private void getBalance() {
        System.out.printf("Balance: %d\n", currentAccount.balance());
    }

    private void addIncome() {
        System.out.println("Enter income:");
        int income = Integer.parseInt(scanner.next());
        dao.update(currentAccount.card().number(), income);
        dao.get(currentAccount.card().number(), currentAccount.card().pin()).ifPresent(account -> currentAccount = account);
        System.out.println("Income was added!\n");
    }

    private void doTransfer() {
        System.out.println("Transfer\n" +
                           "Enter card number:");
        String recipientCardNumber = scanner.next();

        if (recipientCardNumber.equals(currentAccount.card().number())) {
            System.out.println("You can't transfer money to the same account!\n");
            return;
        }

        if (!cardGenerator.isCorrectCardNumber(recipientCardNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!\n");
            return;
        }

        if (!dao.contains(recipientCardNumber)) {
            System.out.println("Such a card does not exist.\n");
            return;
        }

        System.out.println("Enter how much money you want to transfer:");
        int moneyToTransfer = Integer.parseInt(scanner.next());

        if (moneyToTransfer > currentAccount.balance()) {
            System.out.println("Not enough money!\n");
            return;
        }

        dao.update(currentAccount.card().number(), -moneyToTransfer);
        dao.update(recipientCardNumber, moneyToTransfer);
        dao.get(currentAccount.card().number(), currentAccount.card().pin()).ifPresent(account -> currentAccount = account);
        System.out.println("Success!\n");
    }

    private void closeAccount() {
        dao.delete(currentAccount);
        System.out.println("The account has been closed!\n");
        currentAccount = null;
    }

    private void logOut() {
        currentAccount = null;
        System.out.println("You have successfully logged out!\n");
    }
}
