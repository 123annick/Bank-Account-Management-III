package models.exceptions;

public class PersistenceException extends BankException {

  /**
   * Creates the exception with a message that can be shown to the user.
   *
   * @param message the user-friendly error message
   */
  public PersistenceException(String message) {
    super(message);
  }
}
