package ui;

import models.Account;
import models.exceptions.InvalidAccountException;
import services.AccountRepository;
import utils.ValidationUtils;

public class AccountSelector {
  private static final String CANCEL = "0";

  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final AccountRepository accounts;

  /**
   * Creates a helper that asks the user to pick an account.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param accounts where accounts are looked up
   */
  public AccountSelector(ConsoleInput input, ConsoleOutput output, AccountRepository accounts) {
    this.input = input;
    this.output = output;
    this.accounts = accounts;
  }

  /**
   * Asks for an account number until a real account is found or the user types 0. The
   * number must match the ACC followed by 3 digits pattern before it is looked up.
   *
   * @param prompt the question to show
   * @return the chosen account, or null when the user cancels
   */
  public Account selectAccount(String prompt) {
    while (true) {
      String accountNumber = input.readText(prompt + " (0 to cancel): ");
      if (accountNumber.equals(CANCEL)) {
        return null;
      }
      if (!ValidationUtils.IS_ACCOUNT_NUMBER.test(accountNumber.toUpperCase())) {
        output.printError(ValidationUtils.ACCOUNT_NUMBER_RULE);
        continue;
      }
      try {
        return accounts.findAccount(accountNumber);
      } catch (InvalidAccountException e) {
        output.printError(e.getMessage());
      }
    }
  }
}
