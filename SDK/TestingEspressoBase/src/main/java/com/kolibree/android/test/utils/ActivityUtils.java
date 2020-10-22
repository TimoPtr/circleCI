/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils;

import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

/** Created by miguelaragues on 17/1/18. */
public abstract class ActivityUtils {

  private ActivityUtils() {}

  public static <T extends AppCompatActivity> void assertResult(
      ActivityTestRule<T> activityTestRule, int expectedResult) {
    assertThat(activityTestRule.getActivityResult(), hasResultCode(expectedResult));
  }

  public static <T extends AppCompatActivity> void assertResultOk(ActivityTestRule<T> testRule) {
    assertResult(testRule, Activity.RESULT_OK);
  }

  public static void assertActivityIsFinished(Activity activity) {
    assertActivityIsFinished(activity, true);
  }

  public static void assertActivityIsAlive(Activity activity) {
    assertActivityIsFinished(activity, false);
  }

  private static void assertActivityIsFinished(Activity activity, boolean isFinished) {
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    assertEquals(isFinished, activity.isDestroyed() || activity.isFinishing());
  }
}
