package services;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import models.Account;
import models.Transaction;
import utils.FormatUtils;
import utils.FunctionalUtils;

public class StatementGenerator {
  private static final String SEPARATOR = "─".repeat(72);

  private final TransactionHistory transactionHistory;

  /**
   * Creates a generator that reads transactions from the given history.
   *
   * @param transactionHistory where the transactions are read from
   */
  public StatementGenerator(TransactionHistory transactionHistory) {
    this.transactionHistory = transactionHistory;
  }

  /**
   * Builds a full statement with every transaction, newest first.
   *
   * @param account the account to describe
   * @return the statement text, ready to print
   */
  public String generateStatement(Account account) {
    return generateStatement(account, transaction -> true, FunctionalUtils.BY_DATE_NEWEST_FIRST);
  }

  /**
   * Builds a statement with only the transactions that pass a rule, in the given order.
   *
   * @param account the account to describe
   * @param rule which transactions to show
   * @param order how to sort the shown transactions
   * @return the statement text, ready to print
   */
  public String generateStatement(Account account, Predicate<Transaction> rule,
      Comparator<Transaction> order) {
    List<Transaction> shown = FunctionalUtils.filterAndSort(
        transactionHistory.getTransactionsFor(account.getAccountNumber()), rule, order);
    StringBuilder statement = new StringBuilder();
    appendHeader(statement, account);
    if (shown.isEmpty()) {
      statement.append("No transactions recorded for this account.\n");
      return statement.toString();
    }
    shown.forEach(transaction -> statement.append(formatRow(transaction)));
    statement.append(SEPARATOR).append("\n");
    appendSummary(statement, shown);
    return statement.toString();
  }

  /**
   * Adds up all money that came into the account, using reduce.
   *
   * @param transactions the transactions to add up
   * @return the total of deposits and transfers in
   */
  public double calculateTotalDeposits(List<Transaction> transactions) {
    return FunctionalUtils.sumAmounts(transactions, FunctionalUtils.IS_CREDIT);
  }

  /**
   * Adds up all money that left the account, using reduce.
   *
   * @param transactions the transactions to add up
   * @return the total of withdrawals and transfers out
   */
  public double calculateTotalWithdrawals(List<Transaction> transactions) {
    return FunctionalUtils.sumAmounts(transactions, FunctionalUtils.IS_DEBIT);
  }

  private void appendHeader(StringBuilder statement, Account account) {
    statement.append(String.format("Account: %s - %s (%s)%n", account.getAccountNumber(),
        account.getCustomer().getName(), account.getAccountType().getDisplayName()));
    statement.append("Current Balance: ")
        .append(FormatUtils.currency(account.getBalance())).append("\n\n");
    statement.append("Transactions:\n").append(SEPARATOR).append("\n");
  }

  private String formatRow(Transaction transaction) {
    return String.format("%-7s | %-19s | %-12s | %-12s | %s%n",
        transaction.getTransactionId(),
        FormatUtils.dateTime(transaction.getTimestamp()),
        transaction.getType(),
        FormatUtils.signedCurrency(transaction.getSignedAmount()),
        FormatUtils.currency(transaction.getBalanceAfter()));
  }

  private void appendSummary(StringBuilder statement, List<Transaction> transactions) {
    double deposits = calculateTotalDeposits(transactions);
    double withdrawals = calculateTotalWithdrawals(transactions);
    statement.append("Total Transactions: ").append(transactions.size()).append("\n");
    statement.append("Total Deposits: ").append(FormatUtils.currency(deposits)).append("\n");
    statement.append("Total Withdrawals: ")
        .append(FormatUtils.currency(withdrawals)).append("\n");
    statement.append("Net Change: ")
        .append(FormatUtils.signedCurrency(deposits - withdrawals)).append("\n");
  }
}
