import dao.AccountDao;
import model.Account;
import model.Card;

import java.util.Scanner;

class BankingSystem {

    private final AccountDao accountDao;
    private final CardGenerator cardGenerator;
    private final Scanner scanner;
    private Account currentAccount;

    public BankingSystem(AccountDao accountDao) {
        this.accountDao = accountDao;
        cardGenerator = new CardGenerator();
        scanner = new Scanner(System.in);
    }

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

    private void welcomeMenu() {
        System.out.println("""
                1. Create an account
                2. Log into account
                0. Exit""");
    }

    private void registerAccount() {
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

    private void logIntoAccount() {
        System.out.println("\nEnter your card number:");
        String inCardNum = scanner.next();
        System.out.println("Enter your PIN:");
        String inCardPIN = scanner.next();

        accountDao.get(inCardNum, inCardPIN).ifPresentOrElse(account -> {
                    currentAccount = account;
                    System.out.println("\nYou have successfully logged in!\n");
                    accountMenu();
                },
                () -> System.out.println("\nWrong card number or PIN!\n"));
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

    private void getBalance() {
        System.out.printf("Balance: %d\n", currentAccount.getBalance());
    }

    private void addIncome() {
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

    private void doTransfer() {
        System.out.println("Transfer\n" +
                           "Enter card number:");
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

            int negateTransferredMoney = -moneyToTransfer;
            updateAccount(currentAccount.getCard().number(), negateTransferredMoney);

            System.out.println("Success!\n");
        } catch (NumberFormatException e) {
            System.out.println("You should enter the amount of money you want to transfer!\n");
            doTransfer();
        }
    }


    private void closeAccount() {
        accountDao.delete(currentAccount);
        System.out.println("The account has been closed!\n");
        currentAccount = null;
    }

    private void logOut() {
        currentAccount = null;
        System.out.println("You have successfully logged out!\n");
    }

    private void updateAccount(String cardNumber, int transferredMoney) {
        accountDao.update(cardNumber, transferredMoney);
        accountDao.get(cardNumber, currentAccount.getCard().pin())
                .ifPresent(account -> currentAccount.setBalance(currentAccount.getBalance() + transferredMoney));
    }

    private boolean cardNumberValidation(String cardNumber) {
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

    private boolean recipientCredentialsValidation(String recipientCardNumber) {
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

    private void stopApplication() {
        scanner.close();
        accountDao.dbConfiguration().close();
        System.out.println("Bye!");
        System.exit(0);
    }
}
