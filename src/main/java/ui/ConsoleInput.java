package ui;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.Predicate;

public class ConsoleInput {
  private final Scanner scanner;
  private final ConsoleOutput output;

  /**
   * Creates a reader for keyboard input.
   *
   * @param scanner the scanner that reads the keyboard
   * @param output used to show retry messages
   */
  public ConsoleInput(Scanner scanner, ConsoleOutput output) {
    this.scanner = scanner;
    this.output = output;
  }

  /**
   * Asks until the user types some text.
   *
   * @param prompt the question to show
   * @return the trimmed text
   */
  public String readText(String prompt) {
    while (true) {
      System.out.print(prompt);
      String text = scanner.nextLine().trim();
      if (!text.isEmpty()) {
        return text;
      }
      output.printError("This field cannot be empty.");
    }
  }

  /**
   * Asks until the typed text passes a rule, such as a regex check, showing the error
   * message after each wrong answer.
   *
   * @param prompt the question to show
   * @param rule the rule the answer must pass
   * @param errorMessage what to show when the answer breaks the rule
   * @return the accepted text
   */
  public String readValidated(String prompt, Predicate<String> rule, String errorMessage) {
    String text = readText(prompt);
    while (!rule.test(text)) {
      output.printError(errorMessage);
      text = readText(prompt);
    }
    return text;
  }

  /**
   * Asks until the user types a whole number inside the range.
   *
   * @param prompt the question to show
   * @param min the smallest allowed number
   * @param max the largest allowed number
   * @return the number typed
   */
  public int readInt(String prompt, int min, int max) {
    while (true) {
      try {
        int value = parseInt(readText(prompt));
        if (value >= min && value <= max) {
          return value;
        }
        output.printError("Please enter a number from " + min + " to " + max + ".");
      } catch (InputMismatchException e) {
        output.printError(e.getMessage());
      }
    }
  }

  /**
   * Asks until the user types a number. Dollar signs and commas are ignored.
   *
   * @param prompt the question to show
   * @return the number typed, which may still be zero or negative
   */
  public double readDouble(String prompt) {
    while (true) {
      try {
        return parseDouble(readText(prompt));
      } catch (InputMismatchException e) {
        output.printError(e.getMessage());
      }
    }
  }

  /**
   * Asks a yes or no question until the user types Y or N.
   *
   * @param prompt the question to show
   * @return true for yes, false for no
   */
  public boolean confirm(String prompt) {
    while (true) {
      String answer = readText(prompt);
      if (answer.equalsIgnoreCase("Y")) {
        return true;
      }
      if (answer.equalsIgnoreCase("N")) {
        return false;
      }
      output.printError("Please type Y or N.");
    }
  }

  /**
   * Waits until the user presses Enter.
   */
  public void pause() {
    System.out.print("\nPress Enter to continue...");
    scanner.nextLine();
  }

  private int parseInt(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new InputMismatchException("Invalid input. Please enter a whole number.");
    }
  }

  private double parseDouble(String text) {
    try {
      return Double.parseDouble(text.replace("$", "").replace(",", ""));
    } catch (NumberFormatException e) {
      throw new InputMismatchException("Invalid input. Please enter a number.");
    }
  }
}
