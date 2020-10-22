/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.card.DynamicCardViewState
import kotlinx.android.parcel.Parcelize

internal abstract class MockHomeCardViewState(
    override val visible: Boolean
) : DynamicCardViewState

@Parcelize
internal class VisibleCardViewState(
    override val position: DynamicCardPosition = DynamicCardPosition.ZERO
) : MockHomeCardViewState(true) {

    override fun asBindingModel() = VisibleCardBindingModel(cardPosition = position)
}

@Parcelize
internal class InvisibleCardViewState(
    override val position: DynamicCardPosition = DynamicCardPosition.ZERO
) : MockHomeCardViewState(false) {

    override fun asBindingModel() = InvisibleCardBindingModel(cardPosition = position)
}

@Parcelize
internal class ToggleableCardViewState(
    override val visible: Boolean,
    override val position: DynamicCardPosition = DynamicCardPosition.ZERO
) : MockHomeCardViewState(visible) {

    override fun asBindingModel() = ToggleableCardBindingModel(
        visible,
        cardPosition = position
    )
}
