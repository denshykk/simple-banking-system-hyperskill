package dao;

import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(String cardNumber, String cardPIN);

    boolean contains(String cardNumber);

    void save(T t);

    void update(String cardNumber, int income);

    void delete(T t);

    void close();
}
