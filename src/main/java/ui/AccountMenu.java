package ui;

import models.Account;
import models.AccountType;
import models.CustomerDetails;
import models.CustomerType;
import models.exceptions.BankException;
import services.AccountFactory;
import services.AccountRepository;
import utils.FormatUtils;
import utils.ValidationUtils;

public class AccountMenu implements MenuAction {
  private static final String ROW_FORMAT = "%-8s | %-17s | %-10s | %-12s | %s%n";
  private static final String TABLE_LINE = "─".repeat(80);

  private final ConsoleInput input;
  private final ConsoleOutput output;
  private final AccountRepository accounts;
  private final AccountFactory accountFactory;

  /**
   * Creates the Manage Accounts menu.
   *
   * @param input reads the keyboard
   * @param output prints messages
   * @param accounts where accounts are stored
   * @param accountFactory creates new accounts
   */
  public AccountMenu(ConsoleInput input, ConsoleOutput output, AccountRepository accounts,
      AccountFactory accountFactory) {
    this.input = input;
    this.output = output;
    this.accounts = accounts;
    this.accountFactory = accountFactory;
  }

  /**
   * Returns the main menu title of this option.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Manage Accounts";
  }

  /**
   * Shows the account sub-menu and runs the chosen option.
   */
  @Override
  public void execute() {
    output.printTitle("MANAGE ACCOUNTS");
    output.println("1. Create Account");
    output.println("2. View All Accounts");
    output.println("3. Back to Main Menu");
    int choice = input.readInt("\nEnter your choice: ", 1, 3);
    if (choice == 1) {
      createAccount();
    } else if (choice == 2) {
      viewAllAccounts();
    }
  }

  private void createAccount() {
    output.printTitle("ACCOUNT CREATION");
    CustomerDetails details = readCustomerDetails();
    CustomerType customerType = readCustomerType();
    AccountType accountType = readAccountType();
    openAccount(details, customerType, accountType);
  }

  private CustomerDetails readCustomerDetails() {
    String name = input.readValidated("Enter customer name: ", ValidationUtils.IS_NAME,
        ValidationUtils.NAME_RULE);
    int age = input.readInt("Enter customer age: ", 18, 120);
    String email = input.readValidated("Enter customer email: ", ValidationUtils.IS_EMAIL,
        ValidationUtils.EMAIL_RULE);
    output.printSuccess("Email accepted!");
    String phone = input.readValidated("Enter customer phone: ", ValidationUtils.IS_PHONE,
        ValidationUtils.PHONE_RULE);
    String address = input.readValidated("Enter customer address: ",
        ValidationUtils.IS_ADDRESS, ValidationUtils.ADDRESS_RULE);
    return new CustomerDetails(name, age, email, phone, address);
  }

  private CustomerType readCustomerType() {
    output.println("\nCustomer type:");
    output.println("1. Regular Customer (Standard banking services)");
    output.println("2. Premium Customer (Enhanced benefits, min balance $10,000)");
    int choice = input.readInt("\nSelect type (1-2): ", 1, 2);
    return choice == 1 ? CustomerType.REGULAR : CustomerType.PREMIUM;
  }

  private AccountType readAccountType() {
    output.println("\nAccount type:");
    output.println("1. Savings Account (Interest: 3.5%, Min Balance: $500)");
    output.println("2. Checking Account (Overdraft: $1,000, Monthly Fee: $10)");
    int choice = input.readInt("\nSelect type (1-2): ", 1, 2);
    return choice == 1 ? AccountType.SAVINGS : AccountType.CHECKING;
  }

  private void openAccount(CustomerDetails details, CustomerType customerType,
      AccountType accountType) {
    while (true) {
      double deposit = input.readDouble("\nEnter initial deposit amount: $");
      try {
        Account account =
            accountFactory.createAccount(details, customerType, accountType, deposit);
        accounts.addAccount(account);
        printAccountCreated(account);
        return;
      } catch (BankException e) {
        output.printError(e.getMessage());
      }
    }
  }

  private void printAccountCreated(Account account) {
    output.println("");
    output.printSuccess("Account created successfully!");
    output.println("  Account Number: " + account.getAccountNumber());
    output.println("  Customer ID: " + account.getCustomer().getCustomerId());
    output.println("  Customer: " + account.getCustomer().getName() + " ("
        + account.getCustomer().getCustomerType().getDisplayName() + ")");
    output.println("  Account Type: " + account.getAccountType().getDisplayName());
    output.println("  Initial Balance: " + FormatUtils.currency(account.getBalance()));
    output.println("  " + account.getAccountFeatures());
    output.println("  Status: " + account.getStatus());
  }

  private void viewAllAccounts() {
    output.printTitle("ACCOUNT LISTING");
    System.out.printf(ROW_FORMAT, "ACC NO", "CUSTOMER NAME", "TYPE", "BALANCE", "STATUS");
    output.println(TABLE_LINE);
    accounts.getAllAccounts().forEach(this::printAccountRow);
    output.println("\nTotal Accounts: " + accounts.getAccountCount());
    output.println("Total Bank Balance: " + FormatUtils.currency(accounts.getTotalBalance()));
  }

  private void printAccountRow(Account account) {
    System.out.printf(ROW_FORMAT, account.getAccountNumber(), account.getCustomer().getName(),
        account.getAccountType().getDisplayName(), FormatUtils.currency(account.getBalance()),
        account.getStatus());
    output.println("         | Customer ID: " + account.getCustomer().getCustomerId()
        + " | " + account.getCustomer().getEmail() + " | " + account.getCustomer().getPhone());
    output.println("         | " + account.getAccountFeatures());
    output.println(TABLE_LINE);
  }
}
