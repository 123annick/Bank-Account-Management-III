package services;

import java.util.List;
import models.Account;
import models.AccountType;
import models.CustomerDetails;
import models.CustomerType;
import models.exceptions.BankException;

public final class SampleDataLoader {

  private SampleDataLoader() {
  }

  /**
   * Creates the five Week 1 sample accounts: three savings and two checking.
   *
   * @param factory used to create the accounts with new ids
   * @return the five sample accounts
   * @throws BankException when a sample account breaks a rule
   */
  public static List<Account> createSampleAccounts(AccountFactory factory) throws BankException {
    return List.of(
        factory.createAccount(details("John Smith", 40, "john.smith@bank.com", "+1-555-0001",
            "1 Main St"), CustomerType.REGULAR, AccountType.SAVINGS, 5250),
        factory.createAccount(details("Sarah Johnson", 34, "sarah.johnson@bank.com",
            "+1-555-0002", "2 Main St"), CustomerType.REGULAR, AccountType.CHECKING, 3450),
        factory.createAccount(details("Michael Chen", 45, "michael.chen@bank.com", "+1-555-0003",
            "3 Main St"), CustomerType.PREMIUM, AccountType.SAVINGS, 15750),
        factory.createAccount(details("Emily Brown", 29, "emily.brown@bank.com", "+1-555-0004",
            "4 Main St"), CustomerType.REGULAR, AccountType.CHECKING, 890),
        factory.createAccount(details("David Wilson", 52, "david.wilson@bank.com", "+1-555-0005",
            "5 Main St"), CustomerType.PREMIUM, AccountType.SAVINGS, 25300));
  }

  private static CustomerDetails details(String name, int age, String email, String phone,
      String address) {
    return new CustomerDetails(name, age, email, phone, address);
  }
}
