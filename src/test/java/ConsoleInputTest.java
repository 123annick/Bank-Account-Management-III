import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.BankConsoleApp;
import ui.ConsoleInput;
import ui.ConsoleOutput;
import utils.ValidationUtils;

@ExtendWith(ConsoleTestLogger.class)
class ConsoleInputTest {
  private final ByteArrayOutputStream printed = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @BeforeEach
  void captureConsole() {
    originalOut = System.out;
    System.setOut(new PrintStream(printed, true, StandardCharsets.UTF_8));
  }

  @AfterEach
  void restoreConsole() {
    System.setOut(originalOut);
  }

  @Test
  void nameWithNumbersIsAskedAgain() {
    String name = inputFrom("12345\nJohn3\nAnn Uwase\n").readValidated("Name: ",
        ValidationUtils.IS_NAME, ValidationUtils.NAME_RULE);
    assertEquals("Ann Uwase", name);
    assertEquals(2, countErrors());
  }

  @Test
  void amountRejectsBadFormatsUntilValid() {
    double amount = inputFrom("abc\n1e3\n10.555\n$1,500.50\n").readAmount("Amount: ");
    assertEquals(1500.50, amount);
    assertEquals(3, countErrors());
  }

  @Test
  void negativeAmountPassesFormatSoTheRuleCanExplainIt() {
    assertEquals(-200, inputFrom("-200\n").readAmount("Amount: "));
  }

  @Test
  void wholeNumberOutsideRangeIsAskedAgain() {
    int age = inputFrom("abc\n1.5\n15\n200\n30\n").readInt("Age: ", 18, 120);
    assertEquals(30, age);
    assertEquals(4, countErrors());
  }

  @Test
  void blankTextIsAskedAgain() {
    assertEquals("ACC001", inputFrom("\n   \nACC001\n").readText("Account: "));
    assertEquals(2, countErrors());
  }

  @Test
  void confirmAcceptsYesAndNoWords() {
    assertTrue(inputFrom("yes\n").confirm("Confirm? "));
    assertFalse(inputFrom("maybe\nNO\n").confirm("Confirm? "));
  }

  @Test
  void closedInputExitsTheAppSafely() {
    ConsoleOutput output = new ConsoleOutput();
    ConsoleInput input = new ConsoleInput(new Scanner(""), output);
    new BankConsoleApp(input, output, List.of(), () -> true).run();
    assertTrue(printed.toString(StandardCharsets.UTF_8).contains("Input was closed"));
  }

  private ConsoleInput inputFrom(String typed) {
    return new ConsoleInput(new Scanner(typed), new ConsoleOutput());
  }

  private int countErrors() {
    return printed.toString(StandardCharsets.UTF_8).split("Error:", -1).length - 1;
  }
}
