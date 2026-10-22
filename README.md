# Bank Account Management System III — Week 3

A console-based banking application in Java. Week 3 builds on Week 2 and adds modern Java
features: **Java Collections**, **Functional Programming** (lambdas, functional interfaces,
method references, Streams), **File I/O with NIO**, **Regular Expressions** and
**Concurrency** with threads and synchronized methods.

**Author:** Annick Ujeneza
**Week 2 repository:** https://github.com/123annick/Bank-Account-Management-II

## Features

- **Manage Accounts:** create savings or checking accounts with regex-validated name, email, phone and address; view all accounts
- **Perform Transactions:** deposit, withdraw and transfer, each with a confirmation prompt
- **Generate Account Statements:** four views built with Streams: latest first, largest amount first, deposits only, withdrawals only
- **Save/Load Data:** save to or reload from `data/accounts.txt` and `data/transactions.txt`
- **Run Concurrent Simulation:** three threads and a parallel stream deposit and withdraw on one account at the same time, then the final balance is checked
- **Persistence:** data loads automatically on startup and saves automatically on exit
- **All Week 2 features retained:** custom exceptions, savings minimum balance, checking overdraft, premium fee waiver, transfers, statements

```
╔════════════════════════════════════════════╗
║      BANK ACCOUNT MANAGEMENT SYSTEM        ║
╚════════════════════════════════════════════╝

Main Menu:
-----------
1. Manage Accounts
2. Perform Transactions
3. Generate Account Statements
4. Save/Load Data
5. Run Concurrent Simulation
6. Exit
```

## Project structure

```
Bank-Account-Management-III/
├── pom.xml
├── data/
│   ├── accounts.txt            5 sample accounts
│   └── transactions.txt        13 sample transactions
├── docs/
│   ├── collections-architecture.md
│   └── git-workflow.md
├── src/
│   ├── main/java/
│   │   ├── Main.java
│   │   ├── models/             Account, SavingsAccount, CheckingAccount, Customer,
│   │   │   │                   RegularCustomer, PremiumCustomer, Transaction, ...
│   │   │   └── exceptions/     BankException and 6 custom exceptions
│   │   ├── services/           AccountManager, TransactionManager, FilePersistenceService,
│   │   │                       RecordMapper, StatementGenerator, TransactionSimulator, ...
│   │   ├── utils/              ValidationUtils, ConcurrencyUtils, FunctionalUtils,
│   │   │                       FormatUtils, IdGenerator
│   │   └── ui/                 BankConsoleApp, one class per menu, ConsoleInput/Output
│   └── test/java/              9 JUnit 5 test classes (72 tests)
└── README.md
```

The code keeps the Maven standard layout from Week 2 (`src/main/java`, `src/test/java`) so
IntelliJ downloads JUnit 5 automatically. All lab classes are in the packages the lab names.

## 1. Collections

| Data | Collection | Where | Why |
| --- | --- | --- | --- |
| Accounts | `HashMap<String, Account>` | `AccountManager` | Lookup by account number takes the same time however many accounts exist (O(1)), instead of the Week 2 linear search (O(n)) |
| Transactions | `ArrayList<Transaction>` | `TransactionManager` | Keeps the order transactions happened and grows without a fixed limit (the Week 2 array stopped at 200) |
| Menu options | `List<MenuAction>` | `BankConsoleApp` | `List.of(...)` builds a fixed, read-only list |

Week 2's `StorageFullException` was removed: collections grow as needed.

## 2. Functional programming

