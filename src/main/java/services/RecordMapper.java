package services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import models.Account;
import models.AccountType;
import models.Customer;
import models.CustomerDetails;
import models.CustomerType;
import models.Transaction;
import models.TransactionType;
import utils.ValidationUtils;

public class RecordMapper {
  public static final String ACCOUNT_HEADER = "# accountNumber|accountType|status|balance|"
      + "customerId|customerType|name|age|email|phone|address";
  public static final String TRANSACTION_HEADER =
      "# transactionId|accountNumber|type|amount|balanceAfter|timestamp";

  private static final String SEPARATOR = "|";
  private static final Pattern SPLITTER = Pattern.compile("\\|");
  private static final int ACCOUNT_FIELDS = 11;
  private static final int TRANSACTION_FIELDS = 6;
  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private final AccountFactory accountFactory;

  /**
   * Creates a mapper that rebuilds accounts through the given factory.
   *
   * @param accountFactory used to rebuild accounts and keep ids in step
   */
  public RecordMapper(AccountFactory accountFactory) {
    this.accountFactory = accountFactory;
  }

  /**
   * Turns an account into one line of text for accounts.txt.
   *
   * @param account the account to write
   * @return the line, fields separated by |
   */
  public String toLine(Account account) {
    Customer customer = account.getCustomer();
    return String.join(SEPARATOR, account.getAccountNumber(), account.getAccountType().name(),
        account.getStatus(), money(account.getBalance()), customer.getCustomerId(),
        customer.getCustomerType().name(), customer.getName(), String.valueOf(customer.getAge()),
        customer.getEmail(), customer.getPhone(), customer.getAddress());
  }

  /**
   * Turns a transaction into one line of text for transactions.txt.
   *
   * @param transaction the transaction to write
   * @return the line, fields separated by |
   */
  public String toLine(Transaction transaction) {
    return String.join(SEPARATOR, transaction.getTransactionId(),
        transaction.getAccountNumber(), transaction.getType().name(),
        money(transaction.getAmount()), money(transaction.getBalanceAfter()),
        transaction.getTimestamp().format(TIMESTAMP_FORMAT));
  }

  /**
   * Turns one line of accounts.txt back into an account.
   *
   * @param line the line to read
   * @return the account, or empty when the line is broken
   */
  public Optional<Account> toAccount(String line) {
    String[] fields = SPLITTER.split(line, -1);
    if (fields.length != ACCOUNT_FIELDS || !ValidationUtils.IS_ACCOUNT_NUMBER.test(fields[0])) {
      return Optional.empty();
    }
    try {
      return Optional.of(buildAccount(fields));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Turns one line of transactions.txt back into a transaction.
   *
   * @param line the line to read
   * @return the transaction, or empty when the line is broken
   */
  public Optional<Transaction> toTransaction(String line) {
    String[] fields = SPLITTER.split(line, -1);
    if (fields.length != TRANSACTION_FIELDS) {
      return Optional.empty();
    }
    try {
      return Optional.of(new Transaction(fields[0], fields[1], TransactionType.valueOf(fields[2]),
          Double.parseDouble(fields[3]), Double.parseDouble(fields[4]),
          LocalDateTime.parse(fields[5], TIMESTAMP_FORMAT)));
    } catch (IllegalArgumentException | DateTimeParseException e) {
      return Optional.empty();
    }
  }

  private Account buildAccount(String[] fields) {
    CustomerDetails details = new CustomerDetails(fields[6], Integer.parseInt(fields[7]),
        fields[8], fields[9], fields[10]);
    Account account = accountFactory.restoreAccount(fields[0], fields[4], details,
        CustomerType.valueOf(fields[5]), AccountType.valueOf(fields[1]),
        Double.parseDouble(fields[3]));
    account.setStatus(fields[2]);
    return account;
  }

  private String money(double amount) {
    return String.format(Locale.ROOT, "%.2f", amount);
  }
}
