/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

internal abstract class HeaderBindingModel(
    @StringRes val title: Int
) : ToothbrushSettingsItemBindingModel {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_header)
    }
}

@Parcelize
internal object BrushDetailsHeaderBindingModel :
    HeaderBindingModel(R.string.tb_settings_brush_details_header)

@Parcelize
internal object BrushConditionHeaderBindingModel :
    HeaderBindingModel(R.string.tb_settings_brush_condition_header)

@Parcelize
internal object BrushNicknameAndUserHeaderBindingModel :
    HeaderBindingModel(R.string.tb_settings_nickname_and_user_header)
