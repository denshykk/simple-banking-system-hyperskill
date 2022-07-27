import java.util.Random;

import lombok.RequiredArgsConstructor;
import model.Card;

@RequiredArgsConstructor
public class CardGenerator {

  private final Random random;

  /**
   * Generate a card and PIN, then return a new Card object with those values.
   *
   * @return A new Card object with a randomly generated card number and PIN.
   */
  public Card generateCredentials() {
    return new Card(generateCard(), generatePIN());
  }

  /**
   * It generates a random number, adds a checksum to it, and returns the result as a string
   *
   * @return A string of a credit card number.
   */
  public String generateCard() {
    final int bin = 400_000;
    final int accountIdentifier = random.nextInt(900_000_000) + 100_000_000;
    final int checkSum = luhnAlgorithm(bin, accountIdentifier);
    return String.format("%d%d%d", bin, accountIdentifier, checkSum);
  }

  /**
   * It generates a random number between 1000 and 9999, converts it to a string, and returns it
   *
   * @return A random 4 digit number
   */
  public String generatePIN() {
    return String.valueOf(1000 + random.nextInt(9000));
  }

  /**
   * > The Luhn algorithm is a simple checksum formula used to validate a variety of identification numbers, such as
   * credit card numbers, IMEI numbers, National Provider Identifier numbers in the United States, Canadian Social
   * Insurance Numbers, Israel ID Numbers, South African ID Numbers, Greek Social Security Numbers (ΑΜΚΑ), and survey
   * codes appearing on McDonald's, Taco Bell, and Tractor Supply Co. receipts
   *
   * @param bin
   *     The first 6 digits of the card number.
   * @param accountIdentifier
   *     This is the account number that you want to generate the card number for.
   *
   * @return The last digit of the card number.
   */
  public int luhnAlgorithm(int bin, int accountIdentifier) {
    String cardNumbersWithoutChecksum = String.format("%d%d", bin, accountIdentifier);
    int sum = 0;

    for (int i = 0; i < cardNumbersWithoutChecksum.length(); i++) {

      int digit = Integer.parseInt(cardNumbersWithoutChecksum.substring(i, i + 1));

      if (i % 2 == 0) {
        digit *= 2;
      }

      digit = (digit % 10) + (digit / 10);
      sum += digit;
    }

    return (10 - (sum % 10)) % 10;
  }

}
