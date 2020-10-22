/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.EventTracker
import com.kolibree.android.tracker.EventTrackerLifecycleCallbacks
import javax.inject.Inject

internal class EventTrackerAppInitializer @Inject constructor() : AppInitializer {

    /**
     * We need this dirty hack because [BaseKolibreeApplication.appComponent]
     * is replaced in UI tests
     */
    private val eventTracker: EventTracker
        get() = BaseKolibreeApplication.appComponent.eventTracker()

    override fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : EventTrackerLifecycleCallbacks() {
                override fun registerEventTracker(lifecycle: Lifecycle) {
                    lifecycle.addObserver(eventTracker)
                    Analytics.init(eventTracker)
                }

                override fun unregisterEventTracker(lifecycle: Lifecycle) {
                    lifecycle.removeObserver(eventTracker)
                }

                override fun setScreenName(activity: Activity, screenName: String) {
                    eventTracker.setCurrentScreen(activity, screenName)
                }
            })
    }
}
