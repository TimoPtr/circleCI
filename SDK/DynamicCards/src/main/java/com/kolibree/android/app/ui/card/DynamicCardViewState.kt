/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState

/**
 * Common interface for dynamic home card view states. Every card needs to hold
 * view state that conforms to this interface
 */
@VisibleForApp
interface DynamicCardViewState : BaseViewState {

    /**
     * Indicates if the card is visible to the user.
     * Can be constant or calculated from the state.
     */
    val visible: Boolean

    /**
     * Determines the initial order of cards on the view
     */
    val position: DynamicCardPosition

    /**
     * Mapping function that transforms the instance of view state to instance
     * of [DynamicCardBindingModel], making the state displayable on the UI.
     *
     * Has to return instance of binding model class related to this type of view state.
     */
    fun asBindingModel(): DynamicCardBindingModel
}
