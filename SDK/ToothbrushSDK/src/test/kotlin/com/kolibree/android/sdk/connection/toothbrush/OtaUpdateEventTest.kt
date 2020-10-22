/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromAction
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent.Companion.fromProgressiveAction
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class OtaUpdateEventTest : BaseUnitTest() {
    /*
    updateWithEvent
     */
    @Test
    fun `updateWithEvent returns event with action and errorMessageId from parameter event`() {
        val event = fromAction(OTA_UPDATE_REBOOTING)

        val expectedActionId = OTA_UPDATE_INSTALLING
        val expectedErrorMessageId = 35
        val parameterEvent =
            OtaUpdateEvent(action = expectedActionId, errorMessageId = expectedErrorMessageId)

        val newEvent = event.updateWithEvent(parameterEvent)

        assertEquals(expectedActionId, newEvent.action)
        assertEquals(expectedErrorMessageId, newEvent.errorMessageId)
    }

    @Test
    fun `when current progress is null and parameter progress is null, updateWithEvent returns event with progress null`() {
        val event = fromAction(OTA_UPDATE_REBOOTING)

        assertNull(event.progress)

        assertNull(event.updateWithEvent(event).progress)
    }

    @Test
    fun `when current progress is null and parameter progress is not null, updateWithEvent returns event with progress from parameter`() {
        val event = fromAction(OTA_UPDATE_REBOOTING)
        assertNull(event.progress)

        val expectedProgress = 56
        val parameterEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress)

        assertEquals(expectedProgress, event.updateWithEvent(parameterEvent).progress)
    }

    @Test
    fun `when current progress is not null and parameter progress is null, updateWithEvent returns event with progress from current item`() {
        val expectedProgress = 56
        val event = fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress)

        val parameterEvent = fromAction(OTA_UPDATE_REBOOTING)
        assertNull(parameterEvent.progress)

        assertEquals(expectedProgress, event.updateWithEvent(parameterEvent).progress)
    }

    @Test
    fun `when current progress is not null and parameter progress is zero, updateWithEvent returns event with progress from current item`() {
        val expectedProgress = 56
        val event = fromProgressiveAction(OTA_UPDATE_INSTALLING, expectedProgress)

        val parameterEvent = fromProgressiveAction(OTA_UPDATE_INSTALLING, 0)

        assertEquals(expectedProgress, event.updateWithEvent(parameterEvent).progress)
    }

    /*
    isProgressComplete
     */
    @Test
    fun `when progress is null, isProgressComplete returns false`() {
        val event = fromAction(OTA_UPDATE_REBOOTING)

        assertNull(event.progress)

        assertFalse(event.isProgressCompleted())
    }

    @Test
    fun `when progress is below 100, isProgressComplete returns false`() {
        (0 until 100).forEach { progress ->
            assertFalse(
                "Failed for $progress",
                fromProgressiveAction(OTA_UPDATE_INSTALLING, progress).isProgressCompleted()
            )
        }
    }

    @Test
    fun `when progress is 100, isProgressComplete returns true`() {
        assertTrue(fromProgressiveAction(OTA_UPDATE_INSTALLING, 100).isProgressCompleted())
    }
}
