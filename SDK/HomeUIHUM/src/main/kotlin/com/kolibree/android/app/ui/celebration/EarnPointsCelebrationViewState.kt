/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.celebration

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.rewards.morewaystoearnpoints.model.CompleteEarnPointsChallenge
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class EarnPointsCelebrationViewState(
    val items: List<EarnPointsChallenge>,
    val selectedIndex: Int
) : BaseViewState {

    companion object {
        fun from(challenges: List<CompleteEarnPointsChallenge>) = EarnPointsCelebrationViewState(
            items = challenges.map { it.challenge },
            selectedIndex = 0
        )
    }
}
