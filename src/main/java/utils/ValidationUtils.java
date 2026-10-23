package utils;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import models.exceptions.InvalidAmountException;
import models.exceptions.InvalidInputException;

public final class ValidationUtils {
  public static final int MIN_AGE = 18;
  public static final int MAX_AGE = 120;
  public static final double MAX_TRANSACTION_AMOUNT = 1_000_000;

  public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^ACC\\d{3}$");
  public static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^CUS\\d{3}$");
  public static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^TXN\\d{3,}$");
  public static final Pattern EMAIL_PATTERN = Pattern.compile("^(?=.{6,100}$)(?!.*\\.\\.)"
      + "[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
  public static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?(?:[0-9][ -]?){6,14}[0-9]$");
  public static final Pattern NAME_PATTERN =
      Pattern.compile("^(?=.{2,50}$)[A-Za-z]+(?:[ '-][A-Za-z]+)*$");
  public static final Pattern ADDRESS_PATTERN =
      Pattern.compile("^(?=.{3,80}$)(?=.*[A-Za-z])[A-Za-z0-9][A-Za-z0-9 ,.'#/-]*$");
  public static final Pattern AMOUNT_PATTERN =
      Pattern.compile("^-?\\$?(?:\\d{1,3}(?:,\\d{3})+|\\d+)(?:\\.\\d{1,2})?$");
  public static final Pattern STATUS_PATTERN = Pattern.compile("^(Active|Inactive)$");

  public static final Predicate<String> IS_ACCOUNT_NUMBER = matches(ACCOUNT_NUMBER_PATTERN);
  public static final Predicate<String> IS_CUSTOMER_ID = matches(CUSTOMER_ID_PATTERN);
  public static final Predicate<String> IS_TRANSACTION_ID = matches(TRANSACTION_ID_PATTERN);
  public static final Predicate<String> IS_EMAIL = matches(EMAIL_PATTERN);
  public static final Predicate<String> IS_PHONE = matches(PHONE_PATTERN);
  public static final Predicate<String> IS_NAME = matches(NAME_PATTERN);
  public static final Predicate<String> IS_ADDRESS = matches(ADDRESS_PATTERN);
  public static final Predicate<String> IS_AMOUNT_TEXT = matches(AMOUNT_PATTERN);
  public static final Predicate<String> IS_STATUS = matches(STATUS_PATTERN);

  public static final String ACCOUNT_NUMBER_RULE =
      "Invalid account number format. Use ACC followed by 3 digits (e.g., ACC001).";
  public static final String EMAIL_RULE =
      "Invalid email format. Please enter a valid address (e.g., name@example.com)";
  public static final String PHONE_RULE =
      "Invalid phone number. Use 7 to 15 digits, optionally starting with + (e.g., +1-555-0001).";
  public static final String NAME_RULE = "Invalid name. Use 2 to 50 letters only, with single "
      + "spaces, hyphens or apostrophes between words (e.g., Mary-Jane O'Neil). "
      + "Numbers and symbols are not allowed.";
  public static final String ADDRESS_RULE = "Invalid address. Use 3 to 80 characters with at "
      + "least one letter; allowed symbols are , . ' # / - (e.g., 12 Main St).";
  public static final String AMOUNT_FORMAT_RULE =
      "Invalid amount format. Use digits with up to 2 decimals (e.g., 1500 or 1,500.50).";
  public static final String AGE_RULE = "Age must be between " + MIN_AGE + " and " + MAX_AGE + ".";

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
   * Tells whether an age is inside the allowed range.
   *
   * @param age the age to check
   * @return true when the age is allowed
   */
  public static boolean isValidAge(int age) {
    return age >= MIN_AGE && age <= MAX_AGE;
  }

  /**
   * Checks that an age is inside the allowed range.
   *
   * @param age the age to check
   * @throws InvalidInputException when the age is too low or too high
   */
  public static void validateAge(int age) throws InvalidInputException {
    if (!isValidAge(age)) {
      throw new InvalidInputException(AGE_RULE);
    }
  }

  /**
   * Checks that an amount is a real number above zero, not above the transaction limit, and
   * has at most 2 decimal places.
   *
   * @param amount the amount to check
   * @throws InvalidAmountException when any of these rules is broken
   */
  public static void validateAmount(double amount) throws InvalidAmountException {
    if (!Double.isFinite(amount) || amount <= 0) {
      throw new InvalidAmountException("Invalid amount. Amount must be greater than 0.");
    }
    if (amount > MAX_TRANSACTION_AMOUNT) {
      throw new InvalidAmountException("Amount cannot exceed "
          + FormatUtils.currency(MAX_TRANSACTION_AMOUNT) + " in one transaction.");
    }
    if (hasMoreThanTwoDecimals(amount)) {
      throw new InvalidAmountException("Amount can have at most 2 decimal places.");
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

  private static boolean hasMoreThanTwoDecimals(double amount) {
    double cents = amount * 100;
    return Math.abs(cents - Math.round(cents)) > 1e-6;
  }
}
