/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import java.lang.ref.WeakReference
import javax.inject.Inject
import timber.log.Timber

/**
 * ActivityLifecycleCallbacks that holds WeakReference of created activities and exposes a method
 * to finish all of them in LIFO order, Last In First Finished
 */
internal class CreatedActivitiesWatcher
@Inject constructor() : Application.ActivityLifecycleCallbacks {
    @VisibleForTesting
    val startedActivities: HashSet<WeakReference<Activity>> = LinkedHashSet()

    override fun onActivityPaused(activity: Activity) {
        // no-op
    }

    override fun onActivityResumed(activity: Activity) {
        // no-op
    }

    override fun onActivityStarted(activity: Activity) {
        // no-op
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // no-op
    }

    override fun onActivityStopped(activity: Activity) {
        // no-op
    }

    override fun onActivityDestroyed(activity: Activity) {
        // no-op
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Timber.d("Enforcer activity created %s", activity)
        startedActivities.add(WeakReference(activity))
    }

    fun clear() {
        startedActivities.clear()
    }

    fun finishActivitiesReverseOrder() {
        startedActivities
            .reversed()
            .mapNotNull { it.get() }
            .forEach {
                Timber.d("Enforcer finishing %s", it)
                it.finish()
            }
    }
}
