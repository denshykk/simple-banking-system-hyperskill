package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This class represents an account.
 */
@Getter
@AllArgsConstructor
public class Account {

  private final Card card;
  @Setter
  private       int  balance;

}

