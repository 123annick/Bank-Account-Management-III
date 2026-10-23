import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import models.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.ValidationUtils;

@ExtendWith(ConsoleTestLogger.class)
class RegexValidationTest {

  @Test
  void validAccountNumbersMatchPattern() {
    List.of("ACC001", "ACC123", "ACC999")
        .forEach(number -> assertTrue(ValidationUtils.IS_ACCOUNT_NUMBER.test(number)));
  }

  @Test
  void invalidAccountNumbersAreRejected() {
    List.of("ACC99", "ACC1234", "acc001x", "AC001", "ACCABC", "", " ACC001")
        .forEach(number -> assertFalse(ValidationUtils.IS_ACCOUNT_NUMBER.test(number)));
  }

  @Test
  void validEmailsAreAccepted() {
    List.of("john.smith@bank.com", "a+b@mail.co.uk", "user_1@example.org")
        .forEach(email -> assertTrue(ValidationUtils.IS_EMAIL.test(email)));
  }

  @Test
  void invalidEmailsAreRejected() {
    List.of("john.smith@bank", "no-at-sign.com", "@bank.com", "a b@bank.com", "ann@.com")
        .forEach(email -> assertFalse(ValidationUtils.IS_EMAIL.test(email)));
  }

  @Test
  void phoneNumbersFollowPattern() {
    assertTrue(ValidationUtils.IS_PHONE.test("+1-555-0001"));
    assertTrue(ValidationUtils.IS_PHONE.test("+250788123456"));
    assertFalse(ValidationUtils.IS_PHONE.test("0788"));
    assertFalse(ValidationUtils.IS_PHONE.test("phone123"));
  }

  @Test
  void namesFollowPattern() {
    assertTrue(ValidationUtils.IS_NAME.test("Mary-Jane O'Neil"));
    assertFalse(ValidationUtils.IS_NAME.test("J"));
    assertFalse(ValidationUtils.IS_NAME.test("John|Smith"));
  }

  @Test
  void nullTextNeverMatches() {
    assertFalse(ValidationUtils.IS_EMAIL.test(null));
  }

  @Test
  void requireValidThrowsWithFriendlyMessage() {
    InvalidInputException exception = assertThrows(InvalidInputException.class,
        () -> ValidationUtils.requireValid("bad", ValidationUtils.IS_EMAIL,
            ValidationUtils.EMAIL_RULE));
    assertEquals(ValidationUtils.EMAIL_RULE, exception.getMessage());
  }

  @Test
  void namesWithNumbersOrSymbolsAreRejected() {
    List.of("12345", "John3", "Ann  Lee", "-Ann", "Ann-", "A", "Ann_Lee", "Ann@Lee")
        .forEach(name -> assertFalse(ValidationUtils.IS_NAME.test(name)));
  }

  @Test
  void realNamesAreAccepted() {
    List.of("Ann", "Ann Uwase", "Mary-Jane", "O'Neil", "Jean Paul Habimana")
        .forEach(name -> assertTrue(ValidationUtils.IS_NAME.test(name)));
  }

  @Test
  void phoneMustHaveSevenToFifteenDigits() {
    assertTrue(ValidationUtils.IS_PHONE.test("+250 788 123 456"));
    assertTrue(ValidationUtils.IS_PHONE.test("5550001"));
    assertFalse(ValidationUtils.IS_PHONE.test("555000"));
    assertFalse(ValidationUtils.IS_PHONE.test("1234567890123456"));
    assertFalse(ValidationUtils.IS_PHONE.test("+1--555-0001"));
  }

  @Test
  void addressMustContainALetter() {
    assertTrue(ValidationUtils.IS_ADDRESS.test("12 Main St"));
    assertFalse(ValidationUtils.IS_ADDRESS.test("12345"));
    assertFalse(ValidationUtils.IS_ADDRESS.test("1 Main St|ACC999"));
  }

  @Test
  void emailWithDoubleDotsIsRejected() {
    assertFalse(ValidationUtils.IS_EMAIL.test("ann@bank..com"));
    assertFalse(ValidationUtils.IS_EMAIL.test("ann..lee@bank.com"));
  }

  @Test
  void amountTextAllowsOnlyMoneyFormats() {
    List.of("1500", "1,500", "1,500.50", "$20.5", "0.99", "-200")
        .forEach(text -> assertTrue(ValidationUtils.IS_AMOUNT_TEXT.test(text)));
    List.of("1e3", "10.555", "abc", "1,50", "", "NaN", "Infinity", "12.")
        .forEach(text -> assertFalse(ValidationUtils.IS_AMOUNT_TEXT.test(text)));
  }

  @Test
  void customerAndTransactionIdsFollowPatterns() {
    assertTrue(ValidationUtils.IS_CUSTOMER_ID.test("CUS001"));
    assertFalse(ValidationUtils.IS_CUSTOMER_ID.test("CUS1"));
    assertTrue(ValidationUtils.IS_TRANSACTION_ID.test("TXN1000"));
    assertFalse(ValidationUtils.IS_TRANSACTION_ID.test("TX001"));
  }
}
