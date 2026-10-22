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
│   └── test/java/              11 JUnit 5 test classes (97 tests)
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

## 4. Regular expressions and input validation

Every value typed by the user, and every line read from a data file, is checked before it is
used. All patterns are in `ValidationUtils`, compiled once as `Pattern` constants and checked
with `pattern.matcher(text).matches()` inside reusable `Predicate<String>` rules.

| Input | Rule | Accepted | Rejected |
| --- | --- | --- | --- |
| Account number | `^ACC\d{3}$` (lower case is converted first) | `ACC001`, `acc001` | `ACC99`, `ACC1234`, `abc` |
| Customer name | `^(?=.{2,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$`: letters only, words joined by one space, hyphen or apostrophe | `Ann Uwase`, `Mary-Jane O'Neil` | `12345`, `John3`, `Ann  Lee`, `-Ann`, `Ann_Lee` |
| Age | whole number from 18 to 120 | `30` | `abc`, `1.5`, `15`, `200` |
| Email | `^(?=.{6,100}$)(?!.*\.\.)[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$` | `john.smith@bank.com` | `john.smith@bank`, `ann@bank..com` |
| Phone | `^\+?(?:[0-9][ -]?){6,14}[0-9]$`: 7 to 15 digits, single spaces or dashes | `+1-555-0001`, `+250 788 123 456` | `0788`, `phone123`, `+1--555` |
| Address | `^(?=.{3,80}$)(?=.*[A-Za-z])[A-Za-z0-9][A-Za-z0-9 ,.'#/-]*$`: needs a letter | `12 Main St` | `12345`, anything with `\|` |
| Money (typed) | `^-?\$?(?:\d{1,3}(?:,\d{3})+\|\d+)(?:\.\d{1,2})?$` | `1500`, `1,500.50`, `$20.5` | `abc`, `1e3`, `10.555`, `1,50` |
| Money (value) | greater than 0, at most $1,000,000.00, at most 2 decimals | `0.01`, `1000000` | `0`, `-200`, `1000000.01` |
| Menu choices | whole number in the menu's range | `1`–`6` | `abc`, `7`, `1.5` |
| Yes/No | `Y`, `YES`, `N`, `NO` in any case | `y`, `Yes` | `maybe` |
| Any text | must not be blank | | empty or spaces only |

**How it works:**

- `ConsoleInput.readValidated(prompt, rule, message)` repeats the question until the answer
  passes the rule, so the program never crashes and the user always sees why.
- `ConsoleInput.readAmount` checks the money format first; `ValidationUtils.validateAmount`
  then checks the value (so `-200` still shows "Amount must be greater than 0").
- `AccountFactory` checks name, email, phone, address and age **again** and throws
  `InvalidInputException`, so invalid data can't reach the bank from any path.
- `RecordMapper` applies the same rules to every line in the data files; a line that breaks
  any rule is skipped and reported, not loaded.
- `AccountFactory` refuses a 1,000th account because `ACC1000` would break the `ACC\d{3}`
  pattern.
- Closing the input (Ctrl+D) is caught in `BankConsoleApp`, which saves and exits safely.

The lab's email pattern `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$` accepts `john.smith@bank`, but the
lab's own example expects that address to be rejected. The pattern above keeps the same
characters and also requires a domain ending such as `.com`.

## Errors and exceptions

Everything that can be thrown in Java is a `Throwable`. It has two branches:

```
Throwable
├── Error                      serious JVM problems, never caught by this program
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception                  CHECKED: the compiler forces catch or throws
    ├── IOException            Java  → caught in FilePersistenceService
    ├── InterruptedException   Java  → caught in SimulationMenu
    ├── BankException          mine  → caught in every menu
    │   ├── InvalidAmountException
    │   ├── InsufficientFundsException
    │   ├── OverdraftExceededException
    │   ├── InvalidAccountException
    │   ├── InvalidInputException
    │   └── PersistenceException
    └── RuntimeException       UNCHECKED: the compiler does not force handling
        ├── NumberFormatException     Integer.parseInt("abc") → ConsoleInput
        ├── InputMismatchException    wrong menu input → ConsoleInput
        ├── NoSuchElementException    input closed (Ctrl+D) → BankConsoleApp
        ├── IllegalArgumentException  bad enum or number in a data file → RecordMapper
        └── DateTimeParseException    bad date in a data file → RecordMapper
```

