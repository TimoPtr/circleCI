/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic

import android.app.Activity
import androidx.annotation.Keep

/** Dummy [AnalyticsTracker] implementation for CoreSDK clients */
@Keep
class NoOpAnalyticsTracker : AnalyticsTracker {

    override fun sendEvent(actionName: String, details: Map<String, String?>) {
        // no-op
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        // no-op
    }
}
