# Git Workflow — Bank Account Management System

## Week 3 workflow: GitFlow

Week 3 follows **GitFlow**, a branching model with two long-lived branches and three kinds of
short-lived branches.

| Branch | Lives | Created from | Merged into | Purpose |
| --- | --- | --- | --- | --- |
| `main` | Forever | — | — | Only released, tested versions. Each release is tagged (`v3.0.0`) |
| `develop` | Forever | `main` | `release/*` | Where finished features come together before a release |
| `feature/*` | Until merged | `develop` | `develop` | One new feature or phase |
| `release/*` | Until released | `develop` | `main` and back into `develop` | Final checks and version number for a release |
| `hotfix/*` | Until merged | `main` | `main` and `develop` | Urgent fix to a released version |

```
main     ●──────────────────────────────────────────────●  v3.0.0
          \                                            /
develop    ●────●────●────●────●────●────●────────────●
                 \  / \  / \  / \  / \  / \          /
feature/*         ●●   ●●   ●●   ●    ●●   ●●●●●●●  (release/v3.0.0)
```

**Why GitFlow instead of committing to `main`:** `main` always holds a version that was
released and tested, so a reviewer or user can trust it. Work in progress lives on
`develop` and feature branches, and nothing reaches `main` without a release step.

### How Week 3 was built

The repository starts from the full Week 2 history. `develop` was created from `main`, and
each phase was built on a feature branch and merged into `develop` with `--no-ff`, in the
lab's order: collections → functional → I/O → regex → threads.

```bash
git checkout main
git checkout -b develop

git checkout -b feature/collections develop
git add src
git commit -m "Replace arrays with HashMap and ArrayList in AccountManager and TransactionManager"
git checkout develop
git merge --no-ff feature/collections
```

| Phase | Branch | Commit | Message |
| --- | --- | --- | --- |
| 1. Collections | `feature/collections` | `0acdb39` | Replace arrays with HashMap and ArrayList in AccountManager and TransactionManager |
|  | `develop` | `e984e16` | Merge branch 'feature/collections' into develop |
| 2. Functional | `feature/functional` | `4a9d2e9` | Add FunctionalUtils with comparators, predicates and reduce |
|  | `feature/functional` | `630391c` | Sort and filter statements with Streams and Lambdas |
|  | `develop` | `cdfc45d` | Merge branch 'feature/functional' into develop |
| 3. File I/O | `feature/file-io` | `0573576` | Add FilePersistenceService using NIO Files, Paths and Files.lines |
|  | `feature/file-io` | `dbf650d` | Add Save/Load menu with auto-load on startup and auto-save on exit |
|  | `develop` | `52afa76` | Merge branch 'feature/file-io' into develop |
| 4. Regex | `feature/regex` | `edf81f0` | Add regex validation with Pattern, Matcher and Predicate in ValidationUtils |
|  | `develop` | `253cded` | Merge branch 'feature/regex' into develop |
| 5. Concurrency | `feature/concurrency` | `ce489b1` | Make balance updates thread-safe with synchronized methods |
|  | `feature/concurrency` | `1d7d910` | Add concurrent simulation with Threads, Runnable and parallel streams |
|  | `develop` | `460d971` | Merge branch 'feature/concurrency' into develop |
| Docs | `feature/documentation` | (see `git log`) | Document Collections, Streams, File I/O, Regex and Concurrency |
|  | `develop` | (see `git log`) | Merge branch 'feature/documentation' into develop |

`--no-ff` always records a merge commit, so the history shows exactly where each feature
joined `develop`.

## Pull requests: finishing Week 3 on GitHub

A **pull request (PR)** is a request on GitHub to merge one branch into another. It shows
the changes, lets a reviewer comment line by line, and records the review. In a team,
nothing is merged into `develop` or `main` without a PR.

The input-validation work (based on my Week 2 review) is on `feature/input-validation` and
is **not merged yet**. It goes through two pull requests:

```
feature/input-validation ──PR #1──▶ develop ──▶ release/v3.0.0 ──PR #2──▶ main (tag v3.0.0)
                                                         └──────────────▶ develop (back-merge)
```

