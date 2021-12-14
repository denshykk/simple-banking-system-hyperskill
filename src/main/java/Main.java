import dao.AccountDao;
import dao.DBConfiguration;

public class Main {

    public static void main(String[] args) {
        if (args[0].equals("-fileName") && args.length == 2) {
            DBConfiguration dbConfiguration = new DBConfiguration(args[1]);
            AccountDao accountDao = new AccountDao(dbConfiguration);

            BankingSystem bankingSystem = new BankingSystem(accountDao);
            bankingSystem.showMenu();
        } else {
            throw new IllegalArgumentException("""
                    You should pass the -fileName as the first argument
                    and SQLite db name (.s3db extension) as the second one!""");
        }
    }
}
