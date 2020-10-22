/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.app.base.BaseAction

internal sealed class ShopCartAction : BaseAction {
    object ProductRemovedByDecreasingQuantity : ShopCartAction()
    object ProductRemovedBySwipe : ShopCartAction()
    data class ShowProductRemovedSnackbar(
        val isEmptyCart: Boolean
    ) : ShopCartAction()

    object ScrollDownShowingRates : ShopCartAction()
}
