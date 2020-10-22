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

@Keep
interface AnalyticsTracker {

    fun sendEvent(actionName: String, details: Map<String, String?>)

    fun setCurrentScreen(activity: Activity, screenName: String)
}
