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
}
