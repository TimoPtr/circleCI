/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.widget.TextView;
import com.azimolabs.conditionwatcher.ConditionWatcher;
import com.azimolabs.conditionwatcher.Instruction;
import com.kolibree.R;
import java.lang.ref.WeakReference;

/** Created by miguelaragues on 8/3/18. */
public class SnackbarEspressoUtils {

  private SnackbarEspressoUtils() {}

  public static void assertSnackbarWithMessageDisplayed(Activity activity, String message)
      throws Exception {
    ConditionWatcher.waitForCondition(new SnackbarIsDisplayedInstruction(activity));
    onView(withId(R.id.snackbar_text)).check(matches(withText(message)));
  }

  private static class SnackbarIsDisplayedInstruction extends Instruction {

    private final WeakReference<Activity> weakActivity;

    SnackbarIsDisplayedInstruction(Activity activity) {
      weakActivity = new WeakReference<>(activity);
    }

    @Override
    public String getDescription() {
      return "Snackbar is displayed";
    }

    @Override
    public boolean checkCondition() {
      Activity activity = weakActivity.get();
      if (activity == null) {
        return false;
      }

      TextView snackbarText = activity.findViewById(R.id.snackbar_text);
      return snackbarText != null;
    }
  }
}
