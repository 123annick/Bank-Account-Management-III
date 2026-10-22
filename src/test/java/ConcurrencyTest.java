import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import models.Account;
import models.exceptions.BankException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.SimulationReport;
import services.TransactionManager;
import services.TransactionSimulator;
import utils.ConcurrencyUtils;

@ExtendWith(ConsoleTestLogger.class)
class ConcurrencyTest {
  private TransactionManager manager;
  private Account savings;

  @BeforeEach
  void setUp() {
    manager = new TransactionManager();
    savings = TestData.savingsAccount(1000);
  }

  @Test
  void manyThreadsDepositingLoseNoMoney() throws Exception {
    List<Runnable> tasks = new ArrayList<>();
    IntStream.range(0, 20).forEach(i -> tasks.add(() -> depositManyTimes(100)));
    ConcurrencyUtils.runInParallelThreads(tasks);
    assertEquals(3000, savings.getBalance());
    assertEquals(2000, manager.getTransactionCount());
  }

  @Test
  void parallelStreamDepositsAreThreadSafe() {
    IntStream.rangeClosed(1, 500).parallel().forEach(i -> depositManyTimes(1));
    assertEquals(1500, savings.getBalance());
    assertEquals(500, manager.getTransactionCount());
  }

  @Test
  void concurrentWithdrawalsNeverBreakMinimumBalance() throws Exception {
    List<Runnable> tasks = new ArrayList<>();
    IntStream.range(0, 10).forEach(i -> tasks.add(this::tryWithdrawHundred));
    ConcurrencyUtils.runInParallelThreads(tasks);
    assertEquals(500, savings.getBalance());
    assertEquals(5, manager.getTransactionCount());
  }

  @Test
  void simulatorReportsConsistentBalance() throws Exception {
    Account account = TestData.savingsAccount(6000);
    List<String> log = java.util.Collections.synchronizedList(new ArrayList<>());
    SimulationReport report = new TransactionSimulator(manager, log::add).run(account);
    assertTrue(report.isConsistent());
    assertEquals(6850, report.finalBalance());
    assertEquals(13, report.recordedTransactions());
    assertEquals(13, log.size());
  }

  private void depositManyTimes(int times) {
    IntStream.range(0, times).forEach(i -> {
      try {
        manager.deposit(savings, 1);
      } catch (BankException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  private void tryWithdrawHundred() {
    try {
      manager.withdraw(savings, 100);
    } catch (BankException e) {
      return;
    }
  }
}
