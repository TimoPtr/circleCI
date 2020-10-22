/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.GETTING_OLDER
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.GOOD
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.NEEDS_REPLACEMENT
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionData
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.DiscreteState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.PercentageState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.UnknownState
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionState
import com.kolibree.android.app.ui.toothbrushsettings.binding.ToothbrushSettingsItemBindingModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.offlinebrushings.sync.LastSyncData
import com.kolibree.android.offlinebrushings.sync.LastSyncDate
import com.kolibree.android.offlinebrushings.sync.NeverSync
import com.kolibree.android.offlinebrushings.sync.StartSync
import com.kolibree.android.sdk.version.BaseVersion
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level3Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.Level6Month
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewDays
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel.Discrete.LevelFewWeeks
import com.kolibree.android.toothbrushupdate.OtaUpdateType
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

@Parcelize
internal data class ToothbrushSettingsViewState(
    val toothbrushName: String = "",
    val connectionState: ConnectionState? = null,
    val lastSyncDate: LocalDate? = null,
    val mac: String,
    val model: String = NO_VALUE,
    val serial: String = NO_VALUE,
    val firmware: String = NO_VALUE,
    val hardware: String = NO_VALUE,
    val bootloader: String = NO_VALUE,
    val dsp: String = NO_VALUE,
    val batteryLevel: BatteryLevelState = UnknownState,
    val isIdentifying: Boolean = false,
    val brushHeadConditionState: BrushHeadConditionState? = null,
    val brushHeadReplacementDate: LocalDate? = null,
    val otaUpdateType: OtaUpdateType? = null,
    val hasDsp: Boolean = false
) : BaseViewState {

    @IgnoredOnParcel
    val isActive = connectionState == ConnectionState.CONNECTED

    fun items(): List<ToothbrushSettingsItemBindingModel> =
        ToothbrushSettingsItemsBuilder.build(this)

    fun isIdentifyPossible(): Boolean = isActive && !isIdentifying

    fun isOptionalOtaAvailable(): Boolean = otaUpdateType == OtaUpdateType.STANDARD && isActive

    fun isMandatoryOtaAvailable(): Boolean =
        (otaUpdateType == OtaUpdateType.MANDATORY_NEEDS_INTERNET || otaUpdateType == OtaUpdateType.MANDATORY) &&
            isActive

    fun withLastSyncData(data: LastSyncData): ToothbrushSettingsViewState {
        val date: LocalDate? = when (data) {
            is NeverSync -> null
            is StartSync -> TrustedClock.getNowLocalDate()
            is LastSyncDate -> data.date.toLocalDate()
        }
        return copy(lastSyncDate = date)
    }

    fun withBootloaderVersion(bootloaderVersion: SoftwareVersion): ToothbrushSettingsViewState =
        copy(bootloader = format(bootloaderVersion))

    fun withFirmwareVersion(firmwareVersion: SoftwareVersion): ToothbrushSettingsViewState =
        copy(firmware = format(firmwareVersion))

    fun withDspVersion(dspVersion: DspVersion): ToothbrushSettingsViewState =
        copy(dsp = format(dspVersion))

    fun withBatteryLevel(batteryLevel: BatteryLevel): ToothbrushSettingsViewState {
        val batteryLevelState = when (batteryLevel) {
            is BatteryLevel.LevelDiscrete -> fromDiscrete(batteryLevel.discrete)
            is BatteryLevel.LevelPercentage -> fromPercentage(batteryLevel.value)
            is BatteryLevel.LevelUnknown -> UnknownState
        }

        return copy(batteryLevel = batteryLevelState)
    }

    fun withBrushHeadConditionData(headConditionData: BrushHeadConditionData) = copy(
        brushHeadConditionState = fromBrushHeadCondition(headConditionData.condition),
        brushHeadReplacementDate = headConditionData.lastReplacementDate
    )

    private fun fromPercentage(percentage: Int) = PercentageState(
        text = "$percentage%",
        icon = batteryLevelIcon(percentage)
    )

    private fun fromDiscrete(discrete: Discrete): DiscreteState {

        @StringRes
        val textRes = when (discrete) {
            Level6Month -> R.string.tb_settings_battery_level_6_months
            Level3Month -> R.string.tb_settings_battery_level_3_months
            LevelFewWeeks -> R.string.tb_settings_battery_level_weeks
            LevelFewDays -> R.string.tb_settings_battery_level_days
        }

        @DrawableRes
        val icon = when (discrete) {
            Level6Month -> R.drawable.ic_battery_level_100
            Level3Month -> R.drawable.ic_battery_level_75
            LevelFewWeeks -> R.drawable.ic_battery_level_25
            LevelFewDays -> R.drawable.ic_battery_level_0
        }

        return DiscreteState(textRes, icon)
    }

    @DrawableRes
    private fun batteryLevelIcon(batteryLevel: Int): Int = when {
        batteryLevel <= BATTERY_LEVEL_EMPTY -> R.drawable.ic_battery_level_0
        batteryLevel <= BATTERY_LEVEL_ONE_BAR -> R.drawable.ic_battery_level_25
        batteryLevel <= BATTERY_LEVEL_ONE_TWO_BARS -> R.drawable.ic_battery_level_75
        else -> R.drawable.ic_battery_level_100
    }

    private fun fromBrushHeadCondition(condition: BrushHeadCondition) = when (condition) {
        GOOD -> BrushHeadConditionState.GOOD
        GETTING_OLDER -> BrushHeadConditionState.GETTING_OLDER
        NEEDS_REPLACEMENT -> BrushHeadConditionState.NEEDS_REPLACEMENT
    }

    private fun format(version: BaseVersion): String {
        return if (version == SoftwareVersion.NULL || version == DspVersion.NULL) {
            NO_VALUE
        } else {
            version.toString()
        }
    }

    companion object {
        fun initial(mac: String) = ToothbrushSettingsViewState(mac = mac)

        private const val BATTERY_LEVEL_EMPTY = 0
        private const val BATTERY_LEVEL_ONE_BAR = 25
        private const val BATTERY_LEVEL_ONE_TWO_BARS = 75
    }
}

internal const val NO_VALUE = "-"

internal enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED;

    fun isConnected(): Boolean = this == CONNECTED
    fun isConnecting(): Boolean = this == CONNECTING
}

sealed class BatteryLevelState : Parcelable {

    @Parcelize
    internal data class DiscreteState(
        @StringRes val textRes: Int,
        @DrawableRes val icon: Int
    ) : BatteryLevelState()

    @Parcelize
    internal data class PercentageState(
        val text: String,
        val icon: Int
    ) : BatteryLevelState()

    @Parcelize
    internal object UnknownState : BatteryLevelState()
}
