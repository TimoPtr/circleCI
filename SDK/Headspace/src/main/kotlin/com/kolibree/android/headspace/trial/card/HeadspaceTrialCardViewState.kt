/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class HeadspaceTrialCardViewState(
    override val position: DynamicCardPosition,
    override val visible: Boolean,
    override val isDescriptionVisible: Boolean = false,
    override val progress: Int? = null,
    override val copiedToClipboard: Boolean = false,
    override val isUnlocked: Boolean = false,
    val pointsNeeded: Int? = null,
    val redeemUrl: String? = null,
    val discountCode: String? = null
) : DynamicCardViewState, HeadSpaceTrialState {

    override fun asBindingModel(): DynamicCardBindingModel {
        return HeadspaceTrialCardBindingModel(this)
    }

    fun withInactiveStatus(status: Inactive): HeadspaceTrialCardViewState {
        return copy(visible = false)
    }

    fun withInProgressStatus(status: InProgress): HeadspaceTrialCardViewState {
        return copy(
            visible = true,
            isUnlocked = false,
            pointsNeeded = status.pointsNeeded,
            progress = status.progress
        )
    }

    fun withUnlockedStatus(status: Unlocked): HeadspaceTrialCardViewState {
        return copy(
            visible = true,
            isUnlocked = true,
            pointsNeeded = 0,
            redeemUrl = status.redeemUrl,
            discountCode = status.discountCode
        )
    }

    /**
     * @return true if the view must represent Unlockable state
     */
    @IgnoredOnParcel
    val isUnlockable: Boolean = pointsNeeded == 0 && !isUnlocked

    /**
     * @return true if the view must represent InProgress state
     */
    @IgnoredOnParcel
    override val isProgressVisible: Boolean = !isUnlockable && !isUnlocked

    @VisibleForApp
    companion object {
        fun initial(position: DynamicCardPosition) =
            HeadspaceTrialCardViewState(position = position, visible = false)
    }
}

internal interface HeadSpaceTrialState {
    val progress: Int?

    @IgnoredOnParcel
    val isProgressVisible: Boolean

    @IgnoredOnParcel
    val isDescriptionVisible: Boolean

    @IgnoredOnParcel
    val copiedToClipboard: Boolean

    @IgnoredOnParcel
    val isUnlocked: Boolean
}
