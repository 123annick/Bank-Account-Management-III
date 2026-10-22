package services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import models.Account;
import models.Transaction;
import models.TransactionType;
import models.exceptions.BankException;
import models.exceptions.InvalidAccountException;
import utils.IdGenerator;

public class TransactionManager implements TransactionHistory {
  private final List<Transaction> transactions = new ArrayList<>();
  private final IdGenerator transactionIds = new IdGenerator("TXN");

  /**
   * Deposits money into an account and records the transaction.
   *
   * @param account the account receiving the money
   * @param amount the amount to deposit
   * @return the recorded transaction
   * @throws BankException when the amount is invalid
   */
  public Transaction deposit(Account account, double amount) throws BankException {
    account.deposit(amount);
    return applyTransaction(account, TransactionType.DEPOSIT, amount);
  }

  /**
   * Withdraws money from an account and records the transaction.
   *
   * @param account the account giving the money
   * @param amount the amount to withdraw
   * @return the recorded transaction
   * @throws BankException when the amount is invalid or a balance rule is broken
   */
  public Transaction withdraw(Account account, double amount) throws BankException {
    account.withdraw(amount);
    return applyTransaction(account, TransactionType.WITHDRAWAL, amount);
  }

  /**
   * Moves money from one account to another and records both sides as one step.
   *
   * @param source the account giving the money
   * @param target the account receiving the money
   * @param amount the amount to move
   * @throws BankException when the transfer breaks any rule; no balance changes in that case
   */
  public void transfer(Account source, Account target, double amount)
      throws BankException {
    validateTransfer(source, target, amount);
    source.withdraw(amount);
    target.deposit(amount);
    applyTransaction(source, TransactionType.TRANSFER_OUT, amount);
    applyTransaction(target, TransactionType.TRANSFER_IN, amount);
  }

  /**
   * Checks a transfer without moving any money.
   *
   * @param source the account giving the money
   * @param target the account receiving the money
   * @param amount the amount to move
   * @throws BankException when the accounts are the same or the source cannot pay
   */
  public void validateTransfer(Account source, Account target, double amount)
      throws BankException {
    if (source.getAccountNumber().equals(target.getAccountNumber())) {
      throw new InvalidAccountException("Cannot transfer money to the same account.");
    }
    source.checkWithdrawal(amount);
  }

  /**
   * Returns the transactions of one account in the order they were recorded.
   *
   * @param accountNumber the account to look for
   * @return a new list with only that account's transactions
   */
  @Override
  public List<Transaction> getTransactionsFor(String accountNumber) {
    return transactions.stream()
        .filter(transaction -> transaction.getAccountNumber().equalsIgnoreCase(accountNumber))
        .collect(Collectors.toList());
  }

  /**
   * Returns every recorded transaction in the order they were recorded.
   *
   * @return a new list with all transactions
   */
  public List<Transaction> getAllTransactions() {
    return new ArrayList<>(transactions);
  }

  /**
   * Returns how many transactions are recorded for all accounts.
   *
   * @return the number of transactions
   */
  public int getTransactionCount() {
    return transactions.size();
  }

  /**
   * Returns the id the next recorded transaction will get.
   *
   * @return the upcoming transaction id
   */
  public String peekNextTransactionId() {
    return transactionIds.peek();
  }

  /**
   * Removes every recorded transaction and keeps the given ones instead, for example after
   * loading from a file. New transaction ids continue after the highest loaded id.
   *
   * @param loaded the transactions to keep
   */
  public void replaceAll(Collection<Transaction> loaded) {
    transactions.clear();
    transactions.addAll(loaded);
    loaded.forEach(transaction -> transactionIds.skipPast(transaction.getTransactionId()));
  }

  private Transaction applyTransaction(Account account, TransactionType type, double amount) {
    Transaction transaction = new Transaction(transactionIds.next(),
        account.getAccountNumber(), type, amount, account.getBalance(),
        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    transactions.add(transaction);
    return transaction;
  }
}