| Feature | Example in the code | Where |
| --- | --- | --- |
| Lambda | `account -> account.getBalance() > 1000` | `AccountManagerTest`, `AccountRepository.findAccounts` |
| `Predicate<T>` | `IS_CREDIT`, `IS_EMAIL`, `belongsTo(accountNumber)` | `FunctionalUtils`, `ValidationUtils` |
| `Comparator<T>` | `BY_DATE_NEWEST_FIRST`, `BY_AMOUNT_LARGEST_FIRST` | `FunctionalUtils` |
| `Function<T, R>` | the line parser passed to `readRecords` | `FilePersistenceService` |
| `Consumer<T>` | the simulation log `output::println` | `TransactionSimulator`, `Main` |
| `BooleanSupplier` | `dataMenu::save` runs on exit | `BankConsoleApp`, `Main` |
| Custom functional interface | `BankOperation` (a lambda that may throw a bank error) | `services/BankOperation` |
| Method references | `Account::getBalance`, `mapper::toAccount`, `Optional::stream`, `Thread::start`, `this::printAccountRow` | throughout |
| Streams: `filter`, `sorted`, `map`, `collect` | statements and account lists | `FunctionalUtils.filterAndSort`, `AccountManager.findAccounts` |
| Streams: `reduce` | `.map(Transaction::getAmount).reduce(0.0, Double::sum)` | `FunctionalUtils.sumAmounts` |
| Streams: `mapToDouble().sum()` | total bank balance | `AccountManager.getTotalBalance` |

```java
transactions.stream()
    .filter(FunctionalUtils.IS_CREDIT)
    .sorted(FunctionalUtils.BY_AMOUNT_LARGEST_FIRST)
    .collect(Collectors.toList());
```

## 3. File I/O with NIO

- `FilePersistenceService` uses `Path`, `Paths.get("data")`, `Files.lines`, `Files.write` and `Files.createDirectories`.
- Each line is mapped to an object with a method reference: `lines.map(mapper::toAccount)`. A broken line becomes `Optional.empty()` and is skipped and counted instead of crashing the load.
- `Files.lines` runs inside try-with-resources so the file is always closed.
- `IOException` is turned into the custom checked `PersistenceException` with a clear message.
- If `accounts.txt` is missing or empty, the program creates both files with 5 sample accounts.
- IDs continue after the highest loaded ID, so new accounts and transactions never repeat one.

File format (one record per line, fields separated by `|`, lines starting with `#` are headers):

```
ACC001|SAVINGS|Active|6250.00|CUS001|REGULAR|John Smith|40|john.smith@bank.com|+1-555-0001|1 Main St
TXN001|ACC001|DEPOSIT|1500.00|6750.00|2026-10-15T09:00:00
```

## 4. Regular expressions

All patterns are in `ValidationUtils`, compiled once as `Pattern` constants and checked with
`pattern.matcher(text).matches()` inside reusable `Predicate<String>` rules.

| Field | Pattern | Valid | Invalid |
| --- | --- | --- | --- |
| Account number | `^ACC\d{3}$` | `ACC001` | `ACC99`, `ACC1234` |
| Email | `^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$` | `john.smith@bank.com` | `john.smith@bank` |
| Phone | `^\+?[0-9][0-9 -]{5,13}[0-9]$` | `+1-555-0001` | `0788` |
| Name | `^[A-Za-z][A-Za-z .'-]{1,49}$` | `Mary-Jane O'Neil` | `J`, `John\|Smith` |
| Address | `^[A-Za-z0-9][A-Za-z0-9 ,.'#/-]{2,79}$` | `1 Main St` | `\|` characters |

The lab's email pattern `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$` accepts `john.smith@bank`, but the
lab's own example expects that address to be rejected. The pattern above keeps the same
characters and also requires a domain ending such as `.com`.

`ConsoleInput.readValidated(prompt, rule, message)` repeats the question until the answer
passes the rule. `AccountFactory` checks the same rules again, so invalid data can never reach
the files.

## 5. Concurrency

- `Account.deposit`, `withdraw`, `checkWithdrawal`, `getBalance` and `updateBalance` are `synchronized`, so the check and the balance change happen as one step per account.
- `TransactionManager.deposit`, `withdraw`, `transfer` and its list readers are `synchronized`, so the balance change and its record are added together and the `ArrayList` is never changed by two threads at once.
- `ConcurrencyUtils.runInParallelThreads` starts one `Thread` per `Runnable` (named Thread-1, Thread-2, ...) and waits with `join()`.
- `TransactionSimulator` runs 2 deposits and 1 withdrawal on three threads, then 10 deposits on a parallel stream (`IntStream.rangeClosed(1, 10).parallel()`), and checks that the final balance equals the starting balance plus every recorded transaction.

