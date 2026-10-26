package services;

public record SimulationReport(String accountNumber, double startingBalance,
    double finalBalance, double recordedChange, int recordedTransactions) {

  /**
   * Tells whether the final balance equals the starting balance plus every recorded change,
   * which proves no update was lost.
   *
   * @return true when the balance is consistent
   */
  public boolean isConsistent() {
    return Math.abs(startingBalance + recordedChange - finalBalance) < 0.005;
  }
}
