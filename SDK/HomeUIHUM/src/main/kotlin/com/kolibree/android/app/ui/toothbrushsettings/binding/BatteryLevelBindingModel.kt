/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import android.content.Context
import androidx.annotation.DrawableRes
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.DiscreteState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.PercentageState
import com.kolibree.android.app.ui.toothbrushsettings.BatteryLevelState.UnknownState
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

@Parcelize
internal data class BatteryLevelBindingModel(
    private val batteryLevel: BatteryLevelState
) : ToothbrushSettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_battery_level_item)
    }

    @DrawableRes
    fun batteryLevelIcon(): Int = when (batteryLevel) {
        is DiscreteState -> batteryLevel.icon
        is PercentageState -> batteryLevel.icon
        is UnknownState -> R.drawable.ic_battery_level_0
    }

    fun batteryLevel(context: Context): String = when (batteryLevel) {
        is DiscreteState -> context.getString(batteryLevel.textRes)
        is PercentageState -> batteryLevel.text
        is UnknownState -> context.getString(R.string.value_not_available)
    }
}
