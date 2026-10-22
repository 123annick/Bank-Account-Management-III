package services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import models.Account;
import models.Transaction;
import models.exceptions.PersistenceException;

public class FilePersistenceService {
  public static final String ACCOUNTS_FILE = "accounts.txt";
  public static final String TRANSACTIONS_FILE = "transactions.txt";

  private final Path accountsFile;
  private final Path transactionsFile;
  private final RecordMapper mapper;

  /**
   * Creates a service that keeps accounts.txt and transactions.txt in the given folder.
   *
   * @param dataDirectory the folder for the data files, for example Paths.get("data")
   * @param mapper turns objects into lines and lines back into objects
   */
  public FilePersistenceService(Path dataDirectory, RecordMapper mapper) {
    this.accountsFile = dataDirectory.resolve(ACCOUNTS_FILE);
    this.transactionsFile = dataDirectory.resolve(TRANSACTIONS_FILE);
    this.mapper = mapper;
  }

  /**
   * Reads every account from accounts.txt. Broken lines are skipped and counted.
   *
   * @return the accounts and how many lines were skipped
   * @throws PersistenceException when the file cannot be read
   */
  public LoadResult<Account> loadAccounts() throws PersistenceException {
    return readRecords(accountsFile, mapper::toAccount);
  }

  /**
   * Reads every transaction from transactions.txt. Broken lines are skipped and counted.
   *
   * @return the transactions and how many lines were skipped
   * @throws PersistenceException when the file cannot be read
   */
  public LoadResult<Transaction> loadTransactions() throws PersistenceException {
    return readRecords(transactionsFile, mapper::toTransaction);
  }

  /**
   * Writes every account to accounts.txt, replacing what was there.
   *
   * @param accounts the accounts to save
   * @throws PersistenceException when the file cannot be written
   */
  public void saveAccounts(Collection<Account> accounts) throws PersistenceException {
    writeLines(accountsFile, RecordMapper.ACCOUNT_HEADER,
        accounts.stream().map(mapper::toLine));
  }

  /**
   * Writes every transaction to transactions.txt, replacing what was there.
   *
   * @param transactions the transactions to save
   * @throws PersistenceException when the file cannot be written
   */
  public void saveTransactions(Collection<Transaction> transactions)
      throws PersistenceException {
    writeLines(transactionsFile, RecordMapper.TRANSACTION_HEADER,
        transactions.stream().map(mapper::toLine));
  }

  /**
   * Returns the path of accounts.txt.
   *
   * @return the accounts file path
   */
  public Path getAccountsFile() {
    return accountsFile;
  }

  /**
   * Returns the path of transactions.txt.
   *
   * @return the transactions file path
   */
  public Path getTransactionsFile() {
    return transactionsFile;
  }

  private <T> LoadResult<T> readRecords(Path file, Function<String, Optional<T>> parser)
      throws PersistenceException {
    if (!Files.exists(file)) {
      return new LoadResult<>(List.of(), 0);
    }
    try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
      List<Optional<T>> parsed = lines
          .map(String::trim)
          .filter(line -> !line.isEmpty() && !line.startsWith("#"))
          .map(parser)
          .collect(Collectors.toList());
      List<T> items = parsed.stream().flatMap(Optional::stream).collect(Collectors.toList());
      return new LoadResult<>(items, parsed.size() - items.size());
    } catch (IOException e) {
      throw new PersistenceException(
          "Could not read " + file.getFileName() + ": " + e.getMessage());
    }
  }

  private void writeLines(Path file, String header, Stream<String> records)
      throws PersistenceException {
    List<String> lines = new ArrayList<>();
    lines.add(header);
    records.forEach(lines::add);
    try {
      Files.createDirectories(file.toAbsolutePath().getParent());
      Files.write(file, lines, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new PersistenceException(
          "Could not write " + file.getFileName() + ": " + e.getMessage());
    }
  }
}
