/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query.error

import androidx.annotation.Keep
import com.shopify.buy3.Storefront

@Keep
class ShopifyCheckoutUserError(val errors: List<Storefront.CheckoutUserError>) : RuntimeException() {
    override fun toString(): String = errors.joinToString(" ; ") { "message = ${it.message}" }
}
