/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.model

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalTime

internal class BrushReminderTypeTest : BaseUnitTest() {

    @Test
    fun `morning reminder default time is 8AM`() {
        assertEquals(LocalTime.of(8, 0), BrushingReminderType.MORNING.defaultLocalTime())
    }

    @Test
    fun `afternoon reminder default time is 1PM`() {
        assertEquals(LocalTime.of(13, 0), BrushingReminderType.AFTERNOON.defaultLocalTime())
    }

    @Test
    fun `evening reminder default time is 8PM`() {
        assertEquals(LocalTime.of(20, 0), BrushingReminderType.EVENING.defaultLocalTime())
    }
}
