import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import models.Account;
import models.AccountType;
import models.CustomerType;
import models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import services.AccountFactory;
import services.FilePersistenceService;
import services.LoadResult;
import services.RecordMapper;
import services.TransactionManager;

@ExtendWith(ConsoleTestLogger.class)
class FilePersistenceServiceTest {
  @TempDir
  Path tempDir;

  private FilePersistenceService persistence;
  private AccountFactory factory;

  @BeforeEach
  void setUp() {
    factory = new AccountFactory();
    persistence = new FilePersistenceService(tempDir, new RecordMapper(factory));
  }

  @Test
  void roundTripKeepsTwoAccountsAndThreeTransactions() throws Exception {
    Account savings = factory.createAccount(TestData.customerDetails("Ann Uwase"),
        CustomerType.PREMIUM, AccountType.SAVINGS, 12000);
    Account checking = factory.createAccount(TestData.customerDetails("Ben Okoro"),
        CustomerType.REGULAR, AccountType.CHECKING, 300);
    TransactionManager manager = new TransactionManager();
    manager.deposit(savings, 500.5);
    manager.withdraw(checking, 800);
    manager.transfer(savings, checking, 100);
    persistence.saveAccounts(List.of(savings, checking));
    persistence.saveTransactions(manager.getAllTransactions());

    FilePersistenceService reloaded =
        new FilePersistenceService(tempDir, new RecordMapper(new AccountFactory()));
    List<Account> accounts = reloaded.loadAccounts().items();
    List<Transaction> transactions = reloaded.loadTransactions().items();
    assertEquals(2, accounts.size());
    assertEquals(4, transactions.size());
    assertEquals(12400.5, accounts.get(0).getBalance());
    assertEquals(-400, accounts.get(1).getBalance());
    assertEquals("Ann Uwase", accounts.get(0).getCustomer().getName());
    assertEquals(CustomerType.PREMIUM, accounts.get(0).getCustomer().getCustomerType());
    assertEquals(manager.getAllTransactions().get(1).getTimestamp(),
        transactions.get(1).getTimestamp());
  }

  @Test
  void missingFilesLoadAsEmptyWithoutCrashing() throws Exception {
    assertTrue(persistence.loadAccounts().items().isEmpty());
    assertTrue(persistence.loadTransactions().items().isEmpty());
  }

  @Test
  void savingCreatesTheDataFolderAndFiles() throws Exception {
    Path nested = tempDir.resolve("data");
    FilePersistenceService service = new FilePersistenceService(nested, new RecordMapper(factory));
    service.saveAccounts(List.of());
    assertTrue(Files.exists(nested.resolve(FilePersistenceService.ACCOUNTS_FILE)));
  }

  @Test
  void damagedLinesAreSkippedAndCounted() throws Exception {
    Files.write(tempDir.resolve(FilePersistenceService.ACCOUNTS_FILE), List.of(
        RecordMapper.ACCOUNT_HEADER,
        "ACC001|SAVINGS|Active|900.00|CUS001|REGULAR|Ann Uwase|30|ann@bank.com|+1-555-0000|1 St",
        "this line is broken",
        "ACC002|UNKNOWN|Active|10|CUS002|REGULAR|Ben|30|b@bank.com|+1-555-0000|2 St"));
    LoadResult<Account> result = persistence.loadAccounts();
    assertEquals(1, result.items().size());
    assertEquals(2, result.skippedLines());
  }

  @Test
  void loadedIdsAreNotReused() throws Exception {
    Files.write(tempDir.resolve(FilePersistenceService.ACCOUNTS_FILE), List.of(
        "ACC007|CHECKING|Active|50.00|CUS007|REGULAR|Ann Uwase|30|ann@bank.com|+1-555-0000|1 St"));
    persistence.loadAccounts();
    Account next = factory.createAccount(TestData.customerDetails("Ben Okoro"),
        CustomerType.REGULAR, AccountType.CHECKING, 100);
    assertEquals("ACC008", next.getAccountNumber());
    assertEquals("CUS008", next.getCustomer().getCustomerId());
  }
}
