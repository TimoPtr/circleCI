/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import androidx.fragment.app.FragmentActivity;
import androidx.test.filters.LargeTest;
import com.kolibree.android.app.test.rules.TestSchedulerRxSchedulersOverrideRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * If there are timers/interval/debounces, this won't work
 *
 * <p>Created by miguelaragues on 8/8/17.
 */
@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4.class)
public abstract class BaseAsyncEspressoTest<T extends FragmentActivity>
    extends BaseActivityCommonEspressoTest<T> {

  @Rule
  public final TestSchedulerRxSchedulersOverrideRule asyncScheduler =
      new TestSchedulerRxSchedulersOverrideRule();
}
