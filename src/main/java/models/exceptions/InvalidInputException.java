package models.exceptions;

public class InvalidInputException extends BankException {

  /**
   * Creates the exception with a message that can be shown to the user.
   *
   * @param message the user-friendly error message
   */
  public InvalidInputException(String message) {
    super(message);
  }
}
