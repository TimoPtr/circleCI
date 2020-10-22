/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.container

import com.kolibree.android.app.ui.viewpager.Tab
import com.kolibree.android.shop.R
import com.kolibree.android.shop.presentation.deals.ShopBrandDealsFragment
import com.kolibree.android.shop.presentation.list.ShopProductListFragment

internal object ShopTabs {

    val PRODUCTS = Tab(
        id = 0,
        isDefault = true,
        titleRes = R.string.shop_tab_products
    ) { ShopProductListFragment() }

    val BRAND_DEALS = Tab(
        id = 1,
        titleRes = R.string.shop_tab_brand_deals
    ) { ShopBrandDealsFragment() }
}
