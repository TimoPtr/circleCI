package com.kolibree.android.tracker

import androidx.annotation.Keep

/** Created by Kornel on 3/13/2018.  */
@Keep
interface TrackableScreen {

    fun getScreenName(): AnalyticsEvent
}
