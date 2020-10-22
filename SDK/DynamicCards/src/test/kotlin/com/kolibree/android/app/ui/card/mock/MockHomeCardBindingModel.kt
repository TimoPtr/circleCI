/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardPosition
import kotlinx.android.parcel.Parcelize

internal abstract class MockHomeCardBindingModel(
    val data: MockHomeCardViewState,
    override val layoutId: Int = 0
) : DynamicCardBindingModel(data)

@Parcelize
internal class VisibleCardBindingModel(
    override val layoutId: Int = 1,
    private val cardPosition: DynamicCardPosition
) : MockHomeCardBindingModel(VisibleCardViewState(cardPosition), layoutId)

@Parcelize
internal class InvisibleCardBindingModel(
    override val layoutId: Int = 2,
    private val cardPosition: DynamicCardPosition
) : MockHomeCardBindingModel(InvisibleCardViewState(cardPosition), layoutId)

@Parcelize
internal class ToggleableCardBindingModel(
    private val isVisible: Boolean,
    override val layoutId: Int = 3,
    private val cardPosition: DynamicCardPosition
) : MockHomeCardBindingModel(ToggleableCardViewState(isVisible, cardPosition), layoutId)
