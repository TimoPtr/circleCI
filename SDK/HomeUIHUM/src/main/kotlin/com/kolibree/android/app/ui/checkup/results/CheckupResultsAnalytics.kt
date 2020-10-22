/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object CheckupResultsAnalytics {
    @VisibleForTesting
    fun fromOrigin(origin: CheckupOrigin): AnalyticsEvent = AnalyticsEvent(
        when (origin) {
            CheckupOrigin.HOME -> "Checkup"
            CheckupOrigin.TEST_BRUSHING -> "TestBrushing_Results"
            CheckupOrigin.GUIDED_BRUSHING -> "GuidedBrushing_Results"
        }
    )

    fun main(origin: CheckupOrigin) = fromOrigin(origin)

    fun close(origin: CheckupOrigin?) =
        origin?.let { send(fromOrigin(it) + "Quit") } ?: FailEarly.fail("close: Missing origin")

    fun collect(origin: CheckupOrigin?) =
        origin?.let { send(fromOrigin(it) + "Collect") }
            ?: FailEarly.fail("collect: Missing origin")

    fun delete(origin: CheckupOrigin?) =
        origin?.let { send(fromOrigin(it) + "Delete") } ?: FailEarly.fail("delete: Missing origin")
}
