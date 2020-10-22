/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.homeui.hum.BR
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapters
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel

@BindingAdapter(value = ["items", "interaction"])
internal fun <MODEL : ItemBindingModel, INTERACTION> RecyclerView.setItems(
    items: List<MODEL>,
    interaction: INTERACTION
) {

    val itemsBinding = object : OnItemBindModel<MODEL>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: MODEL?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, interaction)
        }
    }

    BindingRecyclerViewAdapters.setAdapter(
        this,
        ItemBinding.of(itemsBinding),
        items,
        null,
        null,
        null,
        null
    )
}
