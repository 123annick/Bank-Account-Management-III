# Code Map — where everything is

Use this page to find any feature in seconds during a review. Paths are under
`src/main/java/` unless they start with `src/test/java/`.

## Start here

| Question | File → method |
| --- | --- |
| Where does the program start? | `Main.main` — builds every object, loads data, starts the menu |
| Where is the main menu loop? | `ui/BankConsoleApp.run` → `runMenuLoop` → `showMainMenu` |
| Where are the menu options listed? | `Main.main`, the `List.of(...)` of `MenuAction`s |
| Where is the exit message and auto-save? | `ui/BankConsoleApp.printGoodbye`, called with `saveOnExit` (`dataMenu::save`) |

## Menu 1 — Manage Accounts

| Feature | File → method |
| --- | --- |
| Sub-menu | `ui/AccountMenu.execute` |
| Typing customer details (regex) | `ui/AccountMenu.readCustomerDetails` |
| Opening deposit with retry | `ui/AccountMenu.openAccount` |
| Opening rules ($500 savings, $10,000 premium) | `services/AccountFactory.validateInitialDeposit` |
| Details checked again, age check | `services/AccountFactory.validateDetails` |
| 999-account limit | `services/AccountFactory.validateCapacity` |
| New IDs ACC/CUS/TXN | `utils/IdGenerator.next`, `skipPast` |
| Storing accounts (`HashMap`) | `services/AccountManager.addAccount`, `findAccount` |
| Account list sorted by number | `services/AccountManager.findAccounts` / `getAllAccounts` |
| Total bank balance (stream) | `services/AccountManager.getTotalBalance` |

## Menu 2 — Perform Transactions

| Feature | File → method |
| --- | --- |
| Pick account (regex `ACC\d{3}`, 0 to cancel) | `ui/AccountSelector.selectAccount` |
| Deposit / withdraw / transfer screens | `ui/TransactionMenu.deposit`, `withdraw`, `transfer` |
| Amount typed and checked | `ui/ConsoleInput.readAmount` → `ui/TransactionMenu.readValidAmount` |
| Confirmation screen | `ui/TransactionMenu.confirmTransaction` |
| Thread-safe deposit / withdraw / transfer | `services/TransactionManager.deposit`, `withdraw`, `transfer` |
| Balance change (synchronized) | `models/Account.updateBalance` |
| Savings $500 minimum | `models/SavingsAccount.validateWithdrawalRules` |
| Checking -$1,000 overdraft | `models/CheckingAccount.validateWithdrawalRules` |
| Premium fee waiver | `models/CheckingAccount` constructor → `Customer.hasWaivedFees` |

## Menu 3 — Generate Account Statements

| Feature | File → method |
| --- | --- |
| Choosing one of 4 views | `ui/StatementMenu.execute`, `buildStatement` |
| Building the statement | `services/StatementGenerator.generateStatement` |
| Filter + sort with streams | `utils/FunctionalUtils.filterAndSort` |
| Sort by date / amount | `utils/FunctionalUtils.BY_DATE_NEWEST_FIRST`, `BY_AMOUNT_LARGEST_FIRST` |
| Deposits / withdrawals filter | `utils/FunctionalUtils.IS_CREDIT`, `IS_DEBIT` |
| Totals with `reduce` | `utils/FunctionalUtils.sumAmounts` |

## Menu 4 — Save/Load Data

| Feature | File → method |
| --- | --- |
| Save / reload sub-menu | `ui/DataMenu.execute`, `save`, `load` |
| Load at startup, sample data if missing | `ui/DataMenu.loadOnStartup`, `createSampleData` |
| Read files with `Files.lines` | `services/FilePersistenceService.readRecords` |
| Write files with `Files.write` | `services/FilePersistenceService.writeLines` |
| Line ↔ object, file validation | `services/RecordMapper.toLine`, `toAccount`, `toTransaction`, `hasValidAccountFields` |
| Rebuilding loaded accounts | `services/AccountFactory.restoreAccount` |
| Data files | `data/accounts.txt`, `data/transactions.txt` |

## Menu 5 — Run Concurrent Simulation

| Feature | File → method |
| --- | --- |
| Menu and report | `ui/SimulationMenu.execute`, `printReport` |
| 3 threads + parallel stream | `services/TransactionSimulator.run` |
| Starting and joining threads | `utils/ConcurrencyUtils.runInParallelThreads` |
| Custom functional interface | `services/BankOperation` |
| Consistency check | `services/SimulationReport.isConsistent` |

## Validation and exceptions

| Feature | File → method |
| --- | --- |
| All regex patterns and rule messages | `utils/ValidationUtils` (top of the file) |
| Regex → `Predicate` | `utils/ValidationUtils.matches` |
| Amount value rules (> 0, ≤ $1,000,000, 2 decimals) | `utils/ValidationUtils.validateAmount` |
| Age rule | `utils/ValidationUtils.validateAge` |
| Ask again until valid | `ui/ConsoleInput.readValidated`, `readInt`, `readAmount`, `confirm` |
| Unchecked → message (`NumberFormatException`) | `ui/ConsoleInput.parseInt` |
| Input closed (`NoSuchElementException`) | `ui/BankConsoleApp.run` |
| `IOException` → `PersistenceException` | `services/FilePersistenceService.readRecords`, `writeLines` |
| Custom exceptions | `models/exceptions/` |

## Tests

| What is proven | Test class |
| --- | --- |
| Deposit, withdraw, overdraft, minimum balance | `src/test/java/AccountTest` |
| Transfers, records, IDs | `src/test/java/TransactionManagerTest` |
| Custom and Java exception types | `src/test/java/ExceptionTest` |
| Every regex | `src/test/java/RegexValidationTest` |
| Amount, age, name, account-limit rules | `src/test/java/ValidationRulesTest` |
| Console asks again after bad input | `src/test/java/ConsoleInputTest` |
| Save/load round trip, bad file lines | `src/test/java/FilePersistenceServiceTest` |
| Stream sorting and `reduce` | `src/test/java/FunctionalUtilsTest` |
| `HashMap` storage | `src/test/java/AccountManagerTest` |
| Statements | `src/test/java/StatementGeneratorTest` |
| Thread safety | `src/test/java/ConcurrencyTest` |

## Finding things in IntelliJ without searching the whole project

| Shortcut (Windows) | Opens |
| --- | --- |
| `Ctrl+N` | A class by name, e.g. type `TransMan` for `TransactionManager` |
| `Ctrl+F12` | The list of methods in the open file; type to jump |
| `Ctrl+B` or `Ctrl+Click` | The definition of the method or class under the cursor |
| `Alt+F7` | Every place a method is used |
| `Ctrl+E` | Recently opened files |
