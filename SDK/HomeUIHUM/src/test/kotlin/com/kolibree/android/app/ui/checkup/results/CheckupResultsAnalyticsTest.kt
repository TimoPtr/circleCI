/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.results

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.AnalyticsEvent
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class CheckupResultsAnalyticsTest : BaseUnitTest() {

    @Test
    fun `fromOrigin HOME returns right tag`() {
        assertEquals(AnalyticsEvent("Checkup"), CheckupResultsAnalytics.fromOrigin(CheckupOrigin.HOME))
    }

    @Test
    fun `fromOrigin TEST_BRUSHING returns right tag`() {
        assertEquals(AnalyticsEvent("TestBrushing_Results"), CheckupResultsAnalytics.fromOrigin(CheckupOrigin.TEST_BRUSHING))
    }

    @Test
    fun `fromOrigin GUIDED_BRUSHING returns right tag`() {
        assertEquals(AnalyticsEvent("GuidedBrushing_Results"), CheckupResultsAnalytics.fromOrigin(CheckupOrigin.GUIDED_BRUSHING))
    }
}
