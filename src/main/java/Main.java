import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import services.AccountFactory;
import services.AccountManager;
import services.AccountRepository;
import services.FilePersistenceService;
import services.RecordMapper;
import services.StatementGenerator;
import services.TransactionManager;
import services.TransactionSimulator;
import ui.AccountMenu;
import ui.AccountSelector;
import ui.BankConsoleApp;
import ui.ConsoleInput;
import ui.ConsoleOutput;
import ui.DataMenu;
import ui.MenuAction;
import ui.SimulationMenu;
import ui.StatementMenu;
import ui.TransactionMenu;

public class Main {
  private static final Path DATA_DIRECTORY = Paths.get("data");

  /**
   * Builds all objects, loads the saved data from the data folder, and starts the menu.
   *
   * @param args not used
   */
  public static void main(String[] args) {
    AccountRepository accounts = new AccountManager();
    AccountFactory accountFactory = new AccountFactory();
    TransactionManager transactionManager = new TransactionManager();
    ConsoleOutput output = new ConsoleOutput();
    ConsoleInput input = new ConsoleInput(new Scanner(System.in), output);
    FilePersistenceService persistence =
        new FilePersistenceService(DATA_DIRECTORY, new RecordMapper(accountFactory));
    DataMenu dataMenu = new DataMenu(input, output, persistence, accounts, transactionManager,
        accountFactory);
    dataMenu.loadOnStartup();
    AccountSelector accountSelector = new AccountSelector(input, output, accounts);
    List<MenuAction> menuActions = List.of(
        new AccountMenu(input, output, accounts, accountFactory),
        new TransactionMenu(input, output, accountSelector, transactionManager),
        new StatementMenu(input, output, accountSelector,
            new StatementGenerator(transactionManager)),
        dataMenu,
        new SimulationMenu(output, accountSelector,
            new TransactionSimulator(transactionManager, output::println)));
    new BankConsoleApp(input, output, menuActions, dataMenu::save).run();
  }
}
