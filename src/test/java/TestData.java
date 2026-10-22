import java.time.LocalDateTime;
import models.CheckingAccount;
import models.Customer;
import models.CustomerDetails;
import models.PremiumCustomer;
import models.RegularCustomer;
import models.SavingsAccount;
import models.Transaction;
import models.TransactionType;

final class TestData {

  private TestData() {
  }

  static CustomerDetails customerDetails(String name) {
    return new CustomerDetails(name, 30, "test.user@bank.com", "+1-555-0000", "1 Test Street");
  }

  static Customer regularCustomer() {
    return new RegularCustomer("CUS001", customerDetails("John Smith"));
  }

  static Customer premiumCustomer() {
    return new PremiumCustomer("CUS002", customerDetails("Mary Jones"));
  }

  static SavingsAccount savingsAccount(double balance) {
    return new SavingsAccount("ACC001", regularCustomer(), balance);
  }

  static CheckingAccount checkingAccount(double balance) {
    return new CheckingAccount("ACC002", regularCustomer(), balance);
  }

  static Transaction depositAt(String transactionId, LocalDateTime timestamp) {
    return new Transaction(transactionId, "ACC001", TransactionType.DEPOSIT, 100, 1100,
        timestamp);
  }
}
