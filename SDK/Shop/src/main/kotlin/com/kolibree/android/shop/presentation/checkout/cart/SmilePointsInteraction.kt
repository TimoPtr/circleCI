/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.shop.presentation.list.ShopProductInteraction

internal interface ShopCartInteraction : SmilePointsInteraction, ShopProductInteraction,
    ShipmentEstimationInteraction

internal interface SmilePointsInteraction {
    fun onUseSmilesClick(shouldUseSmiles: Boolean)
}

internal interface ShipmentEstimationInteraction {
    fun onShipmentEstimationClick()
}
