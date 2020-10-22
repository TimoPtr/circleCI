/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.tracker

import android.annotation.SuppressLint
import android.app.Activity
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting

@Keep
object Analytics {

    @VisibleForTesting
    var eventTracker: EventTracker = VoidEventTracker()

    // TODO I think since this is and was static before, it may lead to leaks,
    //  as KolibreeEventTracker has really decent list of deps. I will keep it
    //  like this for now, as this may seriously change the behaviour...
    //  https://kolibree.atlassian.net/browse/KLTB002-10784
    @JvmStatic
    fun init(eventTracker: EventTracker): Analytics {
        this.eventTracker = eventTracker
        return this
    }

    @JvmStatic
    fun send(event: AnalyticsEvent) = eventTracker.sendEvent(event)

    @JvmStatic
    fun setCurrentScreen(activity: Activity, screenName: String) = eventTracker.setCurrentScreen(activity, screenName)
}

@SuppressLint("DeobfuscatedPublicSdkClass")
class VoidEventTracker : EventTracker {

    override fun sendEvent(event: AnalyticsEvent) {
        // no-op
    }

    override fun setCurrentScreen(activity: Activity, screenName: String) {
        // no-op
    }
}
