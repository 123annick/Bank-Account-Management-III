package utils;

public class IdGenerator {
  private final String prefix;
  private int counter;

  /**
   * Creates a generator that produces ids such as ACC001, ACC002.
   *
   * @param prefix the text placed before the number
   */
  public IdGenerator(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Returns the next id and moves the counter forward.
   *
   * @return the next id
   */
  public synchronized String next() {
    counter++;
    return format(counter);
  }

  /**
   * Returns the id that {@link #next()} will give, without moving the counter.
   *
   * @return the upcoming id
   */
  public synchronized String peek() {
    return format(counter + 1);
  }

  /**
   * Moves the counter past an id that already exists, for example one loaded from a file,
   * so new ids never repeat it.
   *
   * @param existingId an id such as ACC007
   */
  public synchronized void skipPast(String existingId) {
    counter = Math.max(counter, numberOf(existingId));
  }

  /**
   * Reads the number at the end of an id, for example 7 from ACC007.
   *
   * @param id the id to read
   * @return the number, or 0 when the id has no digits
   */
  public static int numberOf(String id) {
    String digits = id.replaceAll("\\D", "");
    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
  }

  private String format(int number) {
    return String.format("%s%03d", prefix, number);
  }
}
