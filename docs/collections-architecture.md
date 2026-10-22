# Collections Architecture

This document explains how data is stored, processed, saved and protected in Week 3, and
why each structure was chosen.

## 1. Layers

```
ui        menus, keyboard input, console output
  │
services  AccountManager (HashMap)  TransactionManager (ArrayList)
  │       StatementGenerator  FilePersistenceService  TransactionSimulator
  │
models    Account, Customer, Transaction (+ exceptions)
  │
utils     ValidationUtils (regex)  FunctionalUtils (streams)  ConcurrencyUtils (threads)
```

Each layer only calls the layers below it. `Main` creates every object once and passes them in
through constructors.

## 2. Where the data lives

| Data | Type | Owner | Key / order |
| --- | --- | --- | --- |
| Accounts | `Map<String, Account>` backed by `HashMap` | `AccountManager` | key = account number in upper case |
| Transactions | `List<Transaction>` backed by `ArrayList` | `TransactionManager` | order in which they happened |
| Menu options | `List<MenuAction>` from `List.of` | `BankConsoleApp` | display order |

The rest of the program never sees the map or the list directly. It uses the interfaces
`AccountRepository` and `TransactionHistory`, and every method that returns data returns a
**new** list, so callers cannot change the stored collections by accident.

## 3. Why these collections

| Operation | Week 2 (array) | Week 3 (collection) |
| --- | --- | --- |
| Find account by number | Linear search, O(n) | `HashMap.get`, O(1) on average |
| Add account | O(1), fails when 50 are stored | `HashMap.put`, O(1), no limit |
| Detect duplicate account number | Not checked | `containsKey`, O(1) |
| Add transaction | O(1), fails at 200 | `ArrayList.add`, amortised O(1), no limit |
| Transactions of one account | Two loops (count, then copy) | One stream `filter` + `collect`, O(n) |
| Sort transactions | Hand-written insertion sort | `stream().sorted(comparator)`, O(n log n), stable |
| Total deposits | Loop with `if` | `filter` + `map` + `reduce`, O(n) |

**Why `HashMap` and not `TreeMap`?** Lookups are the most common action, and `HashMap` is
faster for them. A sorted list is only needed when showing all accounts, so
`getAllAccounts()` sorts the values with a `Comparator` at that moment.

**Why `ArrayList` and not `LinkedList`?** Transactions are only added at the end and read in
order; `ArrayList` does both quickly and uses less memory.

## 4. Stream pipelines

| Purpose | Pipeline | Where |
| --- | --- | --- |
| Account list, sorted | `values().stream().filter(rule).sorted(BY_ACCOUNT_NUMBER).collect(toList())` | `AccountManager.findAccounts` |
| Total bank balance | `values().stream().mapToDouble(Account::getBalance).sum()` | `AccountManager.getTotalBalance` |
| One account's history | `stream().filter(belongsTo(number)).collect(toList())` | `TransactionManager.getTransactionsFor` |
| Statement views | `stream().filter(rule).sorted(order).collect(toList())` | `FunctionalUtils.filterAndSort` |
| Totals | `stream().filter(rule).map(Transaction::getAmount).reduce(0.0, Double::sum)` | `FunctionalUtils.sumAmounts` |
| Loading a file | `Files.lines(file).map(String::trim).filter(...).map(parser)` then `flatMap(Optional::stream)` | `FilePersistenceService.readRecords` |
| Saving a file | `accounts.stream().map(mapper::toLine)` | `FilePersistenceService.saveAccounts` |
| Batch simulation | `IntStream.rangeClosed(1, 10).parallel().forEach(...)` | `TransactionSimulator.run` |

### Comparators

```java
BY_DATE_NEWEST_FIRST = Comparator.comparing(Transaction::getTimestamp)
    .thenComparingInt(t -> IdGenerator.numberOf(t.getTransactionId()))
    .reversed();
BY_AMOUNT_LARGEST_FIRST = Comparator.comparingDouble(Transaction::getAmount).reversed();
```

The second key in `BY_DATE_NEWEST_FIRST` keeps the order correct when two transactions have
the same timestamp: the later-recorded one comes first.

## 5. Persistence round trip

```
startup:  data/accounts.txt ──Files.lines──▶ mapper::toAccount ──▶ HashMap
          data/transactions.txt ──Files.lines──▶ mapper::toTransaction ──▶ ArrayList
exit:     HashMap.values() ──map(mapper::toLine)──▶ Files.write ──▶ data/accounts.txt
          ArrayList ──map(mapper::toLine)──▶ Files.write ──▶ data/transactions.txt
```

- `RecordMapper` is the only class that knows the text format, so changing the format (or
  moving to a database) changes one class.
- Restored accounts go through `AccountFactory.restoreAccount`, which also moves the ID
  counters past every loaded ID.
- A missing or empty `accounts.txt` creates both files with 5 sample accounts.
- Damaged lines are skipped and reported, not fatal.

## 6. Thread safety

| Shared thing | Protection | Why |
| --- | --- | --- |
| An account's balance | `synchronized` methods on `Account` (`deposit`, `withdraw`, `checkWithdrawal`, `getBalance`, `updateBalance`) | The rule check and the balance change happen as one step; two withdrawals cannot both pass the check |
| The transaction `ArrayList` | `synchronized` methods on `TransactionManager` | `ArrayList` is not thread-safe; only one thread adds at a time, and the record always matches the balance |
| ID counters | `synchronized` methods on `IdGenerator` | Two threads can never get the same transaction ID |
| The account `HashMap` | Only the main thread changes it | The simulation changes balances, not the map |

A transfer is one `synchronized` method on `TransactionManager`, so it never takes two
account locks in different orders, which avoids deadlock.

`ConcurrencyTest` proves the design: 20 threads making 100 deposits each lose no money, a
parallel stream of 500 deposits is exact, and 10 threads withdrawing at the same time never
break the $500 minimum balance.
