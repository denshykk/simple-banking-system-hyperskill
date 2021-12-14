import model.Card;

import java.util.Random;

public class CardGenerator {

    private final Random random;

    public CardGenerator() {
        random = new Random();
    }

    public Card generateCredentials() {
        return new Card(generateCard(), generatePIN());
    }

    private String generateCard() {
        final int bin = 400_000;
        final int accountIdentifier = random.nextInt(900_000_000) + 100_000_000;
        final int checkSum = luhnAlgorithm(bin, accountIdentifier);
        return String.format("%d%d%d", bin, accountIdentifier, checkSum);
    }

    private String generatePIN() {
        return String.valueOf(1000 + random.nextInt(9000));
    }

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
