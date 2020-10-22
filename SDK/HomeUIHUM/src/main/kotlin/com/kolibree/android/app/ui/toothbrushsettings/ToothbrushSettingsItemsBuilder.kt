/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import com.kolibree.android.app.ui.toothbrushsettings.binding.BatteryLevelBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BottomButtonsBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushConditionHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNameItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNicknameAndUserHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.IdentifyBrushBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.ToothbrushSettingsItemBindingModel
import com.kolibree.android.homeui.hum.R

internal object ToothbrushSettingsItemsBuilder {
    fun build(viewState: ToothbrushSettingsViewState): List<ToothbrushSettingsItemBindingModel> {
        val items: List<ToothbrushSettingsItemBindingModel> = mutableListOf(
            createBrushHeaderItem(viewState),
            BrushConditionHeaderBindingModel,
            BatteryLevelBindingModel(viewState.batteryLevel),
            toothbrushHeadConditionItem(viewState),
            BrushNicknameAndUserHeaderBindingModel,
            toothbrushNameItem(viewState),
            IdentifyBrushBindingModel(viewState.isIdentifyPossible()),
            BrushDetailsHeaderBindingModel
        ).apply {
            addToothbrushInfo(viewState)
            add(BottomButtonsBindingModel)
        }

        return items
    }

    private fun MutableList<ToothbrushSettingsItemBindingModel>.addToothbrushInfo(
        viewState: ToothbrushSettingsViewState
    ) {
        add(toothbrushModelDetailsItem(viewState.model))
        add(serialNumberDetailsItem(viewState.serial))
        add(macAddressDetailsItem(viewState.mac))
        add(firmwareVersionDetailsItem(viewState.firmware))
        add(hardwareVersionDetailsItem(viewState.hardware))
        add(bootloaderVersionDetailsItem(viewState.bootloader))

        if (viewState.hasDsp) {
            add(dspVersionDetailsItem(viewState.dsp))
        }
    }

    private fun createBrushHeaderItem(viewState: ToothbrushSettingsViewState) =
        BrushHeaderBindingModel(
            name = viewState.toothbrushName,
            lastSyncDate = viewState.lastSyncDate,
            connectionState = viewState.connectionState,
            optionalOtaAvailable = viewState.isOptionalOtaAvailable(),
            mandatoryOtaAvailable = viewState.isMandatoryOtaAvailable()
        )

    private fun bootloaderVersionDetailsItem(bootloader: String) = BrushDetailsItemBindingModel(
        title = R.string.tb_settings_brush_details_bootloader,
        value = bootloader.orNoValue()
    )

    private fun dspVersionDetailsItem(dsp: String) = BrushDetailsItemBindingModel(
        title = R.string.tb_settings_brush_details_dsp,
        value = dsp.orNoValue()
    )

    private fun hardwareVersionDetailsItem(hardware: String): ToothbrushSettingsItemBindingModel =
        BrushDetailsItemBindingModel(
            title = R.string.tb_settings_brush_details_hardware,
            value = hardware.orNoValue()
        )

    private fun firmwareVersionDetailsItem(firmware: String): ToothbrushSettingsItemBindingModel =
        BrushDetailsItemBindingModel(
            title = R.string.tb_settings_brush_details_firmware,
            value = firmware.orNoValue()
        )

    private fun macAddressDetailsItem(mac: String): ToothbrushSettingsItemBindingModel =
        BrushDetailsItemBindingModel(
            title = R.string.tb_settings_brush_details_mac,
            value = mac.orNoValue()
        )

    private fun serialNumberDetailsItem(serial: String): ToothbrushSettingsItemBindingModel =
        BrushDetailsItemBindingModel(
            title = R.string.tb_settings_brush_details_serial,
            value = serial.orNoValue()
        )

    private fun toothbrushModelDetailsItem(model: String): ToothbrushSettingsItemBindingModel =
        BrushDetailsItemBindingModel(
            title = R.string.tb_settings_brush_details_model,
            value = model.orNoValue()
        )

    private fun toothbrushHeadConditionItem(
        viewState: ToothbrushSettingsViewState
    ): ToothbrushSettingsItemBindingModel = BrushHeadConditionBindingModel(
        headCondition = viewState.brushHeadConditionState,
        lastReplacementDate = viewState.brushHeadReplacementDate
    )

    private fun toothbrushNameItem(viewState: ToothbrushSettingsViewState): ToothbrushSettingsItemBindingModel =
        BrushNameItemBindingModel(
            title = R.string.tb_settings_nickname_title,
            value = viewState.toothbrushName.orNoValue(),
            isClickable = viewState.isActive
        )
}

private fun String.orNoValue(): String = when {
    isEmpty() -> NO_VALUE
    else -> this
}
