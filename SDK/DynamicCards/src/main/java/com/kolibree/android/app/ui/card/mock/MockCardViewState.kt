/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@VisibleForApp
@Parcelize
data class MockCardViewState(
    val staticValue: String = "Hey, this is me!"
) : DynamicCardViewState {

    @IgnoredOnParcel
    override val visible = true

    @IgnoredOnParcel
    override val position = DynamicCardPosition.TEN

    override fun asBindingModel() =
        MockCardBindingModel(this)

    @VisibleForApp
    companion object {

        fun initial() = MockCardViewState()
    }
}
