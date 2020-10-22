/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import android.app.Activity
import android.os.Looper
import android.view.View
import androidx.annotation.IdRes
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

internal object IdlingResourceUtils {

    fun <T : View> findView(@IdRes resId: Int): T? {
        return getCurrentActivity()?.findViewById<T>(resId)
    }

    fun getCurrentActivity(): Activity? {
        var currentActivity: Activity? = null
        if (Looper.myLooper() == Looper.getMainLooper()) {
            currentActivity = getFirstResumedActivity()
        } else {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                currentActivity = getFirstResumedActivity()
            }
        }
        return currentActivity
    }

    /**
     * Return the current activity
     * Should be run on main thread
     */
    private fun getFirstResumedActivity(): Activity? {
        return ActivityLifecycleMonitorRegistry
            .getInstance()
            .getActivitiesInStage(Stage.RESUMED)
            .firstOrNull()
    }
}
