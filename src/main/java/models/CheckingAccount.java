package models;

import models.exceptions.OverdraftExceededException;
import utils.FormatUtils;

public class CheckingAccount extends Account {
  public static final double OVERDRAFT_LIMIT = 1000;
  public static final double STANDARD_MONTHLY_FEE = 10;

  private final double monthlyFee;

  /**
   * Creates a checking account. Customers with waived fees pay no monthly fee.
   *
   * @param accountNumber the unique account number
   * @param customer the owner of the account
   * @param initialBalance the opening balance
   */
  public CheckingAccount(String accountNumber, Customer customer, double initialBalance) {
    super(accountNumber, customer, initialBalance);
    this.monthlyFee = customer.hasWaivedFees() ? 0 : STANDARD_MONTHLY_FEE;
  }

  /**
   * Returns how far below zero the balance may go.
   *
   * @return the overdraft limit
   */
  public double getOverdraftLimit() {
    return OVERDRAFT_LIMIT;
  }

  /**
   * Returns the fee charged every month.
   *
   * @return the monthly fee
   */
  public double getMonthlyFee() {
    return monthlyFee;
  }

  /**
   * Takes the monthly fee from the balance.
   */
  public void applyMonthlyFee() {
    updateBalance(-monthlyFee);
  }

  /**
   * Returns {@link AccountType#CHECKING}.
   *
   * @return the account type
   */
  @Override
  public AccountType getAccountType() {
    return AccountType.CHECKING;
  }

  /**
   * Describes the overdraft limit and monthly fee.
   *
   * @return the account features as text
   */
  @Override
  public String getAccountFeatures() {
    String fee = FormatUtils.currency(monthlyFee);
    if (getCustomer().hasWaivedFees()) {
      fee += " (WAIVED - Premium Customer)";
    }
    return "Overdraft Limit: " + FormatUtils.currency(OVERDRAFT_LIMIT) + " | Monthly Fee: " + fee;
  }

  @Override
  protected void validateWithdrawalRules(double amount) throws OverdraftExceededException {
    if (getBalance() - amount < -OVERDRAFT_LIMIT) {
      throw new OverdraftExceededException(String.format(
          "Overdraft limit exceeded. Current balance: %s. Overdraft limit: %s.",
          FormatUtils.currency(getBalance()), FormatUtils.currency(OVERDRAFT_LIMIT)));
    }
  }
}
