package services;

import models.exceptions.BankException;

@FunctionalInterface
public interface BankOperation {

  /**
   * Runs one banking action, such as a deposit, that may fail with a bank error.
   *
   * @throws BankException when the action breaks a bank rule
   */
  void run() throws BankException;
}
