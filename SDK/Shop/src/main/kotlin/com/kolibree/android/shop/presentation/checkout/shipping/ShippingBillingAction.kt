/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.shop.domain.model.Address.Input

internal sealed class ShippingBillingAction : BaseAction {
    class ScrollToError(val error: Pair<AddressType, Input>) : ShippingBillingAction()
}
