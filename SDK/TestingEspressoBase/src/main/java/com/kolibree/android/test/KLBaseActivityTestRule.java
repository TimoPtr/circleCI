/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

/** Created by miguelaragues on 8/8/17. */
public class KLBaseActivityTestRule<T extends FragmentActivity> extends ActivityTestRule<T> {

  public KLBaseActivityTestRule(Class<T> activityClass, boolean launchActivity) {
    super(activityClass, false, launchActivity);
  }

  protected final Context targetContext() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  @Override
  public T launchActivity(@Nullable Intent startIntent) {
    return super.launchActivity(startIntent);
  }
}
