/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.test;

import android.annotation.SuppressLint;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.test.extensions.TrustedClockExtensionsKt;
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import timber.log.Timber;

/** Created by miguelaragues on 20/2/18. */
public abstract class CommonBaseTest {

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

  private static final Timber.Tree TEST_TREE =
      new Timber.DebugTree() {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
          if (t != null) {
            System.err.println(message);
            System.err.println(t.getMessage());
          } else {
            System.out.println(message);
          }
          // else do nothing
        }
      };

  protected CommonBaseTest() {
    if (Timber.treeCount() == 0) {
      Timber.plant(TEST_TREE);
    }
  }

  @Before
  @CallSuper
  @SuppressLint("VisibleForTests")
  public void setup() throws Exception {
    FailEarly.overrideDelegateWith(TestDelegate.INSTANCE);
    FailEarly.testMainThreadExecution(false);
  }

  @After
  @CallSuper
  @SuppressLint("VisibleForTests")
  public void tearDown() throws Exception {
    TrustedClockExtensionsKt.reset(TrustedClock.INSTANCE);
    // In case someone changed it
    FailEarly.overrideDelegateWith(TestDelegate.INSTANCE);
  }
}
