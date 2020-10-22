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
import com.kolibree.android.app.ui.toothbrushsettings.binding.BatteryLevelBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BottomButtonsBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushConditionHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeadConditionState
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNameItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNicknameAndUserHeaderBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.IdentifyBrushBindingModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.homeui.hum.R
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.junit.Test

internal class ToothbrushSettingsItemsBuilderTest : BaseUnitTest() {

    @Test
    fun `appropriate items are provided with proper order`() {
        val mac = "12:23:45"
        val bootloader = "BL_v123"
        val firmware = "FM_v1"
        val hardware = "HW_v1"
        val serial = "34-4564-333"
        val model = "Super_HUM_V1"
        val dsp = "DSP_v1"
        val lastSyncDate = TrustedClock.getNowLocalDate()
        val name = "My TB"
        val batteryLevel = BatteryLevelState.PercentageState(
            text = "77%",
            icon = R.drawable.ic_battery_level_75
        )
        val isIdentifying = false
        val headCondition = BrushHeadConditionState.GETTING_OLDER
        val headReplacedDate = TrustedClock.getNowLocalDate().minusDays(10)
        val connectionState = ConnectionState.DISCONNECTED

        val viewState = ToothbrushSettingsViewState(
            mac = mac,
            bootloader = bootloader,
            hardware = hardware,
            firmware = firmware,
            serial = serial,
            model = model,
            lastSyncDate = lastSyncDate,
            toothbrushName = name,
            batteryLevel = batteryLevel,
            isIdentifying = isIdentifying,
            brushHeadReplacementDate = headReplacedDate,
            brushHeadConditionState = headCondition,
            connectionState = connectionState,
            hasDsp = true,
            dsp = dsp
        )
        val items = ToothbrushSettingsItemsBuilder.build(viewState)

        val expectedItems = listOf(
            BrushHeaderBindingModel(
                name,
                lastSyncDate,
                connectionState,
                optionalOtaAvailable = false,
                mandatoryOtaAvailable = false
            ),
            BrushConditionHeaderBindingModel,
            BatteryLevelBindingModel(batteryLevel),
            BrushHeadConditionBindingModel(headCondition, headReplacedDate),
            BrushNicknameAndUserHeaderBindingModel,
            BrushNameItemBindingModel(
                title = R.string.tb_settings_nickname_title,
                value = name,
                isClickable = false
            ),
            IdentifyBrushBindingModel(viewState.isIdentifyPossible()),
            BrushDetailsHeaderBindingModel,
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_model, model),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_serial, serial),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_mac, mac),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_firmware, firmware),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_hardware, hardware),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_bootloader, bootloader),
            BrushDetailsItemBindingModel(R.string.tb_settings_brush_details_dsp, dsp),
            BottomButtonsBindingModel
        )

        assertEquals(expectedItems.size, items.size)
        expectedItems.forEachIndexed { index, expectedItem ->
            assertEquals("Item are not the same at index $index", expectedItem, items[index])
        }
    }

    @Test
    fun `Dsp item is not shown when brush does not have a dsp`() {
        val viewState = ToothbrushSettingsViewState(
            mac = "",
            hasDsp = false
        )
        val items = ToothbrushSettingsItemsBuilder.build(viewState)

        items.filterIsInstance(BrushDetailsItemBindingModel::class.java).forEach {
            if (it.title == R.string.tb_settings_brush_details_dsp) {
                fail("items should not contains dsp entry since the brush does not support it")
            }
        }
    }
}
