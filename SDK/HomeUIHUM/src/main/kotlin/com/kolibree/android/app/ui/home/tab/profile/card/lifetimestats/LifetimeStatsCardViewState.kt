/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class LifetimeStatsCardViewState(
    override val visible: Boolean = true,
    override val position: DynamicCardPosition,
    val isLoading: Boolean,
    val currentPoints: Int = 0,
    val lifetimePoints: Int = 0,
    val offlineCount: Int = 0,
    val inAppCount: Int = 0
) : DynamicCardViewState {

    @IgnoredOnParcel
    val shouldDisplayKeepEarningPoints: Boolean by lazy { lifetimePoints == 0 || lifetimePoints == currentPoints }

    override fun asBindingModel() =
        LifetimeStatsCardBindingModel(
            this
        )

    companion object {
        fun initial(position: DynamicCardPosition) = LifetimeStatsCardViewState(
            position = position,
            isLoading = true
        )
    }
}
