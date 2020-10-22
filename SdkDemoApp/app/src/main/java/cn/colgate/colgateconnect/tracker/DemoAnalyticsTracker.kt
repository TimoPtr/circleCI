/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.tracker

import android.app.Activity
import android.content.Context
import com.kolibree.android.tracker.logic.AnalyticsTracker
import javax.inject.Inject
import timber.log.Timber

internal class DemoAnalyticsTracker @Inject constructor(context: Context) : AnalyticsTracker {

    override fun sendEvent(actionName: String, details: Map<String, String?>) {
        Timber.i("Event received, eventName: %s, eventDetails: %s", actionName, details)
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        Timber.i("Screen name received: %s", screenName)
    }
}