```
Running concurrent transaction simulation...

Thread-1: Depositing $500.00 to ACC001
Thread-3: Withdrawing $200.00 from ACC001
Thread-2: Depositing $300.00 to ACC001
...
✓ Thread-safe operations completed successfully.
Final Balance for ACC001: $6,850.00
```

## Setup in IntelliJ IDEA

1. **File > New > Project from Version Control**, paste the repository URL.
2. Click **Load Maven Project** when asked and wait for JUnit 5 to download.
3. Make sure **File > Project Structure > SDK** is JDK 17 or newer.
4. Run `src/main/java/Main.java`. The working directory must be the project folder so the program finds `data/`; this is IntelliJ's default.

## Running the tests

Right-click `src/test/java` > **Run 'All Tests'**. 72 tests in 9 classes, all passing:

| Test class | Tests | Covers |
| --- | --- | --- |
| `AccountTest` | 14 | deposit, withdraw, minimum balance, overdraft, fees, interest, rounding |
| `TransactionManagerTest` | 12 | transactions with `ArrayList`, transfer, IDs continue after loaded data |
| `ExceptionTest` | 11 | every custom exception and its message |
| `RegexValidationTest` | 8 | account number, email, phone and name patterns |
| `FunctionalUtilsTest` | 7 | sorting 10 transactions by amount and date, `reduce` totals, filters |
| `AccountManagerTest` | 6 | `HashMap` lookup, sorting, predicates, `replaceAll` |
| `FilePersistenceServiceTest` | 5 | save/load round trip, missing files, damaged lines, ID continuation |
| `StatementGeneratorTest` | 5 | empty history, newest first, totals, deposits-only view |
| `ConcurrencyTest` | 4 | 20 threads, parallel stream, concurrent withdrawals, the simulator |

## Test scenarios from the lab

| Scenario | How to check |
| --- | --- |
| 1. Collections and Streams | Use menus 1–3; see `AccountManager` and `TransactionManager` |
| 2. Round trip | Create 2 accounts and 3 transactions, choose 4 > 1 (save), exit, run again, open a statement |
| 3. Regex | Create an account and type `john.smith@bank`, then `john.smith@bank.com` |
| 4. Concurrent deposits | Menu 5 with `ACC001` |
| 5. Stream sorting | Menu 3, view 2 (by amount) or 1 (by date); `FunctionalUtilsTest` sorts 10 transactions |
| 6. File errors | Delete `data/accounts.txt` and run: it is recreated with sample accounts |
| 7. Threads and persistence | Menu 5, exit (auto-save), run again, open the statement |
| 8. `reduce()` | Statement totals; `FunctionalUtilsTest.reduceAddsUpDepositsCorrectly` |
| 9. Code style | 2-space Google style, JavaDoc on public methods, methods of 25 lines or fewer |

## Changes from Week 2

| Week 2 | Week 3 |
| --- | --- |
| `Account[50]` with linear search | `HashMap<String, Account>` |
| `Transaction[200]` | `ArrayList<Transaction>` |
| Insertion sort in `TransactionSorter` | `Comparator` + `stream().sorted()` in `FunctionalUtils` |
| Loops to add totals | `reduce` and `mapToDouble().sum()` |
| Data lost on exit | Saved to `data/*.txt` and loaded on startup |
| Only empty-text checks | Regex validation for every customer field and account number |
| Single thread | Synchronized methods, threads and parallel streams |
| Menu option "Run Tests" | Replaced by "Save/Load Data" and "Run Concurrent Simulation" as the Week 3 menu shows; the JUnit suite runs in IntelliJ |

## Git

The history continues from Week 2 and follows GitFlow: each phase was built on a
`feature/*` branch created from `develop` and merged back into `develop`, in the lab's
order: collections → functional → I/O → regex → threads. See
[`docs/git-workflow.md`](docs/git-workflow.md).

## Ready for Week 4

`AccountRepository` and `TransactionHistory` are interfaces, and all file code is in
`FilePersistenceService`. A database version can replace the file service without changing
the menus or the models.
