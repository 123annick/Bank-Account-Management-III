package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public final class ConcurrencyUtils {

  private ConcurrencyUtils() {
  }

  /**
   * Starts every task on its own thread named Thread-1, Thread-2 and so on, then waits
   * until all of them have finished.
   *
   * @param tasks the work each thread does
   * @throws InterruptedException when the waiting thread is interrupted
   */
  public static void runInParallelThreads(List<Runnable> tasks) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    IntStream.range(0, tasks.size())
        .forEach(i -> threads.add(new Thread(tasks.get(i), "Thread-" + (i + 1))));
    threads.forEach(Thread::start);
    for (Thread thread : threads) {
      thread.join();
    }
  }

  /**
   * Returns the name of the thread that is running the calling code.
   *
   * @return the current thread name
   */
  public static String currentThreadName() {
    return Thread.currentThread().getName();
  }
}
