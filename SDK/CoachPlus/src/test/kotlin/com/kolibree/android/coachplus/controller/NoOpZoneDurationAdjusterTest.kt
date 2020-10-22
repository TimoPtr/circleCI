/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.kml.MouthZone16
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

internal class NoOpZoneDurationAdjusterTest : BaseUnitTest() {
    @Test
    fun `getAdjustedDuration returns the parameter divided by 16 zones`() {
        val goalDuration = Duration.of(120L, ChronoUnit.SECONDS)
        val expectedDurationPerZone = goalDuration.toMillis() / MouthZone16.values().size

        val zoneDurationAdjuster = NoOpZoneDurationAdjuster(goalDuration)

        MouthZone16.values().forEach {
            assertEquals(expectedDurationPerZone, zoneDurationAdjuster.getAdjustedDuration(it))
        }
    }
}
