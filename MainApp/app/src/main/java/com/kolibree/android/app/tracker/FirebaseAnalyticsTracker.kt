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
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.kolibree.android.app.crashlogger.CrashLogger
import javax.inject.Inject

internal class FirebaseAnalyticsTracker @Inject constructor(
    context: Context
) : FirebaseAnalyticsTrackerBase() {

    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(eventName: String, arguments: Map<String, String>?) {
        if (arguments == null) {
            analytics.logEvent(eventName, null)
            return
        }

        val bundle = Bundle()
        for ((key, value) in arguments) {
            bundle.putString(key, value)
        }
        analytics.logEvent(eventName, bundle)
    }

    override fun setUserProperty(key: String, value: String) {
        analytics.setUserProperty(key, value)
        CrashLogger.setUserProperty(key, value)
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        analytics.setCurrentScreen(activity, screenName, null)
    }
}
