/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionData
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.DiscreteState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.PercentageState
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionState
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.NeverSync
import com.kolibree.android.offlinebrushings.sync.StartSync
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelDiscrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.LevelPercentage
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

internal class ToothbrushSettingsViewStateTest : BaseUnitTest() {

    @Test
    fun `when toothbrush was never sync lastSyncDate is null`() {
        val viewState = ToothbrushSettingsViewState(
            lastSyncDate = TrustedClock.getNowLocalDate(),
            mac = "mac"
        )
        assertNotNull(viewState.lastSyncDate)

        val updatedViewState = viewState.withLastSyncData(NeverSync("mac"))
        assertNull(updatedViewState.lastSyncDate)
    }

    @Test
    fun `when toothbrush is already syncing lastSyncDate is today`() {
        TrustedClock.utcClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val viewState = ToothbrushSettingsViewState(mac = "mac")
        assertNull(viewState.lastSyncDate)

        val updatedViewState = viewState.withLastSyncData(StartSync("mac"))
        assertEquals(TrustedClock.getNowLocalDate(), updatedViewState.lastSyncDate)
    }

    @Test
    fun `when toothbrush was sync then lastSyncDate is sync date`() {
        TrustedClock.utcClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        val viewState = ToothbrushSettingsViewState(mac = "mac")
        assertNull(viewState.lastSyncDate)

        val syncDate = TrustedClock.getNowZonedDateTime().minusDays(5)
        val updatedViewState = viewState.withLastSyncData(LastSyncDate("mac", syncDate))

        assertEquals(syncDate.toLocalDate(), updatedViewState.lastSyncDate)
    }

    @Test
    fun `when connection is active and not currently being identifying it's possible to identify it`() {
        assertTrue(
            ToothbrushSettingsViewState(
                mac = "mac",
                connectionState = ConnectionState.CONNECTED,
                isIdentifying = false
            ).isIdentifyPossible()
        )
    }

    @Test
    fun `when connection is active and currently being identifying it's not possible to identify it`() {
        assertFalse(
            ToothbrushSettingsViewState(
                mac = "mac",
                connectionState = ConnectionState.CONNECTED,
                isIdentifying = true
            ).isIdentifyPossible()
        )
    }

    @Test
    fun `when connection is not active and not currently being identifying it's not possible to identify it`() {
        assertFalse(
            ToothbrushSettingsViewState(
                mac = "mac",
                connectionState = ConnectionState.DISCONNECTED,
                isIdentifying = false
            ).isIdentifyPossible()
        )
    }

    @Test
    fun `when otaType is mandatory it expose the right result`() {
        val vs = ToothbrushSettingsViewState(
            mac = "mac",
            connectionState = ConnectionState.CONNECTED,
            otaUpdateType = OtaUpdateType.MANDATORY
        )
        assertTrue(vs.isMandatoryOtaAvailable())
        assertFalse(vs.isOptionalOtaAvailable())
    }

    @Test
    fun `when otaType is standard it expose the right result`() {
        val vs = ToothbrushSettingsViewState(
            mac = "mac",
            connectionState = ConnectionState.CONNECTED,
            otaUpdateType = OtaUpdateType.STANDARD
        )
        assertTrue(vs.isOptionalOtaAvailable())
        assertFalse(vs.isMandatoryOtaAvailable())
    }

    @Test
    fun `when otaType is null it expose the right result`() {
        val vs = ToothbrushSettingsViewState(
            mac = "mac",
            connectionState = ConnectionState.CONNECTED,
            otaUpdateType = null
        )
        assertFalse(vs.isOptionalOtaAvailable())
        assertFalse(vs.isMandatoryOtaAvailable())
    }

    @Test
    fun `when otaType is not null but connection is not active it doesn't offer ota`() {
        val vs = ToothbrushSettingsViewState(
            mac = "mac",
            connectionState = ConnectionState.DISCONNECTED,
            otaUpdateType = OtaUpdateType.STANDARD
        )
        assertFalse(vs.isOptionalOtaAvailable())
        assertFalse(vs.isMandatoryOtaAvailable())
    }

    @Test
    fun `when battery is 6 Month LevelDiscrete it should returns right formed state`() {
        val viewState = ToothbrushSettingsViewState.initial("")
            .withBatteryLevel(LevelDiscrete(Level6Month))

        assertEquals(
            DiscreteState(
                R.string.tb_settings_battery_level_6_months,
                R.drawable.ic_battery_level_100
            ),
            viewState.batteryLevel
        )
    }

