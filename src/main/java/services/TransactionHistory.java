package services;

import java.util.List;
import models.Transaction;

public interface TransactionHistory {

  /**
   * Returns the transactions of one account in the order they were recorded.
   *
   * @param accountNumber the account to look for
   * @return a new list with only that account's transactions
   */
  List<Transaction> getTransactionsFor(String accountNumber);
}
