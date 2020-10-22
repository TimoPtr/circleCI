/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities.card.games

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GamesCardViewState(
    val items: List<GamesCardItem>
) : BaseViewState {

    @IgnoredOnParcel
    val cardVisible = items.isNotEmpty()

    companion object {
        fun initial() = GamesCardViewState(emptyList())
    }
}
