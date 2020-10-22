/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.lowbattery

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelDiscrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelPercentage
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class LowBatteryUseCaseImplTest : BaseUnitTest() {

    private val lowBatteryProvider: LowBatteryProvider = mock()

    private lateinit var useCase: LowBatteryUseCaseImpl

    override fun setup() {
        super.setup()

        useCase = LowBatteryUseCaseImpl(lowBatteryProvider)
    }

    @Test
    fun `when battery is 15% and warning has not been shown, low battery requirement should returns true`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(false)

        useCase.isMatchingWarningRequirement(LevelPercentage(15))
            .test().assertValue(true)
    }

    @Test
    fun `when battery is 0% and warning has not been shown, low battery requirement should returns true`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(false)

        useCase.isMatchingWarningRequirement(LevelPercentage(0))
            .test().assertValue(true)
    }

    @Test
    fun `when battery is few days left and warning has not been shown, low battery requirement should returns true`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(false)

        useCase.isMatchingWarningRequirement(LevelDiscrete(LevelFewDays))
            .test().assertValue(true)
    }

    @Test
    fun `when battery discrete is above few days and warning has not been shown, low battery requirement should returns false in any cases`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(false)

        useCase.isMatchingWarningRequirement(LevelDiscrete(LevelFewWeeks))
            .test().assertValue(false)

        useCase.isMatchingWarningRequirement(LevelDiscrete(Level3Month))
            .test().assertValue(false)

        useCase.isMatchingWarningRequirement(LevelDiscrete(Level6Month))
            .test().assertValue(false)
    }

    @Test
    fun `when battery is above 15% and warning has not been shown, low battery requirement should returns false in any cases`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(false)

        useCase.isMatchingWarningRequirement(LevelPercentage(16))
            .test().assertValue(false)

        useCase.isMatchingWarningRequirement(LevelPercentage(50))
            .test().assertValue(false)

        useCase.isMatchingWarningRequirement(LevelPercentage(100))
            .test().assertValue(false)
    }

    @Test
    fun `when battery is low and warning has been shown, low battery requirement should returns false`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(true)

        useCase.isMatchingWarningRequirement(LevelPercentage(0))
            .test().assertValue(false)
    }

    @Test
    fun `when battery is not low and warning has been shown, it should reset the warning`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(true)

        useCase.isMatchingWarningRequirement(LevelPercentage(100)).test()

        verify(lowBatteryProvider).setWarningShown(false)
    }

    @Test
    fun `when battery is not low and warning has been shown, low battery requirement should returns false`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(true)

        useCase.isMatchingWarningRequirement(LevelPercentage(100))
            .test().assertValue(false)
    }

    @Test
    fun `setWarningShown should call lowBatteryProvider setWarningShown with true`() {
        whenever(lowBatteryProvider.isWarningShown()).thenReturn(true)

        useCase.setWarningShown().test()

        verify(lowBatteryProvider).setWarningShown(true)
    }
}