### Step 1 — Push everything

```bash
git remote add origin https://github.com/123annick/Bank-Account-Management-III.git
git push -u origin --all
```

On GitHub, open **Settings → General → Default branch** and set it to `develop`, so new pull
requests target it by default.

### Step 2 — Pull request #1: `feature/input-validation` → `develop`

1. On GitHub, open **Pull requests → New pull request**.
2. Set **base: `develop`** and **compare: `feature/input-validation`**.
3. Title: `Strengthen input validation`
4. Description:

```
## What
- Names accept letters only (no numbers or symbols); phone needs 7-15 digits;
  address needs a letter; email rejects double dots
- Money input: format check (no letters, 1e3 or 3 decimals), $1,000,000 limit
- Age 18-120 checked again in AccountFactory; 999-account limit
- Every line in the data files is validated when loading
- Closing the input (Ctrl+D) saves and exits safely; Y/YES/N/NO accepted
- Docs: validation rules, errors vs exceptions, GitFlow, code map

## Why
Week 2 review: improve input validation and explain checked vs unchecked exceptions.
A name such as "12345" used to be accepted.

## How to test
- Run all tests: 97 passing
- Create an account with name 12345 -> error, then Ann Uwase -> accepted
```

5. Click **Create pull request**, open the **Files changed** tab and read through the changes
   (this is the code review).
6. Click **Merge pull request** → **Confirm merge**. Keep the branch or delete it.

### Step 3 — Release branch

```bash
git checkout develop
git pull
git checkout -b release/v3.0.0
```

Open `pom.xml`, change `<version>2.0.0</version>` to `<version>3.0.0</version>`, run all
tests, then:

```bash
git add pom.xml
git commit -m "Prepare release v3.0.0"
git push -u origin release/v3.0.0
```

### Step 4 — Pull request #2: `release/v3.0.0` → `main`

1. **New pull request** with **base: `main`** and **compare: `release/v3.0.0`**.
2. Title: `Release v3.0.0 — Week 3`
3. Description: `Collections, Streams, File I/O, Regex, Concurrency and input validation. 97 tests passing.`
4. **Create pull request** → **Merge pull request** → **Confirm merge**.

### Step 5 — Tag the release and update `develop`

```bash
git checkout main
git pull
git tag -a v3.0.0 -m "Week 3 release"
git push origin v3.0.0

git checkout develop
git merge --no-ff main -m "Merge release v3.0.0 back into develop"
git push
```

The back-merge brings the version change from the release branch into `develop`, so both
long-lived branches agree. GitHub shows the tag under **Releases/Tags**.

### Hotfixes later

If a bug is found in `v3.0.0`:

```bash
git checkout -b hotfix/v3.0.1 main
git commit -am "Fix ..."
```

Open a PR from `hotfix/v3.0.1` into `main`, tag `v3.0.1`, then merge the hotfix into
`develop` too.

---

## Week 2 workflow

This document records how Git was used during the lab: which branches were created, what each
commit contains, where merges and cherry-picks happened, and how to repeat the workflow.

### 1. Starting point

The Week 2 repository starts from the Week 1 history, so the Week 1 commit is kept:

```bash
git clone https://github.com/123annick/Bank-Account-Management.git Bank-Account-Management-II
cd Bank-Account-Management-II
git remote remove origin
```

| Commit | Message |
|---|---|
| `2627b4a` | Bank Account Management System with README (Week 1) |

### 2. Branch strategy

| Branch | Created from | Purpose |
|---|---|---|
| `main` | — | Always compiles and runs. Only receives merges and documentation. |
| `feature/refactor` | `main` | Phase 1: Maven layout, clean-code refactoring of models and services |
| `feature/exceptions` | `feature/refactor` | Phase 2: custom exceptions, validation and the console UI |
| `feature/testing` | `main` | Phase 3: JUnit 5 tests and fixes found while testing |
| `feature/error-handling` | `main` | Extra validation fix; receives a cherry-picked fix from testing |

`feature/exceptions` was branched from `feature/refactor` because the exceptions are thrown by the
refactored classes. Merging `feature/exceptions` into `main` therefore delivered both phases at
once, and `main` never contained code that did not compile.

