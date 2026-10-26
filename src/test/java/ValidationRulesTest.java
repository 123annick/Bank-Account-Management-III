import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import models.AccountType;
import models.CustomerDetails;
import models.CustomerType;
import models.exceptions.InvalidAccountException;
import models.exceptions.InvalidAmountException;
import models.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.AccountFactory;
import utils.ValidationUtils;

@ExtendWith(ConsoleTestLogger.class)
class ValidationRulesTest {
  private AccountFactory factory;

  @BeforeEach
  void setUp() {
    factory = new AccountFactory();
  }

  @Test
  void amountAboveLimitIsRejected() {
    InvalidAmountException exception = assertThrows(InvalidAmountException.class,
        () -> ValidationUtils.validateAmount(1_000_000.01));
    assertEquals("Amount cannot exceed $1,000,000.00 in one transaction.",
        exception.getMessage());
  }

  @Test
  void amountAtLimitIsAccepted() {
    assertDoesNotThrow(() -> ValidationUtils.validateAmount(1_000_000));
  }

  @Test
  void amountWithThreeDecimalsIsRejected() {
    InvalidAmountException exception = assertThrows(InvalidAmountException.class,
        () -> ValidationUtils.validateAmount(10.555));
    assertEquals("Amount can have at most 2 decimal places.", exception.getMessage());
  }

  @Test
  void amountsWithCentsAreAccepted() {
    assertDoesNotThrow(() -> ValidationUtils.validateAmount(0.01));
    assertDoesNotThrow(() -> ValidationUtils.validateAmount(1500.5));
    assertDoesNotThrow(() -> ValidationUtils.validateAmount(19.99));
  }

  @Test
  void agesOutsideRangeAreRejected() {
    assertThrows(InvalidInputException.class, () -> ValidationUtils.validateAge(17));
    assertThrows(InvalidInputException.class, () -> ValidationUtils.validateAge(121));
    assertDoesNotThrow(() -> ValidationUtils.validateAge(18));
  }

  @Test
  void factoryRejectsNameWithNumbers() {
    CustomerDetails details = new CustomerDetails("John3", 30, "john@bank.com", "+1-555-0000",
        "1 Main St");
    InvalidInputException exception = assertThrows(InvalidInputException.class,
        () -> factory.createAccount(details, CustomerType.REGULAR, AccountType.CHECKING, 100));
    assertEquals(ValidationUtils.NAME_RULE, exception.getMessage());
  }

  @Test
  void factoryRejectsUnderageCustomer() {
    CustomerDetails details = new CustomerDetails("Ann Uwase", 15, "ann@bank.com",
        "+1-555-0000", "1 Main St");
    assertThrows(InvalidInputException.class,
        () -> factory.createAccount(details, CustomerType.REGULAR, AccountType.CHECKING, 100));
  }

  @Test
  void factoryStopsAfterAccountNineHundredNinetyNine() {
    factory.restoreAccount("ACC999", "CUS999", TestData.customerDetails("Ann Uwase"),
        CustomerType.REGULAR, AccountType.CHECKING, 100);
    assertThrows(InvalidAccountException.class, () -> factory.createAccount(
        TestData.customerDetails("Ben Okoro"), CustomerType.REGULAR, AccountType.CHECKING, 100));
  }
}
