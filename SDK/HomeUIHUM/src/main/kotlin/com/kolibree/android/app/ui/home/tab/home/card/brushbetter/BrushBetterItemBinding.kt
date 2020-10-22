/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import android.content.Context
import android.os.Parcelable
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

@Parcelize
internal data class BrushBetterItemBinding(
    val item: BrushBetterItem,
    val iconRes: Int,
    val titleRes: Int,
    val bodyRes: Int
) : ItemBindingModel, Parcelable {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.binding, R.layout.item_brush_better)
    }

    fun getPoints(context: Context): String {
        return context.getString(R.string.brush_better_card_item_points, item.points.toString())
    }
}