### 3. Phase 1 — Refactoring (`feature/refactor`)

```bash
git checkout -b feature/refactor
mkdir -p src/main/java
git mv src/model src/main/java/model
git mv src/service src/main/java/service
git mv src/app src/main/java/app
git add .
git commit -m "Move Week 1 code into Maven standard layout with JUnit 5 support"
git add .
git commit -m "Refactor models: injected IDs, enums and polymorphic fee rules"
git add .
git commit -m "Refactored AccountManager and TransactionManager"
```

| Commit | Message |
|---|---|
| `f6e4cc5` | Move Week 1 code into Maven standard layout with JUnit 5 support |
| `9446542` | Refactor models: injected IDs, enums and polymorphic fee rules |
| `30219ce` | Refactored AccountManager and TransactionManager |

`git mv` was used so Git records the files as moved, not deleted and re-added, and the Week 1
history of each file is kept.

### 4. Phase 2 — Exception handling (`feature/exceptions`)

```bash
git checkout -b feature/exceptions
git add src/main/java/models/exceptions src/main/java/utils/ValidationUtils.java
git commit -m "Add custom exception hierarchy and ValidationUtils"
git add .
git commit -m "Add console UI with try-catch error handling and confirmation prompts"

git checkout main
git merge --no-ff feature/exceptions
```

| Commit | Message |
|---|---|
| `604edcb` | Add custom exception hierarchy and ValidationUtils |
| `483562b` | Add console UI with try-catch error handling and confirmation prompts |
| `1ff646e` | Merge branch 'feature/exceptions' into main |

`--no-ff` (no fast-forward) always creates a merge commit, so the history shows clearly that a
feature branch was joined into `main`.

### 5. More refactoring on `feature/refactor`

While `feature/exceptions` was being merged, one more refactoring commit was made on
`feature/refactor`: the statement now uses an insertion sort instead of a backwards loop.

```bash
git checkout feature/refactor
git add src/main/java/utils/TransactionSorter.java src/main/java/services/StatementGenerator.java
git commit -m "Sort statement transactions newest first using insertion sort"
```

| Commit | Message |
|---|---|
| `c47c6b0` | Sort statement transactions newest first using insertion sort |

### 6. Phase 3 — Testing (`feature/testing`) and the first cherry-pick

```bash
git checkout main
git checkout -b feature/testing
git add src/test/java
git commit -m "Add JUnit 5 tests for deposit, withdraw and transfer"

git cherry-pick -x c47c6b0
git add src/test/java
git commit -m "Add tests for statement generation and transaction sorting"
```

**Why cherry-pick here?** The testing branch needed the sorting change to write
`TransactionSorterTest`, but merging the whole `feature/refactor` branch was not needed: its other
commits were already in `main`. `git cherry-pick` copies just that one commit. The `-x` option adds
`(cherry picked from commit c47c6b0...)` to the message so reviewers can trace where it came from.

While testing, a floating-point problem was found: `1000 + 0.1 + 0.2` gave `1000.3000000000001`.
It was fixed on the testing branch, followed by a test that proves the fix, and the Run Tests
menu option:

```bash
git commit -am "Fix floating-point drift by rounding balances to cents"
git commit -am "Add test for rounding balances to cents"
git add .
git commit -m "Add Run Tests menu option with JUnit-style console summary"
```

| Commit | Message |
|---|---|
| `ee37aaa` | Add JUnit 5 tests for deposit, withdraw and transfer |
| `ec92dbb` | Sort statement transactions newest first using insertion sort (cherry-picked from `c47c6b0`) |
| `0c532fb` | Add tests for statement generation and transaction sorting |
| `9da543f` | Fix floating-point drift by rounding balances to cents |
| `ab5cde5` | Add test for rounding balances to cents |
| `abc983c` | Add Run Tests menu option with JUnit-style console summary |

### 7. Error handling branch and cherry-picks in both directions

```bash
git checkout main
git checkout -b feature/error-handling
git commit -am "Reject NaN and infinite amounts in ValidationUtils"

git cherry-pick -x 9da543f

git checkout feature/testing
git cherry-pick -x a069248
git commit -am "Add test for NaN and infinite amounts"
```

