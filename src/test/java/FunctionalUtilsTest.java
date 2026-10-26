import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import models.Transaction;
import models.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.FunctionalUtils;

@ExtendWith(ConsoleTestLogger.class)
class FunctionalUtilsTest {
  private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 9, 0);

  private List<Transaction> transactions;

  @BeforeEach
  void setUp() {
    double[] amounts = {120, 50, 900, 15, 300, 75, 640, 10, 205, 450};
    transactions = new ArrayList<>();
    IntStream.range(0, amounts.length).forEach(i -> transactions.add(new Transaction(
        String.format("TXN%03d", i + 1), "ACC001",
        i % 3 == 0 ? TransactionType.WITHDRAWAL : TransactionType.DEPOSIT,
        amounts[i], 1000, START.plusHours(i))));
  }

  @Test
  void sortsTenTransactionsByAmountLargestFirst() {
    List<Transaction> sorted = FunctionalUtils.filterAndSort(transactions, t -> true,
        FunctionalUtils.BY_AMOUNT_LARGEST_FIRST);
    List<Double> amounts = sorted.stream().map(Transaction::getAmount)
        .collect(Collectors.toList());
    assertEquals(List.of(900.0, 640.0, 450.0, 300.0, 205.0, 120.0, 75.0, 50.0, 15.0, 10.0),
        amounts);
  }

  @Test
  void sortsTenTransactionsByDateNewestFirst() {
    List<Transaction> sorted = FunctionalUtils.filterAndSort(transactions, t -> true,
        FunctionalUtils.BY_DATE_NEWEST_FIRST);
    assertEquals("TXN010", sorted.get(0).getTransactionId());
    assertEquals("TXN001", sorted.get(9).getTransactionId());
  }

  @Test
  void sameTimestampPutsLaterRecordFirst() {
    Transaction first = TestData.depositAt("TXN001", START);
    Transaction second = TestData.depositAt("TXN002", START);
    List<Transaction> sorted = FunctionalUtils.filterAndSort(List.of(first, second), t -> true,
        FunctionalUtils.BY_DATE_NEWEST_FIRST);
    assertSame(second, sorted.get(0));
  }

  @Test
  void reduceAddsUpDepositsCorrectly() {
    double expected = 50 + 900 + 300 + 75 + 10 + 205;
    assertEquals(expected, FunctionalUtils.sumAmounts(transactions, FunctionalUtils.IS_CREDIT));
  }

  @Test
  void reduceAddsUpWithdrawalsCorrectly() {
    double expected = 120 + 15 + 640 + 450;
    assertEquals(expected, FunctionalUtils.sumAmounts(transactions, FunctionalUtils.IS_DEBIT));
  }

  @Test
  void filterKeepsOnlyOneAccount() {
    transactions.add(new Transaction("TXN011", "ACC002", TransactionType.DEPOSIT, 5, 5, START));
    List<Transaction> kept = FunctionalUtils.filterAndSort(transactions,
        FunctionalUtils.belongsTo("acc002"), FunctionalUtils.BY_DATE_NEWEST_FIRST);
    assertEquals(1, kept.size());
    assertTrue(kept.stream().allMatch(t -> t.getAccountNumber().equals("ACC002")));
  }

  @Test
  void originalListIsNotChanged() {
    FunctionalUtils.filterAndSort(transactions, t -> true, FunctionalUtils.BY_AMOUNT_LARGEST_FIRST);
    assertEquals("TXN001", transactions.get(0).getTransactionId());
  }
}
