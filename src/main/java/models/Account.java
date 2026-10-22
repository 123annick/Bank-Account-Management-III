package models;

import models.exceptions.InsufficientFundsException;
import models.exceptions.InvalidAmountException;
import models.exceptions.OverdraftExceededException;
import utils.ValidationUtils;

public abstract class Account implements Transactable {
  private static final String ACTIVE_STATUS = "Active";

  private final String accountNumber;
  private final Customer customer;
  private double balance;
  private String status;

  protected Account(String accountNumber, Customer customer, double initialBalance) {
    this.accountNumber = accountNumber;
    this.customer = customer;
    this.balance = initialBalance;
    this.status = ACTIVE_STATUS;
  }

  /**
   * Returns the unique account number, for example ACC001.
   *
   * @return the account number
   */
  public String getAccountNumber() {
    return accountNumber;
  }

  /**
   * Returns the customer who owns this account.
   *
   * @return the owner
   */
  public Customer getCustomer() {
    return customer;
  }

  /**
   * Returns the current balance.
   *
   * @return the balance
   */
  public synchronized double getBalance() {
    return balance;
  }

  /**
   * Returns the account status, for example Active.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Changes the account status.
   *
   * @param status the new status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Adds money to the balance.
   *
   * @param amount the amount to add, must be greater than zero
   * @throws InvalidAmountException when the amount is zero or negative
   */
  @Override
  public synchronized void deposit(double amount) throws InvalidAmountException {
    ValidationUtils.validateAmount(amount);
    updateBalance(amount);
  }

  /**
   * Takes money from the balance after checking the rules of this account type.
   * The check and the update happen as one step, so two threads cannot both pass the check.
   *
   * @param amount the amount to take, must be greater than zero
   * @throws InvalidAmountException when the amount is zero or negative
   * @throws InsufficientFundsException when a minimum balance would be broken
   * @throws OverdraftExceededException when the overdraft limit would be passed
   */
  @Override
  public synchronized void withdraw(double amount)
      throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
    checkWithdrawal(amount);
    updateBalance(-amount);
  }

  /**
   * Checks whether a withdrawal would be allowed without changing the balance.
   *
   * @param amount the amount to check
   * @throws InvalidAmountException when the amount is zero or negative
   * @throws InsufficientFundsException when a minimum balance would be broken
   * @throws OverdraftExceededException when the overdraft limit would be passed
   */
  public synchronized void checkWithdrawal(double amount)
      throws InvalidAmountException, InsufficientFundsException, OverdraftExceededException {
    ValidationUtils.validateAmount(amount);
    validateWithdrawalRules(amount);
  }

  /**
   * Returns the kind of account.
   *
   * @return the account type
   */
  public abstract AccountType getAccountType();

  /**
   * Returns a short description of the special rules of this account.
   *
   * @return the account features as text
   */
  public abstract String getAccountFeatures();

  protected abstract void validateWithdrawalRules(double amount)
      throws InsufficientFundsException, OverdraftExceededException;

  protected synchronized void updateBalance(double change) {
    balance = roundToCents(balance + change);
  }

  private static double roundToCents(double amount) {
    return Math.round(amount * 100) / 100.0;
  }
}
