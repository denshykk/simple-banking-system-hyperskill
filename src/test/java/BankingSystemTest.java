import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ginsberg.junit.exit.ExpectSystemExit;
import configuration.DBConfiguration;
import dao.AccountDao;
import lombok.SneakyThrows;
import model.Account;
import model.Card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankingSystemTest {

  private static final String CARD_NUMBER = "4000004938320896";
  private static final String PIN         = "1234";

  @Mock
  private AccountDao      accountDao;
  @Mock
  private CardGenerator   cardGenerator;
  @Mock
  private Account         currentAccount;
  @Mock
  private DBConfiguration dbConfiguration;
  @InjectMocks
  private BankingSystem   bankingSystem;

  @Test
  @ExpectSystemExit
  void testShowMenu() {
    final Scanner scanner = new Scanner(new ByteArrayInputStream("0\n".getBytes()));
    BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);
    when(accountDao.getDbConfiguration()).thenReturn(dbConfiguration);
    doNothing().when(dbConfiguration).closeConnection();

    bankingSystem.showMenu();

    verify(accountDao).getDbConfiguration();
    verify(dbConfiguration).closeConnection();
  }

  @Test
  void testWelcomeMenu() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    bankingSystem.welcomeMenu();

    assertEquals("""
                 1. Create an account
                 2. Log into account
                 0. Exit
                 """, out.toString());
  }

  @Test
  void testRegisterAccount() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    when(cardGenerator.generateCredentials()).thenReturn(new Card(CARD_NUMBER, PIN));
    doNothing().when(accountDao).save(any());

    bankingSystem.registerAccount();

    assertEquals("""
                 Your card has been created
                 Your card number:
                 4000004938320896
                 Your card PIN:
                 1234
                                  
                 """, out.toString());
  }

  @Test
  void testLogIntoAccount() {
    final Scanner scanner = new Scanner(new ByteArrayInputStream(String.format("%s %s ", CARD_NUMBER, PIN).getBytes()));
    BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    when(accountDao.get(CARD_NUMBER, PIN)).thenReturn(Optional.empty());

    bankingSystem.logIntoAccount();

    assertEquals("""
                                  
                 Enter your card number:
                 Enter your PIN:
                                  
                 Wrong card number or PIN!
                                  
                 """, out.toString());
  }

  @Test
  @SneakyThrows
  void testAddIncome() {
    final Card card = new Card(CARD_NUMBER, PIN);
    final Scanner scanner = new Scanner(new ByteArrayInputStream(String.format("%s ", "111").getBytes()));
    final BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);
    final Field field = bankingSystem.getClass().getDeclaredField("currentAccount");
    field.setAccessible(true);
    field.set(bankingSystem, currentAccount);

    when(currentAccount.getCard()).thenReturn(card);
    doNothing().when(accountDao).update(CARD_NUMBER, 111);

    bankingSystem.addIncome();

    verify(accountDao).update(CARD_NUMBER, 111);
  }

  @Test
  @SneakyThrows
  void testDoTransfer() {
    final Scanner scanner = new Scanner(new ByteArrayInputStream(String
        .format("%s %s", CARD_NUMBER, "111")
        .getBytes()));

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    final BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);
    final Card card = new Card("4000002454329010", PIN);

    final Field field = bankingSystem.getClass().getDeclaredField("currentAccount");
    field.setAccessible(true);
    field.set(bankingSystem, currentAccount);

    when(currentAccount.getBalance()).thenReturn(9999);
    when(cardGenerator.luhnAlgorithm(400000, 493832089)).thenReturn(6);
    when(accountDao.contains(CARD_NUMBER)).thenReturn(true);
    when(currentAccount.getCard()).thenReturn(card);

    bankingSystem.doTransfer();

    assertThat(out.toString()).contains("Success!");
  }

  @Test
  void testCloseAccount() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    doNothing().when(accountDao).delete(any());

    bankingSystem.closeAccount();

    assertEquals("""
                 The account has been closed!
                                  
                 """, out.toString());
  }

  @Test
  void testLogOut() {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    bankingSystem.logOut();

    assertEquals("""
                 You have successfully logged out!
                                                                    
                 """, out.toString());
  }

  @Test
  void testUpdateAccount() {
    final Card card = new Card(CARD_NUMBER, PIN);

    doNothing().when(accountDao).update(CARD_NUMBER, 9999);
    when(currentAccount.getCard()).thenReturn(card);
    when(currentAccount.getBalance()).thenReturn(123);
    when(accountDao.get(CARD_NUMBER, currentAccount.getCard().pin())).thenReturn(Optional.of(currentAccount));
    doNothing().when(currentAccount).setBalance(anyInt());

    bankingSystem.updateAccount(CARD_NUMBER, 9999);

    verify(accountDao).update(CARD_NUMBER, 9999);
    verify(currentAccount).setBalance(123 + 9999);
  }

  @Test
  void testCardNumberValidation() {
    when(cardGenerator.luhnAlgorithm(400000, 493832089)).thenReturn(6);

    final boolean result = bankingSystem.cardNumberValidation(CARD_NUMBER);

    assertTrue(result);

    verify(cardGenerator).luhnAlgorithm(400000, 493832089);
  }

  @Test
  void testRecipientCredentialsValidation() {
    final Card card = new Card("1111111111111111", "1111");

    when(cardGenerator.luhnAlgorithm(400000, 493832089)).thenReturn(6);
    when(currentAccount.getCard()).thenReturn(card);
    when(accountDao.contains(CARD_NUMBER)).thenReturn(true);

    final boolean result = bankingSystem.recipientCredentialsValidation(CARD_NUMBER);

    assertTrue(result);
  }

  @Test
  @ExpectSystemExit
  void testStopApplication() {
    final Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
    BankingSystem bankingSystem = new BankingSystem(accountDao, cardGenerator, scanner);

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));

    when(accountDao.getDbConfiguration()).thenReturn(dbConfiguration);
    doNothing().when(dbConfiguration).closeConnection();

    bankingSystem.stopApplication();

    assertEquals("Bye!", out.toString());
  }

}
