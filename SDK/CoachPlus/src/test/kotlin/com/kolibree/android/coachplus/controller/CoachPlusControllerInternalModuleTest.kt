/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import com.kolibree.android.coachplus.CoachPlusArgumentProvider
import com.kolibree.android.coachplus.di.CoachPlusControllerInternalModule
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.DEFAULT_BRUSHING_DURATION_SECONDS
import com.kolibree.android.coachplus.di.CoachPlusInjectionConstraints.TICK_PERIOD
import com.kolibree.android.commons.ToothbrushModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.Duration

class CoachPlusControllerInternalModuleTest {
    @Test
    fun `providesZoneDurationAdjuster returns BrushingModeZoneDurationAdjuster if the toothbrush model supports BrushingMode`() {
        val argumentProvider: CoachPlusArgumentProvider = mock()

        val noOpLazy = dagger.Lazy { mock<NoOpZoneDurationAdjuster>() }
        val brushingModeAdjuster = mock<BrushingModeZoneDurationAdjuster>()
        val brushingModeAdjusterLazy = dagger.Lazy { brushingModeAdjuster }
        ToothbrushModel.values()
            .filter { it.supportsVibrationSpeedUpdate() }
            .forEach { model ->
                whenever(argumentProvider.provideToothbrushModel()).thenReturn(model)

                assertEquals(
                    brushingModeAdjuster,
                    CoachPlusControllerInternalModule.providesZoneDurationAdjuster(
                        argumentProvider,
                        noOpLazy,
                        brushingModeAdjusterLazy
                    )
                )
            }
    }

    @Test
    fun `providesZoneDurationAdjuster returns NoOpZoneDurationAdjuster if the toothbrush model doesn't support BrushingMode`() {
        val argumentProvider: CoachPlusArgumentProvider = mock()

        val noOpAdjuster = mock<NoOpZoneDurationAdjuster>()
        val noOpLazy = dagger.Lazy { noOpAdjuster }
        val brushingModeAdjusterLazy = dagger.Lazy { mock<BrushingModeZoneDurationAdjuster>() }
        ToothbrushModel.values()
            .filterNot { it.supportsVibrationSpeedUpdate() }
            .forEach { model ->
                whenever(argumentProvider.provideToothbrushModel()).thenReturn(model)

                assertEquals(
                    noOpAdjuster,
                    CoachPlusControllerInternalModule.providesZoneDurationAdjuster(
                        argumentProvider,
                        noOpLazy,
                        brushingModeAdjusterLazy
                    )
                )
            }
    }

    @Test
    fun `default GoalBrushingDuration is 2 Minutes`() {
        assertEquals(120, DEFAULT_BRUSHING_DURATION_SECONDS)
    }

    @Test
    fun `tickPeriod is 25 Milliseconds`() {
        assertEquals(Duration.ofMillis(25), TICK_PERIOD)
    }
}
