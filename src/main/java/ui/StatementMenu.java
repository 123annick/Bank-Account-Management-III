package ui;

import models.Account;
import services.StatementGenerator;
import utils.FunctionalUtils;

public class StatementMenu implements MenuAction {
  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final AccountSelector accountSelector;
  private final StatementGenerator statementGenerator;

  /**
   * Creates the Generate Account Statements menu.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param accountSelector asks the user to pick an account
   * @param statementGenerator builds the statement text
   */
  public StatementMenu(ConsoleInput input, ConsoleOutput output,
      AccountSelector accountSelector, StatementGenerator statementGenerator) {
    this.input = input;
    this.output = output;
    this.accountSelector = accountSelector;
    this.statementGenerator = statementGenerator;
  }

  /**
   * Returns the main menu title of this option.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Generate Account Statements";
  }

  /**
   * Asks for an account and a view, then prints the statement built with streams.
   */
  @Override
  public void execute() {
    output.printTitle("GENERATE ACCOUNT STATEMENT");
    Account account = accountSelector.selectAccount("Enter Account Number");
    if (account == null) {
      return;
    }
    output.println("\nStatement view:");
    output.println("1. All transactions, latest first");
    output.println("2. All transactions, largest amount first");
    output.println("3. Deposits only, largest amount first");
    output.println("4. Withdrawals only, latest first");
    int choice = input.readInt("\nSelect view (1-4): ", 1, 4);
    output.println("");
    output.println(buildStatement(account, choice));
    output.printSuccess("Transactions filtered and sorted using Streams and Lambdas.");
  }

  private String buildStatement(Account account, int choice) {
    switch (choice) {
      case 2:
        return statementGenerator.generateStatement(account, transaction -> true,
            FunctionalUtils.BY_AMOUNT_LARGEST_FIRST);
      case 3:
        return statementGenerator.generateStatement(account, FunctionalUtils.IS_CREDIT,
            FunctionalUtils.BY_AMOUNT_LARGEST_FIRST);
      case 4:
        return statementGenerator.generateStatement(account, FunctionalUtils.IS_DEBIT,
            FunctionalUtils.BY_DATE_NEWEST_FIRST);
      default:
        return statementGenerator.generateStatement(account);
    }
  }
}
