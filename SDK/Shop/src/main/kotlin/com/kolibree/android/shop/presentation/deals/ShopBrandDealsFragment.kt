/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.deals

import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.shop.R
import com.kolibree.android.shop.TAB_BRAND_DEALS
import com.kolibree.android.shop.databinding.FragmentBrandDealsBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class ShopBrandDealsFragment : BaseMVIFragment<
    ShopBrandDealsViewState,
    ShopBrandDealsActions,
    ShopBrandDealsViewModel.Factory,
    ShopBrandDealsViewModel,
    FragmentBrandDealsBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<ShopBrandDealsViewModel> =
        ShopBrandDealsViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_brand_deals

    override fun execute(action: ShopBrandDealsActions) {
        // no-op for now
    }

    override fun getScreenName(): AnalyticsEvent = TAB_BRAND_DEALS
}
