import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import models.Account;
import models.AccountType;
import models.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.AccountFactory;
import services.AccountManager;

@ExtendWith(ConsoleTestLogger.class)
class AccountManagerTest {
  private AccountManager accountManager;
  private AccountFactory accountFactory;

  @BeforeEach
  void setUp() {
    accountManager = new AccountManager();
    accountFactory = new AccountFactory();
  }

  @Test
  void factoryGeneratesSequentialIds() throws Exception {
    Account first = createRegularChecking("Ann", 100);
    Account second = createRegularChecking("Ben", 200);
    assertEquals("ACC001", first.getAccountNumber());
    assertEquals("ACC002", second.getAccountNumber());
    assertEquals("CUS002", second.getCustomer().getCustomerId());
  }

  @Test
  void findAccountIgnoresCaseAndSpaces() throws Exception {
    Account account = createRegularChecking("Ann", 100);
    accountManager.addAccount(account);
    assertSame(account, accountManager.findAccount("  acc001 "));
  }

  @Test
  void totalBalanceAddsAllAccounts() throws Exception {
    accountManager.addAccount(createRegularChecking("Ann", 100.25));
    accountManager.addAccount(createRegularChecking("Ben", 200.50));
    assertEquals(300.75, accountManager.getTotalBalance(), 0.001);
    assertEquals(2, accountManager.getAccountCount());
  }

  @Test
  void getAllAccountsSortsByAccountNumber() throws Exception {
    Account first = createRegularChecking("Ann", 100);
    Account second = createRegularChecking("Ben", 200);
    Account third = createRegularChecking("Cara", 300);
    accountManager.addAccount(third);
    accountManager.addAccount(second);
    accountManager.addAccount(first);
    List<Account> all = accountManager.getAllAccounts();
    assertEquals(3, all.size());
    assertSame(first, all.get(0));
    assertSame(second, all.get(1));
    assertSame(third, all.get(2));
  }

  @Test
  void findAccountsFiltersWithPredicate() throws Exception {
    accountManager.addAccount(createRegularChecking("Ann", 100));
    accountManager.addAccount(createRegularChecking("Ben", 5000));
    List<Account> rich = accountManager.findAccounts(account -> account.getBalance() > 1000);
    assertEquals(1, rich.size());
    assertEquals("Ben", rich.get(0).getCustomer().getName());
  }

  @Test
  void replaceAllSwapsTheStoredAccounts() throws Exception {
    accountManager.addAccount(createRegularChecking("Ann", 100));
    accountManager.replaceAll(List.of(TestData.savingsAccount(900)));
    assertEquals(1, accountManager.getAccountCount());
    assertEquals(900, accountManager.findAccount("ACC001").getBalance());
  }

  private Account createRegularChecking(String name, double deposit) throws Exception {
    return accountFactory.createAccount(TestData.customerDetails(name), CustomerType.REGULAR,
        AccountType.CHECKING, deposit);
  }
}
