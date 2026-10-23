package services;

import java.util.Collection;
import models.Account;
import models.AccountType;
import models.CheckingAccount;
import models.Customer;
import models.CustomerDetails;
import models.CustomerType;
import models.PremiumCustomer;
import models.RegularCustomer;
import models.SavingsAccount;
import models.exceptions.BankException;
import models.exceptions.InvalidAccountException;
import utils.IdGenerator;
import utils.ValidationUtils;

public class AccountFactory {
  private final IdGenerator accountIds = new IdGenerator("ACC");
  private final IdGenerator customerIds = new IdGenerator("CUS");

  /**
   * Checks the details and opening rules, then creates the customer and the account with
   * new ids.
   *
   * @param details the customer's personal details
   * @param customerType regular or premium
   * @param accountType savings or checking
   * @param initialDeposit the opening deposit
   * @return the new account
   * @throws BankException when a detail has the wrong format, the deposit breaks a rule or
   *     the bank already has 999 accounts
   */
  public Account createAccount(CustomerDetails details, CustomerType customerType,
      AccountType accountType, double initialDeposit) throws BankException {
    validateDetails(details);
    validateInitialDeposit(customerType, accountType, initialDeposit);
    validateCapacity();
    Customer customer = createCustomer(customerIds.next(), details, customerType);
    return buildAccount(accountIds.next(), customer, accountType, initialDeposit);
  }

  /**
   * Rebuilds an account that already exists, for example one read from a file, keeping its
   * ids. New ids created later continue after these ones.
   *
   * @param accountNumber the existing account number
   * @param customerId the existing customer id
   * @param details the customer's personal details
   * @param customerType regular or premium
   * @param accountType savings or checking
   * @param balance the saved balance
   * @return the rebuilt account
   */
  public Account restoreAccount(String accountNumber, String customerId, CustomerDetails details,
      CustomerType customerType, AccountType accountType, double balance) {
    accountIds.skipPast(accountNumber);
    customerIds.skipPast(customerId);
    Customer customer = createCustomer(customerId, details, customerType);
    return buildAccount(accountNumber, customer, accountType, balance);
  }

  /**
   * Makes sure new ids continue after every given account, for example after a reload.
   *
   * @param accounts the accounts that already exist
   */
  public void skipPastExisting(Collection<Account> accounts) {
    accounts.forEach(account -> {
      accountIds.skipPast(account.getAccountNumber());
      customerIds.skipPast(account.getCustomer().getCustomerId());
    });
  }

  private void validateCapacity() throws InvalidAccountException {
    if (!ValidationUtils.IS_ACCOUNT_NUMBER.test(accountIds.peek())
        || !ValidationUtils.IS_CUSTOMER_ID.test(customerIds.peek())) {
      throw new InvalidAccountException("The bank has reached its limit of 999 accounts.");
    }
  }

  private void validateDetails(CustomerDetails details) throws BankException {
    ValidationUtils.requireValid(details.name(), ValidationUtils.IS_NAME,
        ValidationUtils.NAME_RULE);
    ValidationUtils.requireValid(details.email(), ValidationUtils.IS_EMAIL,
        ValidationUtils.EMAIL_RULE);
    ValidationUtils.requireValid(details.phone(), ValidationUtils.IS_PHONE,
        ValidationUtils.PHONE_RULE);
    ValidationUtils.requireValid(details.address(), ValidationUtils.IS_ADDRESS,
        ValidationUtils.ADDRESS_RULE);
    ValidationUtils.validateAge(details.age());
  }

  private void validateInitialDeposit(CustomerType customerType, AccountType accountType,
      double initialDeposit) throws BankException {
    ValidationUtils.validateAmount(initialDeposit);
    ValidationUtils.validateInitialDeposit(initialDeposit,
        customerType.getMinimumOpeningDeposit(), customerType.getDisplayName() + " customers");
    ValidationUtils.validateInitialDeposit(initialDeposit,
        accountType.getMinimumOpeningDeposit(), accountType.getDisplayName() + " accounts");
  }

  private Customer createCustomer(String customerId, CustomerDetails details,
      CustomerType customerType) {
    if (customerType == CustomerType.PREMIUM) {
      return new PremiumCustomer(customerId, details);
    }
    return new RegularCustomer(customerId, details);
  }

  private Account buildAccount(String accountNumber, Customer customer, AccountType accountType,
      double balance) {
    if (accountType == AccountType.SAVINGS) {
      return new SavingsAccount(accountNumber, customer, balance);
    }
    return new CheckingAccount(accountNumber, customer, balance);
  }
}
