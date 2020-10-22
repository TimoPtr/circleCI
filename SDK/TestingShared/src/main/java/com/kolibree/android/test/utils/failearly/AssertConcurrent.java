/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.failearly;

import static junit.framework.TestCase.assertTrue;

import androidx.annotation.Keep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Copied from https://github.com/junit-team/junit4/wiki/multithreaded-code-and-concurrency */
@Keep
public class AssertConcurrent {
  public static void assertConcurrent(
      final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds)
      throws InterruptedException {
    final int numThreads = runnables.size();
    final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());
    final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
    try {
      final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
      final CountDownLatch afterInitBlocker = new CountDownLatch(1);
      final CountDownLatch allDone = new CountDownLatch(numThreads);
      for (final Runnable submittedTestRunnable : runnables) {
        threadPool.submit(
            () -> {
              allExecutorThreadsReady.countDown();
              try {
                afterInitBlocker.await();
                submittedTestRunnable.run();
              } catch (final Throwable e) {
                exceptions.add(e);
              } finally {
                allDone.countDown();
              }
            });
      }
      // wait until all threads are ready
      assertTrue(
          "Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent",
          allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
      // start all test runners
      afterInitBlocker.countDown();
      assertTrue(
          message + " timeout! More than" + maxTimeoutSeconds + "seconds",
          allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
    } finally {
      threadPool.shutdownNow();
    }
    if (!exceptions.isEmpty()) {
      exceptions.get(0).printStackTrace();
    }
    assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
  }
}
