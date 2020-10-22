/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.shop.domain.model.Price
import java.util.Currency
import java.util.Locale
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class RewardYourselfCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val userName: String,
    val userCredits: Price,
    val items: List<RewardYourselfItemBinding>
) : DynamicCardViewState {

    override fun asBindingModel() = RewardYourselfCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            RewardYourselfCardViewState(
                visible = false,
                position = position,
                userName = "",
                userCredits = Price.create(0, Currency.getInstance(Locale.getDefault())),
                items = emptyList()
            )
    }
}
