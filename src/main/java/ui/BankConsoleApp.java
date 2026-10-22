package ui;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

public class BankConsoleApp {
  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final List<MenuAction> menuActions;
  private final BooleanSupplier saveOnExit;

  /**
   * Creates the application with the options shown in the main menu.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param menuActions the main menu options, in display order
   * @param saveOnExit saves the data when the user exits and tells whether it worked
   */
  public BankConsoleApp(ConsoleInput input, ConsoleOutput output, List<MenuAction> menuActions,
      BooleanSupplier saveOnExit) {
    this.input = input;
    this.output = output;
    this.menuActions = menuActions;
    this.saveOnExit = saveOnExit;
  }

  /**
   * Shows the main menu again and again until the user chooses Exit, then saves the data.
   * If the keyboard input is closed (for example Ctrl+D), it still saves and exits safely.
   */
  public void run() {
    try {
      runMenuLoop();
    } catch (NoSuchElementException e) {
      output.println("\nInput was closed. Exiting safely.");
    }
    printGoodbye(saveOnExit.getAsBoolean());
  }

  private void runMenuLoop() {
    int exitOption = menuActions.size() + 1;
    int choice = showMainMenu();
    while (choice != exitOption) {
      menuActions.get(choice - 1).execute();
      input.pause();
      choice = showMainMenu();
    }
  }

  private int showMainMenu() {
    output.println("\n╔════════════════════════════════════════════╗");
    output.println("║      BANK ACCOUNT MANAGEMENT SYSTEM        ║");
    output.println("╚════════════════════════════════════════════╝");
    output.println("\nMain Menu:");
    output.println("-----------");
    IntStream.range(0, menuActions.size())
        .forEach(i -> output.println((i + 1) + ". " + menuActions.get(i).getTitle()));
    int exitOption = menuActions.size() + 1;
    output.println(exitOption + ". Exit");
    return input.readInt("\nEnter your choice: ", 1, exitOption);
  }

  private void printGoodbye(boolean saved) {
    output.println("\nThank you for using the Bank Account Management System!");
    output.println(saved ? "Data automatically saved to disk." : "Data could not be saved.");
    output.println("Goodbye!");
  }
}
