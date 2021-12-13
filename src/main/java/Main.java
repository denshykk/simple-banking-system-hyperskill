import dao.AccountDao;
import dao.DBCreator;

public class Main {

    public static void main(String[] args) {
        try {
            if (args[0].equals("-fileName")) {
                DBCreator dbCreator = new DBCreator(args[1]);
                AccountDao accountDao = new AccountDao(dbCreator);

                BankingSystem bankingSystem = new BankingSystem(accountDao);
                bankingSystem.showMenu();
            } else {
                throw new IllegalArgumentException();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            System.out.println("""
                    You should pass the -fileName as the first argument
                    and SQLite db name (.s3db extension) as the second one!""");
        }
    }
}
