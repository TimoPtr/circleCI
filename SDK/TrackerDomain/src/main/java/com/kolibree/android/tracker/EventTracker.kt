package com.kolibree.android.tracker

import android.app.Activity
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleObserver

@Keep
interface EventTracker : LifecycleObserver {

    fun sendEvent(event: AnalyticsEvent)

    fun setCurrentScreen(activity: Activity, screenName: String)
}
