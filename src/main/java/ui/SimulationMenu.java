package ui;

import models.Account;
import services.SimulationReport;
import services.TransactionSimulator;
import utils.FormatUtils;

public class SimulationMenu implements MenuAction {
  private final ConsoleOutput output;
  private final AccountSelector accountSelector;
  private final TransactionSimulator simulator;

  /**
   * Creates the Run Concurrent Simulation menu.
   *
   * @param output prints messages
   * @param accountSelector asks the user to pick an account
   * @param simulator runs the threads and the parallel stream
   */
  public SimulationMenu(ConsoleOutput output, AccountSelector accountSelector,
      TransactionSimulator simulator) {
    this.output = output;
    this.accountSelector = accountSelector;
    this.simulator = simulator;
  }

  /**
   * Returns the main menu title of this option.
   *
   * @return the title
   */
  @Override
  public String getTitle() {
    return "Run Concurrent Simulation";
  }

  /**
   * Asks for an account, runs simultaneous deposits and withdrawals on it, and checks the
   * final balance.
   */
  @Override
  public void execute() {
    output.printTitle("CONCURRENT TRANSACTION SIMULATION");
    Account account = accountSelector.selectAccount("Enter Account Number");
    if (account == null) {
      return;
    }
    output.println("\nRunning concurrent transaction simulation...\n");
    try {
      printReport(simulator.run(account));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      output.printError("The simulation was interrupted.");
    }
  }

  private void printReport(SimulationReport report) {
    output.println("");
    output.println("Starting Balance: " + FormatUtils.currency(report.startingBalance()));
    output.println("Recorded transactions: " + report.recordedTransactions()
        + " (net " + FormatUtils.signedCurrency(report.recordedChange()) + ")");
    if (report.isConsistent()) {
      output.printSuccess("Thread-safe operations completed successfully.");
    } else {
      output.printError("Balance does not match the recorded transactions.");
    }
    output.println("Final Balance for " + report.accountNumber() + ": "
        + FormatUtils.currency(report.finalBalance()));
  }
}
