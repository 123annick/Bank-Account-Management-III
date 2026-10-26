package ui;

import java.time.LocalDateTime;
import models.Account;
import models.exceptions.BankException;
import models.exceptions.InvalidAmountException;
import services.TransactionManager;
import utils.FormatUtils;
import utils.ValidationUtils;

public class TransactionMenu implements MenuAction {
  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final AccountSelector accountSelector;
  private final TransactionManager transactionManager;

  /**
   * Creates the Perform Transactions menu.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param accountSelector asks the user to pick accounts
   * @param transactionManager carries out and records transactions
   */
  public TransactionMenu(ConsoleInput input, ConsoleOutput output,
      AccountSelector accountSelector, TransactionManager transactionManager) {
    this.input = input;
    this.output = output;
    this.accountSelector = accountSelector;
    this.transactionManager = transactionManager;
  }

  /**
   * Returns the main menu title of this option.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Perform Transactions";
  }

  /**
   * Asks for an account and a transaction type, then runs the transaction.
   */
  @Override
  public void execute() {
    output.printTitle("PROCESS TRANSACTION");
    Account account = accountSelector.selectAccount("Enter Account Number");
    if (account == null) {
      return;
    }
    printAccountSummary(account);
    int choice = readTransactionType();
    if (choice == 1) {
      deposit(account);
    } else if (choice == 2) {
      withdraw(account);
    } else {
      transfer(account);
    }
  }

  private void printAccountSummary(Account account) {
    output.println("\nCustomer: " + account.getCustomer().getName());
    output.println("Account Type: " + account.getAccountType().getDisplayName());
    output.println("Current Balance: " + FormatUtils.currency(account.getBalance()));
  }

  private int readTransactionType() {
    output.println("\nTransaction type:");
    output.println("1. Deposit");
    output.println("2. Withdrawal");
    output.println("3. Transfer");
    return input.readInt("\nSelect type (1-3): ", 1, 3);
  }

  private void deposit(Account account) {
    double amount = readValidAmount("Enter amount to deposit: $");
    double newBalance = account.getBalance() + amount;
    if (!confirmTransaction(account, "DEPOSIT", amount, newBalance)) {
      return;
    }
    try {
      transactionManager.deposit(account, amount);
      printCompleted(account);
    } catch (BankException e) {
      output.printTransactionFailure(e.getMessage());
    }
  }

  private void withdraw(Account account) {
    double amount = readValidAmount("Enter amount to withdraw: $");
    try {
      account.checkWithdrawal(amount);
      double newBalance = account.getBalance() - amount;
      if (confirmTransaction(account, "WITHDRAWAL", amount, newBalance)) {
        transactionManager.withdraw(account, amount);
        printCompleted(account);
      }
    } catch (BankException e) {
      output.printTransactionFailure(e.getMessage());
    }
  }

  private void transfer(Account source) {
    Account target = accountSelector.selectAccount("Enter Target Account Number");
    if (target == null) {
      return;
    }
    double amount = readValidAmount("Enter amount to transfer: $");
    try {
      transactionManager.validateTransfer(source, target, amount);
      String type = "TRANSFER to " + target.getAccountNumber();
      if (confirmTransaction(source, type, amount, source.getBalance() - amount)) {
        transactionManager.transfer(source, target, amount);
        printCompleted(source);
      }
    } catch (BankException e) {
      output.printTransactionFailure(e.getMessage());
    }
  }

  private double readValidAmount(String prompt) {
    while (true) {
      double amount = input.readAmount(prompt);
      try {
        ValidationUtils.validateAmount(amount);
        return amount;
      } catch (InvalidAmountException e) {
        output.printError(e.getMessage());
      }
    }
  }

  private boolean confirmTransaction(Account account, String type, double amount,
      double newBalance) {
    output.printTitle("TRANSACTION CONFIRMATION");
    output.println("Transaction ID: " + transactionManager.peekNextTransactionId());
    output.println("Account: " + account.getAccountNumber());
    output.println("Type: " + type);
    output.println("Amount: " + FormatUtils.currency(amount));
    output.println("Previous Balance: " + FormatUtils.currency(account.getBalance()));
    output.println("New Balance: " + FormatUtils.currency(newBalance));
    output.println("Date/Time: " + FormatUtils.dateTime(LocalDateTime.now()));
    output.printSeparator();
    if (input.confirm("\nConfirm transaction? (Y/N): ")) {
      return true;
    }
    output.println("\nTransaction cancelled.");
    return false;
  }

  private void printCompleted(Account account) {
    output.println("");
    output.printSuccess("Transaction completed successfully!");
    output.println("New Balance: " + FormatUtils.currency(account.getBalance()));
  }
}
