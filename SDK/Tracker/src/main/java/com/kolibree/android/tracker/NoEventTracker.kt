/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker

import android.app.Activity
import androidx.annotation.Keep

/** Dummy [EventTracker] implementation for CoreSDK clients */
@Keep
class NoEventTracker : EventTracker {

    override fun sendEvent(event: AnalyticsEvent) {
        // no-op
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        // no-op
    }
}
