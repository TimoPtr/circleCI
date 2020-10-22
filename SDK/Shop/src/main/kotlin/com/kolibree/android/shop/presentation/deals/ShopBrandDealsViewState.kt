/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.deals

import com.kolibree.android.app.base.BaseViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class ShopBrandDealsViewState(
    val buttonText: String = "BrandDeals"
) : BaseViewState {

    companion object {
        fun initial() = ShopBrandDealsViewState()
    }
}
