/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request

import com.shopify.buy3.Storefront

internal abstract class GraphClientPollingRequest<T : Any> : GraphClientRequest<T>() {

    protected abstract val isPollingFinished: (Storefront.QueryRoot) -> Boolean

    fun retryCondition(): (Storefront.QueryRoot) -> Boolean = isPollingFinished
}
