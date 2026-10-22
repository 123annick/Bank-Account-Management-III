package services;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import models.Account;
import models.Transaction;
import models.exceptions.BankException;
import utils.ConcurrencyUtils;
import utils.FormatUtils;

public class TransactionSimulator {
  private static final double BATCH_DEPOSIT = 25;
  private static final int BATCH_SIZE = 10;

  private final TransactionManager transactionManager;
  private final Consumer<String> log;

  /**
   * Creates a simulator that runs transactions through the given manager.
   *
   * @param transactionManager carries out and records the transactions safely
   * @param log receives one line of text for every thread action
   */
  public TransactionSimulator(TransactionManager transactionManager, Consumer<String> log) {
    this.transactionManager = transactionManager;
    this.log = log;
  }

  /**
   * Runs three threads at the same time (two deposits and one withdrawal), then a batch of
   * deposits on a parallel stream, and reports whether the final balance is consistent.
   *
   * @param account the account all threads work on
   * @return the balances before and after, and the recorded changes
   * @throws InterruptedException when waiting for the threads is interrupted
   */
  public SimulationReport run(Account account) throws InterruptedException {
    double startingBalance = account.getBalance();
    int recordedBefore = transactionManager.getTransactionsFor(account.getAccountNumber()).size();
    ConcurrencyUtils.runInParallelThreads(List.of(
        () -> attempt("Depositing", 500, " to ", account,
            () -> transactionManager.deposit(account, 500)),
        () -> attempt("Depositing", 300, " to ", account,
            () -> transactionManager.deposit(account, 300)),
        () -> attempt("Withdrawing", 200, " from ", account,
            () -> transactionManager.withdraw(account, 200))));
    IntStream.rangeClosed(1, BATCH_SIZE).parallel().forEach(i -> attempt("Depositing",
        BATCH_DEPOSIT, " to ", account,
        () -> transactionManager.deposit(account, BATCH_DEPOSIT)));
    return buildReport(account, startingBalance, recordedBefore);
  }

  private void attempt(String action, double amount, String direction, Account account,
      BankOperation operation) {
    String thread = ConcurrencyUtils.currentThreadName();
    log.accept(thread + ": " + action + " " + FormatUtils.currency(amount) + direction
        + account.getAccountNumber());
    try {
      operation.run();
    } catch (BankException e) {
      log.accept(thread + ": ❌ " + e.getMessage());
    }
  }

  private SimulationReport buildReport(Account account, double startingBalance,
      int recordedBefore) {
    List<Transaction> history = transactionManager.getTransactionsFor(account.getAccountNumber());
    List<Transaction> recorded = history.subList(recordedBefore, history.size());
    double change = recorded.stream().mapToDouble(Transaction::getSignedAmount).sum();
    return new SimulationReport(account.getAccountNumber(), startingBalance,
        account.getBalance(), change, recorded.size());
  }
}
