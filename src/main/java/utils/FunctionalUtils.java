package utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import models.Transaction;

public final class FunctionalUtils {
  public static final Comparator<Transaction> BY_DATE_NEWEST_FIRST =
      Comparator.comparing(Transaction::getTimestamp)
          .thenComparingInt(transaction -> IdGenerator.numberOf(transaction.getTransactionId()))
          .reversed();
  public static final Comparator<Transaction> BY_AMOUNT_LARGEST_FIRST =
      Comparator.comparingDouble(Transaction::getAmount).reversed();

  public static final Predicate<Transaction> IS_CREDIT =
      transaction -> transaction.getType().isCredit();
  public static final Predicate<Transaction> IS_DEBIT = IS_CREDIT.negate();

  private FunctionalUtils() {
  }

  /**
   * Builds a rule that is true for transactions of one account, ignoring upper or lower case.
   *
   * @param accountNumber the account to keep
   * @return a predicate that selects that account's transactions
   */
  public static Predicate<Transaction> belongsTo(String accountNumber) {
    return transaction -> transaction.getAccountNumber().equalsIgnoreCase(accountNumber);
  }

  /**
   * Keeps the items that pass a rule and puts them in the given order.
   *
   * @param items the items to process
   * @param rule which items to keep
   * @param order how to sort the kept items
   * @param <T> the type of item
   * @return a new list with the kept items in order
   */
  public static <T> List<T> filterAndSort(Collection<T> items, Predicate<? super T> rule,
      Comparator<? super T> order) {
    return items.stream().filter(rule).sorted(order).collect(Collectors.toList());
  }

  /**
   * Adds up the amounts of the transactions that pass a rule, using reduce.
   *
   * @param transactions the transactions to add up
   * @param rule which transactions to include
   * @return the total amount
   */
  public static double sumAmounts(Collection<Transaction> transactions,
      Predicate<Transaction> rule) {
    return transactions.stream()
        .filter(rule)
        .map(Transaction::getAmount)
        .reduce(0.0, Double::sum);
  }
}
