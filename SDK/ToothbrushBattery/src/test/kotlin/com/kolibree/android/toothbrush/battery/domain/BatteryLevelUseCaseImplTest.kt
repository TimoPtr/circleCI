/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelDiscrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelPercentage
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelUnknown
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class BatteryLevelUseCaseImplTest : BaseUnitTest() {

    private val useCase = BatteryLevelUseCaseImpl()

    @Test
    fun `when tb supports discrete levels then use discreteBatteryLevel`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBattery(DiscreteBatteryLevel.BATTERY_6_MONTHS)
            .build()

        useCase.batteryLevel(connection).test()

        verify(connection.toothbrush().battery()).discreteBatteryLevel
    }

    @Test
    fun `when tb connection is not active, send unknown state`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.TERMINATED)
            .withBattery(DiscreteBatteryLevel.BATTERY_6_MONTHS)
            .build()

        val batteryLevelTest = useCase.batteryLevel(connection).test()

        batteryLevelTest.assertValue(LevelUnknown)
    }

    @Test
    fun `when tb supports discrete levels then returns BatteryLevelDiscrete`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBattery(DiscreteBatteryLevel.BATTERY_6_MONTHS)
            .build()
        val batteryLevelTest = useCase.batteryLevel(connection).test()

        batteryLevelTest.assertValue(LevelDiscrete(Level6Month))
    }

    @Test
    fun `when tb does not supports discrete levels then use batteryLevel`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBattery(level = 98)
            .build()

        useCase.batteryLevel(connection).test()

        verify(connection.toothbrush().battery()).batteryLevel
    }

    @Test
    fun `when tb supports discrete levels then returns BatteryLevelPercentage`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBattery(level = 65)
            .build()

        val batteryLevelTest = useCase.batteryLevel(connection).test()

        batteryLevelTest.assertValue(LevelPercentage(65))
    }

    @Test
    fun `discreteBatteryLevel returns an appropriate object depending on discreteLevel`() {
        assertEquals(
            LevelDiscrete(Level6Month),
            useCase.discreteBatteryLevel(DiscreteBatteryLevel.BATTERY_6_MONTHS)
        )

        assertEquals(
            LevelDiscrete(Level3Month),
            useCase.discreteBatteryLevel(DiscreteBatteryLevel.BATTERY_3_MONTHS)
        )

        assertEquals(
            LevelDiscrete(LevelFewWeeks),
            useCase.discreteBatteryLevel(DiscreteBatteryLevel.BATTERY_FEW_WEEKS)
        )

        assertEquals(
            LevelDiscrete(LevelFewDays),
            useCase.discreteBatteryLevel(DiscreteBatteryLevel.BATTERY_FEW_DAYS)
        )
    }
}
