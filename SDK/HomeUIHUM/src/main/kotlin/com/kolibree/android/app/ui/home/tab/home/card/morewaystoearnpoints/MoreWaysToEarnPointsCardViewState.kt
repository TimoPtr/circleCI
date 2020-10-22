/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.morewaystoearnpoints

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class MoreWaysToEarnPointsCardViewState(
    override val position: DynamicCardPosition,
    val cards: List<MoreWaysToEarnPointsCardItemBindingModel>
) : DynamicCardViewState {

    @IgnoredOnParcel
    override val visible: Boolean = cards.isNotEmpty()

    override fun asBindingModel(): DynamicCardBindingModel {
        return MoreWaysToEarnPointsCardBindingModel(this)
    }

    companion object {
        fun initial(
            position: DynamicCardPosition,
            cards: List<MoreWaysToEarnPointsCardItemBindingModel> = emptyList()
        ) = MoreWaysToEarnPointsCardViewState(position = position, cards = cards)
    }
}
