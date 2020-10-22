/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.binding

import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding

@Parcelize
internal object BottomButtonsBindingModel : ToothbrushSettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_tb_settings_bottom_item)
    }
}
