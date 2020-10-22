/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.deals

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject

internal class ShopBrandDealsViewModel(
    initialViewStateShop: ShopBrandDealsViewState?
) : BaseViewModel<ShopBrandDealsViewState, ShopBrandDealsActions>(
    initialViewStateShop ?: ShopBrandDealsViewState.initial()
) {

    val buttonText: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.buttonText ?: ""
    }

    fun dummyClick() {
        // no-op
    }

    class Factory @Inject constructor() : BaseViewModel.Factory<ShopBrandDealsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShopBrandDealsViewModel(viewState) as T
    }
}
