/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.dynamiccards.BR
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel

/**
 * A mediator object between view state and the actual binding.
 *
 * Because [DynamicCardViewModel] of the card cannot contain live data of its own, this class
 * can hold binding methods (like string formatting, spans management etc.), according to needs.
 *
 * Every card needs to have its own [DynamicCardBindingModel] that is paired with [DynamicCardInteraction]
 * and returned from [DynamicCardViewModel]. This triad + layout composes the basic codebase of every
 * card.
 *
 * @see ItemBindingModel
 */
@VisibleForApp
abstract class DynamicCardBindingModel(
    private val data: DynamicCardViewState
) : ItemBindingModel, Parcelable {

    /**
     * Indicates if the card is visible to the user.
     * Can be constant or calculated from the state.
     */
    val visible = data.visible

    /**
     * ID of card's layout. Also acts as a unique ID of the card used by DiffUtils.
     */
    /* @LayoutRes */ abstract val layoutId: Int

    /**
     * Determines the initial order of cards on the view
     */
    val position: DynamicCardPosition = data.position

    final override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, layoutId)
    }
}
