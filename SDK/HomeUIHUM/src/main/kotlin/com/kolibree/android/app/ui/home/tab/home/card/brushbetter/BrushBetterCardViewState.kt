/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushbetter

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class BrushBetterCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val items: List<BrushBetterItemBinding>,
    val pulsingDotVisible: Boolean
) : DynamicCardViewState {

    override fun asBindingModel() = BrushBetterCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            BrushBetterCardViewState(
                visible = true,
                position = position,
                items = emptyList(),
                pulsingDotVisible = false
            )
    }
}
