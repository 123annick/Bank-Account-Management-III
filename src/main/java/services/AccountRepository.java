package services;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import models.Account;
import models.exceptions.InvalidAccountException;

public interface AccountRepository {

  /**
   * Stores a new account.
   *
   * @param account the account to store
   * @throws InvalidAccountException when an account with the same number already exists
   */
  void addAccount(Account account) throws InvalidAccountException;

  /**
   * Finds an account by its number, ignoring upper or lower case.
   *
   * @param accountNumber the number to look for, for example ACC001
   * @return the matching account
   * @throws InvalidAccountException when no account has that number
   */
  Account findAccount(String accountNumber) throws InvalidAccountException;

  /**
   * Returns all stored accounts sorted by account number.
   *
   * @return a new list containing every account
   */
  List<Account> getAllAccounts();

  /**
   * Returns the accounts that pass a rule, sorted by account number.
   *
   * @param rule which accounts to keep
   * @return a new list with the matching accounts
   */
  List<Account> findAccounts(Predicate<Account> rule);

  /**
   * Returns how many accounts are stored.
   *
   * @return the number of accounts
   */
  int getAccountCount();

  /**
   * Adds up the balances of all accounts.
   *
   * @return the total bank balance
   */
  double getTotalBalance();

  /**
   * Removes every stored account and stores the given ones instead, for example after loading
   * from a file.
   *
   * @param accounts the accounts to keep
   */
  void replaceAll(Collection<Account> accounts);
}
