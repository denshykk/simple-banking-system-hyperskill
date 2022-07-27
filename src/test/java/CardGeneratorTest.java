import java.util.Random;

import org.junit.jupiter.api.Test;

import model.Card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CardGeneratorTest {

  private final CardGenerator cardGenerator = new CardGenerator(new Random());

  @Test
  void testGenerateCredentials() {
    final Card card = cardGenerator.generateCredentials();

    assertNotNull(card);
    assertNotNull(card.number());
    assertNotNull(card.pin());

    assertEquals(16, card.number().length());
    assertEquals(4, card.pin().length());
  }

  @Test
  void testGenerateCard() {
    final String cardNumber = cardGenerator.generateCard();

    assertNotNull(cardNumber);
    assertEquals(16, cardNumber.length());
  }

  @Test
  void testGeneratePIN() {
    final String pin = cardGenerator.generatePIN();

    assertNotNull(pin);
    assertEquals(4, pin.length());
  }

  @Test
  void testLuhnAlgorithm() {
    final int bin = 400_000;
    final int accountIdentifier = 493832089;

    final int checkSum = cardGenerator.luhnAlgorithm(bin, accountIdentifier);

    assertEquals(6, checkSum);
  }

}
