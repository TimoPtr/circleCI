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
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions

/**
 * Common base class for dynamic home card view models. Every card needs to have
 * view model derived from this class.
 *
 * Card view models don't emit any actions and cannot have any live datas.
 * They communicate with their binding via [DynamicCardHostViewModel], which pumps their
 * view state updates to the layout.
 */
@VisibleForApp
abstract class DynamicCardViewModel<
    VS : DynamicCardViewState,
    I : DynamicCardInteraction,
    BM : DynamicCardBindingModel
    >(initialViewState: VS) : BaseViewModel<VS, NoActions>(initialViewState) {

    /**
     * Interaction associated with this view model.
     * If view model implements the [I] interface, it can pass `this` as a result.
     */
    abstract val interaction: I

    /**
     * Mapping function that transforms the current instance of view state to instance
     * of [DynamicCardBindingModel], making the state displayable on the UI.
     *
     * Has to return instance of binding model class related to this type of view state.
     */
    @Suppress("UNCHECKED_CAST")
    fun toBindingModel(): BM? = getViewState()?.asBindingModel() as? BM
}

typealias DynamicCardViewModelSet = Set<@JvmSuppressWildcards DynamicCardViewModel<*, *, *>>
