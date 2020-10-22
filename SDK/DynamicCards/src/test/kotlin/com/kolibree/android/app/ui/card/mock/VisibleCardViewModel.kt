/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import com.kolibree.android.app.ui.card.DynamicCardInteraction
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.nhaarman.mockitokotlin2.mock

internal class VisibleCardViewModel(
    initialState: VisibleCardViewState = VisibleCardViewState()
) : DynamicCardViewModel<
    VisibleCardViewState,
    DynamicCardInteraction,
    VisibleCardBindingModel>(initialState) {

    override val interaction: DynamicCardInteraction
        get() = mock()
}

internal class InvisibleCardViewModel(
    initialState: InvisibleCardViewState = InvisibleCardViewState()
) : DynamicCardViewModel<
    InvisibleCardViewState,
    DynamicCardInteraction,
    InvisibleCardBindingModel>(initialState) {

    override val interaction: DynamicCardInteraction
        get() = mock()
}

internal class ToggleableCardViewModel(
    visible: Boolean,
    initialState: ToggleableCardViewState = ToggleableCardViewState(visible)
) : DynamicCardViewModel<
    ToggleableCardViewState,
    DynamicCardInteraction,
    ToggleableCardBindingModel>(initialState) {

    override val interaction: DynamicCardInteraction
        get() = mock()
}
