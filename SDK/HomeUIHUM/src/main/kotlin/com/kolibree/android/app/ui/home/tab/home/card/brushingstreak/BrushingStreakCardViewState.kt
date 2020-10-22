/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class BrushingStreakCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition,
    val isExpanded: Boolean,
    val challenge: HumChallenge?
) : DynamicCardViewState {

    override fun asBindingModel() = BrushingStreakCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            BrushingStreakCardViewState(
                visible = true,
                position = position,
                isExpanded = false,
                challenge = null
            )
    }

    fun withChallenge(challenge: HumChallenge): BrushingStreakCardViewState = copy(
        visible = true,
        challenge = challenge,
        isExpanded = challenge !is NotAcceptedChallenge
    )
}
