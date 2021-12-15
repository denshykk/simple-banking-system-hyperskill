package model;

public final class Account {

    private final Card card;
    private int balance;

    public Account(Card card, int balance) {
        this.card = card;
        this.balance = balance;
    }

    public Card getCard() {
        return card;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
