import java.util.Random;
import java.util.Scanner;

import org.sqlite.SQLiteDataSource;

import configuration.DBConfiguration;
import dao.AccountDao;

public class Main {

  public static void main(String[] args) {
    if (!(args.length == 2 && args[0].equals("-fileName"))) {
      throw new IllegalArgumentException("""
                                         You should pass the -fileName as the first argument
                                         and SQLite db name (.s3db extension) as the second one!""");
    }

    SQLiteDataSource dataSource = new SQLiteDataSource();
    DBConfiguration dbConfiguration = new DBConfiguration(dataSource, args[1]);
    AccountDao accountDao = new AccountDao(dbConfiguration);

    Random random = new Random();
    CardGenerator cardGenerator = new CardGenerator(random);

    Scanner scanner = new Scanner(System.in);

    BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);
    bankingSystem.showMenu();
  }

}
