package utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import models.exceptions.InvalidAmountException;
import models.exceptions.InvalidInputException;

public final class ValidationUtils {
  public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^ACC\\d{3}$");
  public static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
  public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9][0-9 -]{5,13}[0-9]$");
  public static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,49}$");
  public static final Pattern ADDRESS_PATTERN =
      Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 ,.'#/-]{2,79}$");

  public static final Predicate<String> IS_ACCOUNT_NUMBER = matches(ACCOUNT_NUMBER_PATTERN);
  public static final Predicate<String> IS_EMAIL = matches(EMAIL_PATTERN);
  public static final Predicate<String> IS_PHONE = matches(PHONE_PATTERN);
  public static final Predicate<String> IS_NAME = matches(NAME_PATTERN);
  public static final Predicate<String> IS_ADDRESS = matches(ADDRESS_PATTERN);

  public static final String ACCOUNT_NUMBER_RULE =
      "Invalid account number format. Use ACC followed by 3 digits (e.g., ACC001).";
  public static final String EMAIL_RULE =
      "Invalid email format. Please enter a valid address (e.g., name@example.com)";
  public static final String PHONE_RULE =
      "Invalid phone number. Use 7 to 15 digits, optionally starting with + (e.g., +1-555-0001).";
  public static final String NAME_RULE =
      "Invalid name. Use 2 to 50 letters, spaces, dots, apostrophes or hyphens.";
  public static final String ADDRESS_RULE =
      "Invalid address. Use 3 to 80 letters, digits, spaces and , . ' # / -";

  private ValidationUtils() {
  }

  /**
   * Builds a rule that is true when the whole text matches the pattern.
   *
   * @param pattern the regular expression to use
   * @return a predicate that tests text against the pattern
   */
  public static Predicate<String> matches(Pattern pattern) {
    return text -> text != null && pattern.matcher(text).matches();
  }

  /**
   * Checks text against a rule and throws when it does not pass.
   *
   * @param text the text to check
   * @param rule the rule the text must pass
   * @param message the error shown when it does not pass
   * @throws InvalidInputException when the text breaks the rule
   */
  public static void requireValid(String text, Predicate<String> rule, String message)
      throws InvalidInputException {
    if (!rule.test(text)) {
      throw new InvalidInputException(message);
    }
  }

  /**
   * Checks that an amount is a real number greater than zero.
   *
   * @param amount the amount to check
   * @throws InvalidAmountException when the amount is zero, negative or not a real number
   */
  public static void validateAmount(double amount) throws InvalidAmountException {
    if (!Double.isFinite(amount) || amount <= 0) {
      throw new InvalidAmountException("Invalid amount. Amount must be greater than 0.");
    }
  }

  /**
   * Checks that an opening deposit reaches the required minimum.
   *
   * @param amount the deposit to check
   * @param minimum the smallest allowed deposit
   * @param subject who the rule applies to, for example "Savings accounts"
   * @throws InvalidAmountException when the deposit is below the minimum
   */
  public static void validateInitialDeposit(double amount, double minimum, String subject)
      throws InvalidAmountException {
    if (amount < minimum) {
      throw new InvalidAmountException(subject + " need an initial deposit of at least "
          + FormatUtils.currency(minimum) + ".");
    }
  }
}
