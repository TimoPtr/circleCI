/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.container

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.shop.domain.model.StoreDetails
import kotlinx.android.parcel.Parcelize

@Parcelize
@VisibleForApp
data class ShopContainerViewState(
    val storeDetails: StoreDetails? = null,
    val currentSmilesCount: Int = 0,
    val discountBannerVisible: Boolean = true
) : BaseViewState {

    @VisibleForApp
    companion object {
        fun initial() = ShopContainerViewState()
    }
}
