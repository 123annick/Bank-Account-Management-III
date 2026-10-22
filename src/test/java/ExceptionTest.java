import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import models.AccountType;
import models.CustomerDetails;
import models.CustomerType;
import models.exceptions.BankException;
import models.exceptions.InsufficientFundsException;
import models.exceptions.InvalidAccountException;
import models.exceptions.InvalidAmountException;
import models.exceptions.OverdraftExceededException;
import models.exceptions.InvalidInputException;
import models.exceptions.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import services.AccountFactory;
import services.AccountManager;
import utils.ValidationUtils;

@ExtendWith(ConsoleTestLogger.class)
class ExceptionTest {
  private AccountManager accountManager;
  private AccountFactory accountFactory;

  @BeforeEach
  void setUp() {
    accountManager = new AccountManager();
    accountFactory = new AccountFactory();
  }

  @Test
  void customExceptionsShareBankExceptionParent() {
    assertTrue(BankException.class.isAssignableFrom(InvalidAmountException.class));
    assertTrue(BankException.class.isAssignableFrom(InsufficientFundsException.class));
    assertTrue(BankException.class.isAssignableFrom(OverdraftExceededException.class));
    assertTrue(BankException.class.isAssignableFrom(InvalidAccountException.class));
    assertTrue(BankException.class.isAssignableFrom(InvalidInputException.class));
    assertTrue(BankException.class.isAssignableFrom(PersistenceException.class));
  }

  @Test
  void customExceptionsAreCheckedExceptions() {
    assertFalse(RuntimeException.class.isAssignableFrom(BankException.class));
  }

  @Test
  void invalidAmountMessageIsUserFriendly() {
    InvalidAmountException exception = assertThrows(InvalidAmountException.class,
        () -> ValidationUtils.validateAmount(-200));
    assertEquals("Invalid amount. Amount must be greater than 0.", exception.getMessage());
  }

  @Test
  void notANumberAmountThrowsInvalidAmountException() {
    assertThrows(InvalidAmountException.class, () -> ValidationUtils.validateAmount(Double.NaN));
    assertThrows(InvalidAmountException.class,
        () -> ValidationUtils.validateAmount(Double.POSITIVE_INFINITY));
  }

  @Test
  void unknownAccountNumberThrowsInvalidAccountException() {
    InvalidAccountException exception = assertThrows(InvalidAccountException.class,
        () -> accountManager.findAccount("ACC999"));
    assertTrue(exception.getMessage().startsWith("Account not found"));
  }

  @Test
  void insufficientFundsMessageShowsCurrentBalance() {
    InsufficientFundsException exception = assertThrows(InsufficientFundsException.class,
        () -> TestData.savingsAccount(2950).withdraw(10000));
    assertTrue(exception.getMessage().contains("Current balance: $2,950.00"));
  }

  @Test
  void overdraftMessageShowsLimit() {
    OverdraftExceededException exception = assertThrows(OverdraftExceededException.class,
        () -> TestData.checkingAccount(100).withdraw(5000));
    assertTrue(exception.getMessage().contains("Overdraft limit: $1,000.00"));
  }

  @Test
  void premiumCustomerBelowMinimumDepositThrowsException() {
    assertThrows(InvalidAmountException.class, () -> accountFactory.createAccount(
        TestData.customerDetails("Ann"), CustomerType.PREMIUM, AccountType.CHECKING, 9999));
  }

  @Test
  void savingsAccountBelowMinimumDepositThrowsException() {
    assertThrows(InvalidAmountException.class, () -> accountFactory.createAccount(
        TestData.customerDetails("Ann"), CustomerType.REGULAR, AccountType.SAVINGS, 499));
  }

  @Test
  void duplicateAccountNumberThrowsInvalidAccountException() throws Exception {
    accountManager.addAccount(TestData.checkingAccount(100));
    assertThrows(InvalidAccountException.class,
        () -> accountManager.addAccount(TestData.checkingAccount(100)));
  }

  @Test
  void invalidEmailWhenCreatingAccountThrowsInvalidInputException() {
    CustomerDetails details = new CustomerDetails("Ann Uwase", 25, "ann@bank", "+1-555-0000",
        "1 Test Street");
    InvalidInputException exception = assertThrows(InvalidInputException.class,
        () -> accountFactory.createAccount(details, CustomerType.REGULAR, AccountType.CHECKING,
            100));
    assertEquals(ValidationUtils.EMAIL_RULE, exception.getMessage());
  }

  @Test
  void javaExceptionTypesMatchHowTheCodeHandlesThem() {
    assertTrue(RuntimeException.class.isAssignableFrom(InputMismatchException.class));
    assertTrue(RuntimeException.class.isAssignableFrom(NumberFormatException.class));
    assertTrue(RuntimeException.class.isAssignableFrom(NoSuchElementException.class));
    assertFalse(RuntimeException.class.isAssignableFrom(IOException.class));
    assertFalse(RuntimeException.class.isAssignableFrom(InterruptedException.class));
    assertFalse(Exception.class.isAssignableFrom(Error.class));
  }
}
