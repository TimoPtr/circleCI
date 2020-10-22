package com.kolibree.android.coachplus

import androidx.annotation.Keep
import com.kolibree.android.tracker.AnalyticsEvent

@Keep
interface CoachPlusAnalytics {

    fun main(): AnalyticsEvent

    fun pause(): AnalyticsEvent

    fun quit(): AnalyticsEvent

    fun resume(): AnalyticsEvent

    fun restart(): AnalyticsEvent
}
