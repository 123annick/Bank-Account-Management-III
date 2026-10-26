import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import models.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.StatementGenerator;
import services.TransactionManager;
import utils.FunctionalUtils;

@ExtendWith(ConsoleTestLogger.class)
class StatementGeneratorTest {
  private TransactionManager transactionManager;
  private StatementGenerator statementGenerator;
  private Account savings;

  @BeforeEach
  void setUp() {
    transactionManager = new TransactionManager();
    statementGenerator = new StatementGenerator(transactionManager);
    savings = TestData.savingsAccount(5250);
  }

  @Test
  void accountWithoutTransactionsShowsFriendlyMessage() {
    String statement = statementGenerator.generateStatement(savings);
    assertTrue(statement.contains("No transactions recorded for this account."));
  }

  @Test
  void statementListsNewestTransactionFirst() throws Exception {
    transactionManager.deposit(savings, 1500);
    transactionManager.withdraw(savings, 750);
    String statement = statementGenerator.generateStatement(savings);
    assertTrue(statement.indexOf("TXN002") < statement.indexOf("TXN001"));
  }

  @Test
  void statementShowsTotalsWithTwoDecimals() throws Exception {
    transactionManager.deposit(savings, 1500);
    transactionManager.withdraw(savings, 750);
    String statement = statementGenerator.generateStatement(savings);
    assertTrue(statement.contains("Total Deposits: $1,500.00"));
    assertTrue(statement.contains("Total Withdrawals: $750.00"));
    assertTrue(statement.contains("Net Change: +$750.00"));
  }

  @Test
  void transfersCountAsMoneyInAndOut() throws Exception {
    Account checking = TestData.checkingAccount(100);
    transactionManager.transfer(savings, checking, 250);
    assertEquals(250, statementGenerator.calculateTotalWithdrawals(
        transactionManager.getTransactionsFor("ACC001")));
    assertEquals(250, statementGenerator.calculateTotalDeposits(
        transactionManager.getTransactionsFor("ACC002")));
  }

  @Test
  void depositsOnlyViewHidesWithdrawalsAndSortsByAmount() throws Exception {
    transactionManager.deposit(savings, 100);
    transactionManager.withdraw(savings, 50);
    transactionManager.deposit(savings, 300);
    String statement = statementGenerator.generateStatement(savings, FunctionalUtils.IS_CREDIT,
        FunctionalUtils.BY_AMOUNT_LARGEST_FIRST);
    assertFalse(statement.contains("WITHDRAWAL"));
    assertTrue(statement.indexOf("TXN003") < statement.indexOf("TXN001"));
  }
}
