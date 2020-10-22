/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.support.oralcare

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class OralCareSupportCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition
) : DynamicCardViewState {

    override fun asBindingModel(): DynamicCardBindingModel = OralCareSupportCardBindingModel(this)

    companion object {
        fun initial(position: DynamicCardPosition) =
            OralCareSupportCardViewState(visible = true, position = position)
    }
}
