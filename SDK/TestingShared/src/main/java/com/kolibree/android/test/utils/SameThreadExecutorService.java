package com.kolibree.android.test.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** See https://stackoverflow.com/a/14639686/218473 */
public final class SameThreadExecutorService extends ThreadPoolExecutor {
  private final CountDownLatch signal = new CountDownLatch(1);

  private SameThreadExecutorService() {
    super(
        1,
        1,
        0,
        TimeUnit.DAYS,
        new SynchronousQueue<>(),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  @Override
  public void shutdown() {
    super.shutdown();
    signal.countDown();
  }

  public static ExecutorService getInstance() {
    return SingletonHolder.instance;
  }

  private static class SingletonHolder {
    static ExecutorService instance = createInstance();
  }

  private static ExecutorService createInstance() {
    final SameThreadExecutorService instance = new SameThreadExecutorService();

    // The executor has one worker thread. Give it a Runnable that waits
    // until the executor service is shut down.
    // All other submitted tasks will use the RejectedExecutionHandler
    // which runs tasks using the  caller's thread.
    instance.submit(
        () -> {
          boolean interrupted = false;
          try {
            while (true) {
              try {
                instance.signal.await();
                break;
              } catch (InterruptedException e) {
                interrupted = true;
              }
            }
          } finally {
            if (interrupted) {
              Thread.currentThread().interrupt();
            }
          }
        });
    return Executors.unconfigurableExecutorService(instance);
  }
}
