/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.extensions.setFixedEpochInstant
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test

class BrushingModePerProfileRepositoryTest : BaseInstrumentationTest() {

    private val repository = BrushingModePerProfileRepository(context())

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        repository.clear()
    }

    override fun tearDown() {
        super.tearDown()

        repository.clear()
    }

    @Test
    fun repository_storeAndRestore_returnsExpectedValues() {
        val profileId = 56L

        val expectedBrushingMode = BrushingMode.Slow

        assertNull(repository.getForProfile(profileId))

        TrustedClock.setFixedDate()
        val expectedLocalDateTime = TrustedClock.getNowOffsetDateTime()

        repository.setForProfile(profileId, expectedBrushingMode)

        val brushingModeForProfile = repository.getForProfile(profileId)

        assertNotNull(brushingModeForProfile)

        assertEquals(expectedBrushingMode, brushingModeForProfile!!.brushingMode)
        assertEquals(expectedLocalDateTime, brushingModeForProfile.dateTime)
    }

    @Test
    fun setForProfile_overwritesPreviousValueForProfile() {
        val profileId = 56L

        val initialBrushingMode = BrushingMode.Slow

        assertNull(repository.getForProfile(profileId))

        TrustedClock.setFixedDate()
        val initialLocalDateTime = TrustedClock.getNowOffsetDateTime()

        repository.setForProfile(profileId, initialBrushingMode)

        TrustedClock.setFixedEpochInstant(initialLocalDateTime.plusHours(3).toEpochSecond())
        val expectedLocalDateTime = TrustedClock.getNowOffsetDateTime()

        val expectedBrushingMode = BrushingMode.Strong
        repository.setForProfile(profileId, expectedBrushingMode)

        val brushingModeForProfile = repository.getForProfile(profileId)

        assertEquals(expectedBrushingMode, brushingModeForProfile!!.brushingMode)
        assertEquals(expectedLocalDateTime, brushingModeForProfile.dateTime)
    }

    @Test
    fun repository_storeAndRestore_returnsNullForDifferentProfile() {
        val profileId = 56L

        val expectedBrushingMode = BrushingMode.Slow

        repository.setForProfile(profileId, expectedBrushingMode)

        assertNull(repository.getForProfile(profileId + 1))
    }
}
