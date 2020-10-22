/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import timber.log.Timber;

/** Created by miguelaragues on 11/1/18. */
public final class EspressoUtils {

  private EspressoUtils() {}

  @Nullable
  public static <T extends FragmentActivity> FragmentActivity waitForActivityToBeDisplayed(
      Class<T> clazz) {
    final Boolean[] timedOut = new Boolean[] {false};

    Thread timeoutThread =
        new Thread(
            () -> {
              try {
                Thread.sleep(10000);

                Timber.w("Espresso Timed out waiting for activity %s", clazz);

                timedOut[0] = true;
              } catch (InterruptedException e) {
                // ignore
              }
            });
    timeoutThread.start();

    while (!timedOut[0]) {
      FragmentActivity currentActivty = EspressoUtils.getCurrentActivity();

      if (currentActivty != null && currentActivty.getClass().equals(clazz)) {
        timeoutThread.interrupt();

        return currentActivty;
      }
    }

    return null;
  }

  public static FragmentActivity getCurrentActivity() {
    getInstrumentation().waitForIdleSync();
    final Activity[] activity = new Activity[1];
    try {
      getInstrumentation()
          .runOnMainSync(
              () -> {
                try {
                  java.util.Collection<Activity> activites =
                      ActivityLifecycleMonitorRegistry.getInstance()
                          .getActivitiesInStage(Stage.RESUMED);
                  activity[0] = Iterables.getOnlyElement(activites);
                } catch (Throwable throwable) {
                  throwable.printStackTrace();
                  activity[0] = null;
                }
              });
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return (FragmentActivity) activity[0];
  }

  @Nullable
  public static Activity getActivity(Context context) {
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return null;
  }
}
