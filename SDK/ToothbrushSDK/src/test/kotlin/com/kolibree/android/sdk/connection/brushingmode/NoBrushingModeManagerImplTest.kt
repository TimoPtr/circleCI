/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeLastSegmentStrategy
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeStrengthOption
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.error.CommandNotSupportedException
import org.junit.Assert.assertFalse
import org.junit.Test

/** [NoBrushingModeManagerImpl] tests */
class NoBrushingModeManagerImplTest : BaseUnitTest() {

    private val noBrushingModeManagerImpl = NoBrushingModeManagerImpl()

    @Test
    fun `isAvailable returns false`() = assertFalse(noBrushingModeManagerImpl.isAvailable())

    @Test
    fun `availableBrushingModes emits CommandNotSupportedException`() {
        noBrushingModeManagerImpl
            .availableBrushingModes()
            .test()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `lastUpdateDate emits CommandNotSupportedException`() {
        noBrushingModeManagerImpl
            .lastUpdateDate()
            .test()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `set emits CommandNotSupportedException`() {
        noBrushingModeManagerImpl
            .set(BrushingMode.Regular)
            .test()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `getCurrent emits CommandNotSupportedException`() {
        noBrushingModeManagerImpl
            .getCurrent()
            .test()
            .assertNoValues()
            .assertNotComplete()
            .assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `setCustomBrushingModeSettings throws CommandNotSupportedException`() {
        noBrushingModeManagerImpl.customize().setCustomBrushingModeSettings(
            BrushingModeSettingsBuilder()
                .addSegmentWithSequence(BrushingModeSequence.GumCare, 2)
                .lastSegmentStrategy(BrushingModeLastSegmentStrategy.EndAfterLastSegment)
                .strengthOption(BrushingModeStrengthOption.ThreeLevels)
                .lastSegment(BrushingModeSequence.GumCare, 2)
                .build()
        ).test().assertError(CommandNotSupportedException::class.java)
    }
}
