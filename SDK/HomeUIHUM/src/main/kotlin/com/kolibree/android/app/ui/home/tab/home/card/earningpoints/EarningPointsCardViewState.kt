/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class EarningPointsCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val pointsPerBrush: Int,
    val expanded: Boolean
) : DynamicCardViewState {

    override fun asBindingModel() = EarningPointsCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            EarningPointsCardViewState(
                visible = true,
                position = position,
                pointsPerBrush = 1,
                expanded = false
            )
    }
}
