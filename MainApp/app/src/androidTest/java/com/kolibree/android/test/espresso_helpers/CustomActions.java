/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.espresso_helpers;

import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.graphics.drawable.ColorDrawable;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import org.hamcrest.Matcher;

/** Created by miguelaragues on 3/1/18. */
public abstract class CustomActions {

  private CustomActions() {}

  /**
   * Only useful if ProgressBar isn't visible at the start
   *
   * <p>See https://stackoverflow.com/a/37864603/218473
   */
  public static ViewAction replaceProgressBarDrawable() {
    return actionWithAssertions(
        new ViewAction() {
          @Override
          public Matcher<View> getConstraints() {
            return isAssignableFrom(ProgressBar.class);
          }

          @Override
          public String getDescription() {
            return "replace the ProgressBar drawable";
          }

          @Override
          public void perform(final UiController uiController, final View view) {
            // Replace the indeterminate drawable with a static red ColorDrawable
            ProgressBar progressBar = (ProgressBar) view;
            progressBar.setIndeterminateDrawable(new ColorDrawable(0xffff0000));
            uiController.loopMainThreadUntilIdle();
          }
        });
  }

  public static ViewAction clickTopCenter() {
    return clickAtLocation(GeneralLocation.TOP_CENTER);
  }

  public static ViewAction clickBottomCenter() {
    return clickAtLocation(GeneralLocation.BOTTOM_CENTER);
  }

  private static ViewAction clickAtLocation(GeneralLocation location) {
    return actionWithAssertions(
        new GeneralClickAction(
            Tap.SINGLE,
            location,
            Press.FINGER,
            InputDevice.SOURCE_UNKNOWN,
            MotionEvent.BUTTON_PRIMARY));
  }
}