| Commit | Branch | Message |
|---|---|---|
| `a069248` | `feature/error-handling` | Reject NaN and infinite amounts in ValidationUtils |
| `63216e4` | `feature/error-handling` | Fix floating-point drift by rounding balances to cents (cherry-picked from `9da543f`) |
| `50eefbf` | `feature/testing` | Reject NaN and infinite amounts in ValidationUtils (cherry-picked from `a069248`) |
| `d70c61f` | `feature/testing` | Add test for NaN and infinite amounts |

- The tested rounding fix (`9da543f`) was cherry-picked **into** `feature/error-handling`, so that
  branch also had correct balances without taking all the test work.
- The validation fix (`a069248`) was cherry-picked **into** `feature/testing`, so a test could be
  written for it.

### 8. Final merges into `main`

```bash
git checkout main
git merge --no-ff feature/testing
git merge --no-ff feature/error-handling
```

| Commit | Message |
|---|---|
| `80b62d6` | Merge branch 'feature/testing' into main |
| `128790a` | Merge branch 'feature/error-handling' into main |

Both branches contained the same two cherry-picked changes. Because the changes were identical,
Git merged them automatically with no conflict.

### 9. Commit graph

```
*   128790a Merge branch 'feature/error-handling' into main
|\
| * 63216e4 Fix floating-point drift by rounding balances to cents
| * a069248 Reject NaN and infinite amounts in ValidationUtils
* |   80b62d6 Merge branch 'feature/testing' into main
|\ \
| |/
|/|
| * d70c61f Add test for NaN and infinite amounts
| * 50eefbf Reject NaN and infinite amounts in ValidationUtils
| * abc983c Add Run Tests menu option with JUnit-style console summary
| * ab5cde5 Add test for rounding balances to cents
| * 9da543f Fix floating-point drift by rounding balances to cents
| * 0c532fb Add tests for statement generation and transaction sorting
| * ec92dbb Sort statement transactions newest first using insertion sort
| * ee37aaa Add JUnit 5 tests for deposit, withdraw and transfer
|/
*   1ff646e Merge branch 'feature/exceptions' into main
|\
| * 483562b Add console UI with try-catch error handling and confirmation prompts
| * 604edcb Add custom exception hierarchy and ValidationUtils
| | * c47c6b0 Sort statement transactions newest first using insertion sort
| |/
| * 30219ce Refactored AccountManager and TransactionManager
| * 9446542 Refactor models: injected IDs, enums and polymorphic fee rules
| * f6e4cc5 Move Week 1 code into Maven standard layout with JUnit 5 support
|/
* 2627b4a Bank Account Management System with README
```

See it yourself with `git log --graph --oneline --all`, or in IntelliJ with
**View > Tool Windows > Git > Log**.

### 10. Resolving a cherry-pick or merge conflict

If Git stops with `CONFLICT`:

1. Run `git status` to see which files are in conflict.
2. Open each file in IntelliJ. It shows **Accept Yours / Accept Theirs / Merge**. Keep the correct
   code and delete the `<<<<<<<`, `=======` and `>>>>>>>` markers.
3. Run the tests to make sure everything still works.
4. Mark the file as fixed and continue:

```bash
git add <file>
git cherry-pick --continue
```

To give up instead: `git cherry-pick --abort` (or `git merge --abort` for a merge).

### 11. Pushing to GitHub

```bash
git remote add origin https://github.com/123annick/Bank-Account-Management-II.git
git push -u origin --all
git log --oneline origin/main
```

`--all` pushes every branch, so `feature/refactor`, `feature/exceptions`, `feature/testing` and
`feature/error-handling` are visible on GitHub next to `main`.

### 12. Commit message rules followed

- Start with a verb in the imperative mood: *Add*, *Fix*, *Refactor*, *Move*, *Sort*.
- Keep the subject line short and specific: say **what** changed, not *"update files"*.
- One logical change per commit, so any single commit can be cherry-picked on its own.
- Use `-x` with cherry-pick so the original commit is always recorded.
