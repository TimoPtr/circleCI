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

internal abstract class BrushDetailsClickableItemBindingModel(
    @StringRes open val title: Int,
    open val value: String,
    open val isClickable: Boolean
) : ToothbrushSettingsItemBindingModel {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_brush_clickable_details_item)
    }
}

@Parcelize
internal data class BrushNameItemBindingModel(
    @StringRes override val title: Int,
    override val value: String,
    override val isClickable: Boolean
) : BrushDetailsClickableItemBindingModel(title, value, isClickable)
