package services;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import models.Account;
import models.exceptions.InvalidAccountException;

public class AccountManager implements AccountRepository {
  private static final Comparator<Account> BY_ACCOUNT_NUMBER =
      Comparator.comparing(Account::getAccountNumber);

  private final Map<String, Account> accounts = new HashMap<>();

  /**
   * Stores a new account in the map, using its account number as the key.
   *
   * @param account the account to store
   * @throws InvalidAccountException when an account with the same number already exists
   */
  @Override
  public void addAccount(Account account) throws InvalidAccountException {
    String key = keyOf(account.getAccountNumber());
    if (accounts.containsKey(key)) {
      throw new InvalidAccountException("Account " + key + " already exists.");
    }
    accounts.put(key, account);
  }

  /**
   * Finds an account with a single map lookup, which takes the same time however many
   * accounts there are.
   *
   * @param accountNumber the number to look for
   * @return the matching account
   * @throws InvalidAccountException when no account has that number
   */
  @Override
  public Account findAccount(String accountNumber) throws InvalidAccountException {
    Account account = accounts.get(keyOf(accountNumber));
    if (account == null) {
      throw new InvalidAccountException(
          "Account not found. Please check the account number and try again.");
    }
    return account;
  }

  /**
   * Returns all stored accounts sorted by account number.
   *
   * @return a new list containing every account
   */
  @Override
  public List<Account> getAllAccounts() {
    return findAccounts(account -> true);
  }

  /**
   * Returns the accounts that pass a rule, sorted by account number.
   *
   * @param rule which accounts to keep
   * @return a new list with the matching accounts
   */
  @Override
  public List<Account> findAccounts(Predicate<Account> rule) {
    return accounts.values().stream()
        .filter(rule)
        .sorted(BY_ACCOUNT_NUMBER)
        .collect(Collectors.toList());
  }

  /**
   * Returns how many accounts are stored.
   *
   * @return the number of accounts
   */
  @Override
  public int getAccountCount() {
    return accounts.size();
  }

  /**
   * Adds up the balances of all accounts with a stream.
   *
   * @return the total bank balance
   */
  @Override
  public double getTotalBalance() {
    return accounts.values().stream().mapToDouble(Account::getBalance).sum();
  }

  /**
   * Removes every stored account and stores the given ones instead.
   *
   * @param newAccounts the accounts to keep
   */
  @Override
  public void replaceAll(Collection<Account> newAccounts) {
    accounts.clear();
    newAccounts.forEach(account -> accounts.put(keyOf(account.getAccountNumber()), account));
  }

  private String keyOf(String accountNumber) {
    return accountNumber.trim().toUpperCase(Locale.ROOT);
  }
}