    @Test
    fun `when battery is 3 Month LevelDiscrete it should returns right formed state`() {
        val viewState = ToothbrushSettingsViewState.initial("")
            .withBatteryLevel(LevelDiscrete(Level3Month))

        assertEquals(
            DiscreteState(
                R.string.tb_settings_battery_level_3_months,
                R.drawable.ic_battery_level_75
            ),
            viewState.batteryLevel
        )
    }

    @Test
    fun `when battery is few weeks LevelDiscrete it should returns right formed state`() {
        val viewState = ToothbrushSettingsViewState.initial("")
            .withBatteryLevel(LevelDiscrete(LevelFewWeeks))

        assertEquals(
            DiscreteState(
                R.string.tb_settings_battery_level_weeks,
                R.drawable.ic_battery_level_25
            ),
            viewState.batteryLevel
        )
    }

    @Test
    fun `when battery is few days LevelDiscrete it should returns right formed state`() {
        val viewState = ToothbrushSettingsViewState.initial("")
            .withBatteryLevel(LevelDiscrete(LevelFewDays))

        assertEquals(
            DiscreteState(
                R.string.tb_settings_battery_level_days,
                R.drawable.ic_battery_level_0
            ),
            viewState.batteryLevel
        )
    }

    @Test
    fun `when battery is 0% it should returns right formed state`() {
        val viewState = ToothbrushSettingsViewState.initial("")
            .withBatteryLevel(LevelPercentage(0))

        assertEquals(
            PercentageState(
                "0%", R.drawable.ic_battery_level_0
            ),
            viewState.batteryLevel
        )
    }

    @Test
    fun `when battery is between 1-25% it should returns right formed state`() {
        val expectedDrawable = R.drawable.ic_battery_level_25
        val viewState = ToothbrushSettingsViewState.initial("")

        assertEquals(
            PercentageState("1%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(1)).batteryLevel
        )

        assertEquals(
            PercentageState("25%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(25)).batteryLevel
        )
    }

    @Test
    fun `when battery is between 26-75% it should returns right formed state`() {
        val expectedDrawable = R.drawable.ic_battery_level_75

        val viewState = ToothbrushSettingsViewState.initial("")

        assertEquals(
            PercentageState("26%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(26)).batteryLevel
        )

        assertEquals(
            PercentageState("75%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(75)).batteryLevel
        )
    }

    @Test
    fun `when battery is above 75% it should returns right formed state`() {
        val expectedDrawable = R.drawable.ic_battery_level_100

        val viewState = ToothbrushSettingsViewState.initial("")

        assertEquals(
            PercentageState("76%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(76)).batteryLevel
        )

        assertEquals(
            PercentageState("100%", expectedDrawable),
            viewState.withBatteryLevel(LevelPercentage(100)).batteryLevel
        )
    }

    @Test
    fun `when the brush head condition is GOOD, the condition state should be right formed`() {

        val frenchRevolutionDate = LocalDate.of(1789, 7, 14)

        val viewState = ToothbrushSettingsViewState.initial("")
            .withBrushHeadConditionData(BrushHeadConditionData(
                BrushHeadCondition.GOOD,
                frenchRevolutionDate
            ))

        assertEquals(BrushHeadConditionState.GOOD, viewState.brushHeadConditionState)
        assertEquals(frenchRevolutionDate, viewState.brushHeadReplacementDate)
    }

    @Test
    fun `when the brush head condition is GETTING_OLDER, the condition state should be right formed`() {

        val inaugurationDate = LocalDate.of(1994, 5, 10)

        val viewState = ToothbrushSettingsViewState.initial("")
            .withBrushHeadConditionData(BrushHeadConditionData(
                BrushHeadCondition.GETTING_OLDER,
                inaugurationDate
            ))

        assertEquals(BrushHeadConditionState.GETTING_OLDER, viewState.brushHeadConditionState)
        assertEquals(inaugurationDate, viewState.brushHeadReplacementDate)
    }

    @Test
    fun `when the brush head condition is NEEDS_REPLACEMENT, the condition state should be right formed`() {

        val kickedAssDate = LocalDate.of(1815, 6, 18)

        val viewState = ToothbrushSettingsViewState.initial("")
            .withBrushHeadConditionData(BrushHeadConditionData(
                BrushHeadCondition.NEEDS_REPLACEMENT,
                kickedAssDate
            ))

        assertEquals(BrushHeadConditionState.NEEDS_REPLACEMENT, viewState.brushHeadConditionState)
        assertEquals(kickedAssDate, viewState.brushHeadReplacementDate)
    }
}
