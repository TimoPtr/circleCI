/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.brushing.wrapper

import java.time.Duration
import junit.framework.TestCase.assertEquals
import org.junit.Test

class IBrushingTest {

    @Test
    fun `brushingGoalTimes generates range from 2 to 5 minutes with 5s step`() {
        val expected = IntProgression.fromClosedRange(
            rangeStart = Duration.ofMinutes(2).seconds.toInt(),
            rangeEnd = Duration.ofMinutes(5).seconds.toInt(),
            step = Duration.ofSeconds(5).seconds.toInt()
        ).toList()

        assertEquals(expected.size, IBrushing.brushingGoalTimes().size)
        assert(IBrushing.brushingGoalTimes().containsAll(expected))
    }
}