| | Error | Checked exception | Unchecked exception |
| --- | --- | --- | --- |
| Extends | `Error` | `Exception` (not `RuntimeException`) | `RuntimeException` |
| Meaning | The JVM itself is in trouble | An expected problem the program should recover from | Usually a programming mistake or invalid data |
| Compiler | Nothing forced | Must `catch` or declare `throws` | Nothing forced |
| Should you catch it? | No: the program can't fix it | Yes, where you can tell the user | Only where you expect it, such as parsing input |
| In this project | Not caught on purpose | `BankException` family, `IOException`, `InterruptedException` | `NumberFormatException`, `InputMismatchException`, `NoSuchElementException`, `IllegalArgumentException` |

My bank exceptions are **checked** because a failed withdrawal or a bad email is an expected
event, not a bug, and the compiler should force every caller to handle it.
`ExceptionTest.javaExceptionTypesMatchHowTheCodeHandlesThem` checks this hierarchy.

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

Right-click `src/test/java` > **Run 'All Tests'**. 97 tests in 11 classes, all passing:

| Test class | Tests | Covers |
| --- | --- | --- |
| `RegexValidationTest` | 15 | every regex: valid and invalid account numbers, names, emails, phones, addresses, money |
| `AccountTest` | 14 | deposit, withdraw, minimum balance, overdraft, fees, interest, rounding |
| `ExceptionTest` | 12 | every custom exception, its message, and checked vs unchecked types |
| `TransactionManagerTest` | 12 | transactions with `ArrayList`, transfer, IDs continue after loaded data |
| `ValidationRulesTest` | 8 | amount limit and decimals, age range, names with numbers, 999-account limit |
| `ConsoleInputTest` | 7 | the console asks again after bad names, amounts, numbers, blank text; safe exit |
| `FilePersistenceServiceTest` | 7 | round trip, missing files, damaged and invalid records, ID continuation |
| `FunctionalUtilsTest` | 7 | sorting 10 transactions by amount and date, `reduce` totals, filters |
| `AccountManagerTest` | 6 | `HashMap` lookup, sorting, predicates, `replaceAll` |
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

## Git: GitFlow with pull requests

The repository follows **GitFlow**:

| Branch | Purpose |
| --- | --- |
| `main` | Released versions only. Week 2 is the last release until `v3.0.0` |
| `develop` | Integration branch: every finished feature is merged here first |
| `feature/*` | One branch per Week 3 phase, created from `develop` and merged back into it |
| `release/v3.0.0` | Created from `develop` to prepare the release, then merged into `main` and tagged |
| `hotfix/*` | Urgent fixes made from `main` (none needed yet) |

The five phase branches (collections → functional → I/O → regex → threads) are merged into
`develop`. The input-validation improvements are on `feature/input-validation`, ready to be
merged into `develop` through a **pull request** on GitHub, followed by a release pull request
into `main`. Every step and command is in [`docs/git-workflow.md`](docs/git-workflow.md).

## What is and isn't implemented

| Implemented | Not implemented (known limits) |
| --- | --- |
| Create accounts (savings, checking; regular, premium) | Closing or deleting an account |
| Deposit, withdraw, transfer with confirmation | Editing customer details after creation |
| Statements with 4 stream-based views | Interest is calculated but never added to the balance automatically |
| Save, reload, auto-load, auto-save | Monthly fee exists (`applyMonthlyFee`) but no menu applies it |
| Regex validation of every input and file record | A customer can't own several accounts; each account has its own customer |
| Thread-safe transactions and a concurrency simulation | Money uses `double` rounded to cents, not `BigDecimal` |
| 97 JUnit tests | No database: data is in text files (planned for a later week) |
| GitFlow branches | Pull requests must be opened by you on GitHub (steps in `docs/git-workflow.md`) |
| Up to 999 accounts (`ACC001`–`ACC999`, the lab's pattern) | More than 999 accounts |

For a quick "where is what" list, see [`docs/code-map.md`](docs/code-map.md).

## Ready for Week 4

`AccountRepository` and `TransactionHistory` are interfaces, and all file code is in
`FilePersistenceService`. A database version can replace the file service without changing
the menus or the models.
