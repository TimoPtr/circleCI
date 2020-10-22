/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import javax.inject.Inject
import timber.log.Timber

@SuppressLint("DeobfuscatedPublicSdkClass")
class MockCardViewModel(
    initialViewState: MockCardViewState
) : DynamicCardViewModel<
    MockCardViewState,
    MockCardInteraction,
    MockCardBindingModel>(initialViewState),
    MockCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is MockCardBindingModel

    override fun onCardClick() {
        Timber.d("Mock card was clicked!")
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<MockCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MockCardViewModel(
            viewState ?: MockCardViewState.initial()
        ) as T
    }
}
