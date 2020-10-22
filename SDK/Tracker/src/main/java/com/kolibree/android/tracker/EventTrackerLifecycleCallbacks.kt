package com.kolibree.android.tracker

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

/** Created by Kornel on 3/13/2018.  */
@Keep
abstract class EventTrackerLifecycleCallbacks : ActivityLifecycleCallbacks {

    abstract fun registerEventTracker(lifecycle: Lifecycle)

    abstract fun unregisterEventTracker(lifecycle: Lifecycle)

    abstract fun setScreenName(activity: Activity, screenName: String)

    override fun onActivityResumed(activity: Activity) {
        if (activity is LifecycleOwner) {
            val shouldTriggerEvent = activity !is NonTrackableScreen
            if (shouldTriggerEvent) {
                setScreenName(
                    activity,
                    (activity as? TrackableScreen)?.let {
                        (activity as TrackableScreen).getScreenName().name
                    } ?: activity.javaClass.simpleName
                )
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is LifecycleOwner) {
            registerEventTracker((activity as LifecycleOwner).lifecycle)
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity is LifecycleOwner) {
            unregisterEventTracker((activity as LifecycleOwner).lifecycle)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        // no-op
    }

    override fun onActivityPaused(activity: Activity) {
        // no-op
    }

    override fun onActivityStopped(activity: Activity) {
        // no-op
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // no-op
    }
}
