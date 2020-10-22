/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.ui.card

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus.Available
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentStatus.NotAvailable
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class HeadspaceMindfulMomentCardViewState(
    override val position: DynamicCardPosition,
    override val visible: Boolean,
    val mindfulMoment: HeadspaceMindfulMoment? = null
) : DynamicCardViewState {

    override fun asBindingModel() = HeadspaceMindfulMomentCardBindingModel(this)

    fun withAvailableStatus(status: Available) = copy(
        visible = true,
        mindfulMoment = status.headspaceMindfulMoment
    )

    fun withNotAvailableStatus(status: NotAvailable) = copy(visible = false)

    @VisibleForApp
    companion object {
        fun initial(position: DynamicCardPosition) = HeadspaceMindfulMomentCardViewState(
            position = position,
            visible = false
        )
    }
}
