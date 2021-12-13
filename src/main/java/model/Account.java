package model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Account) obj;
        return Objects.equals(this.card, that.card) &&
               this.balance == that.balance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(card, balance);
    }

    @Override
    public String toString() {
        return "Account[" +
               "card=" + card + ", " +
               "balance=" + balance + ']';
    }
}
