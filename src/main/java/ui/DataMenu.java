package ui;

import java.util.List;
import models.Account;
import models.Transaction;
import models.exceptions.BankException;
import services.AccountFactory;
import services.AccountRepository;
import services.FilePersistenceService;
import services.LoadResult;
import services.SampleDataLoader;
import services.TransactionManager;

public class DataMenu implements MenuAction {
  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final FilePersistenceService persistence;
  private final AccountRepository accounts;
  private final TransactionManager transactionManager;
  private final AccountFactory accountFactory;

  /**
   * Creates the Save/Load Data menu.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param persistence reads and writes the data files
   * @param accounts where accounts are kept in memory
   * @param transactionManager where transactions are kept in memory
   * @param accountFactory creates sample accounts when no data file exists
   */
  public DataMenu(ConsoleInput input, ConsoleOutput output, FilePersistenceService persistence,
      AccountRepository accounts, TransactionManager transactionManager,
      AccountFactory accountFactory) {
    this.input = input;
    this.output = output;
    this.persistence = persistence;
    this.accounts = accounts;
    this.transactionManager = transactionManager;
    this.accountFactory = accountFactory;
  }

  /**
   * Returns the main menu title of this option.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Save/Load Data";
  }

  /**
   * Shows the save and load options and runs the chosen one.
   */
  @Override
  public void execute() {
    output.printTitle("SAVE/LOAD DATA");
    output.println("1. Save data to files");
    output.println("2. Load data from files (unsaved changes are lost)");
    output.println("3. Back to Main Menu");
    int choice = input.readInt("\nEnter your choice: ", 1, 3);
    if (choice == 1) {
      save();
    } else if (choice == 2 && input.confirm("Replace the data in memory? (Y/N): ")) {
      load();
    }
  }

  /**
   * Loads the data files at startup. When accounts.txt is missing or holds no accounts,
   * creates the data files with the five sample accounts so the program never starts empty.
   */
  public void loadOnStartup() {
    output.println("Loading account data from files...\n");
    try {
      LoadResult<Account> loadedAccounts = persistence.loadAccounts();
      if (loadedAccounts.items().isEmpty() && loadedAccounts.skippedLines() == 0) {
        createSampleData();
        return;
      }
      applyLoad(loadedAccounts);
    } catch (BankException e) {
      output.printError(e.getMessage() + " Starting with no accounts.");
    }
  }

  /**
   * Writes all accounts and transactions to the data files.
   *
   * @return true when both files were written
   */
  public boolean save() {
    output.printTitle("SAVING ACCOUNT DATA");
    try {
      persistence.saveAccounts(accounts.getAllAccounts());
      output.println("Accounts saved to " + persistence.getAccountsFile());
      persistence.saveTransactions(transactionManager.getAllTransactions());
      output.println("Transactions saved to " + persistence.getTransactionsFile());
      output.printSuccess("File save completed successfully.");
      return true;
    } catch (BankException e) {
      output.printError(e.getMessage());
      return false;
    }
  }

  private void load() {
    try {
      applyLoad(persistence.loadAccounts());
    } catch (BankException e) {
      output.printError(e.getMessage());
    }
  }

  private void applyLoad(LoadResult<Account> loadedAccounts) throws BankException {
    LoadResult<Transaction> loadedTransactions = persistence.loadTransactions();
    replaceData(loadedAccounts.items(), loadedTransactions.items());
    reportLoad(loadedAccounts, loadedTransactions);
  }

  private void replaceData(List<Account> loadedAccounts, List<Transaction> loadedTransactions) {
    accounts.replaceAll(loadedAccounts);
    accountFactory.skipPastExisting(loadedAccounts);
    transactionManager.replaceAll(loadedTransactions);
  }

  private void reportLoad(LoadResult<Account> loadedAccounts,
      LoadResult<Transaction> loadedTransactions) {
    output.printSuccess(loadedAccounts.items().size() + " accounts loaded successfully from "
        + FilePersistenceService.ACCOUNTS_FILE);
    output.printSuccess(loadedTransactions.items().size() + " transactions loaded from "
        + FilePersistenceService.TRANSACTIONS_FILE);
    int skipped = loadedAccounts.skippedLines() + loadedTransactions.skippedLines();
    if (skipped > 0) {
      output.printError(skipped + " damaged line(s) were skipped.");
    }
  }

  private void createSampleData() {
    output.println(FilePersistenceService.ACCOUNTS_FILE
        + " not found or empty. Creating data files with 5 sample accounts.");
    try {
      accounts.replaceAll(SampleDataLoader.createSampleAccounts(accountFactory));
      transactionManager.replaceAll(List.of());
      save();
    } catch (BankException e) {
      output.printError("Could not create sample data. " + e.getMessage());
    }
  }
}
