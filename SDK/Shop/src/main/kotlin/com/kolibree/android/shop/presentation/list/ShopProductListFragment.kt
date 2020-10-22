/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.shop.R
import com.kolibree.android.shop.TAB_PRODUCTS
import com.kolibree.android.shop.databinding.FragmentShopProductListBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class ShopProductListFragment : BaseMVIFragment<
    ShopProductListViewState,
    ShopProductListAction,
    ShopProductListViewModel.Factory,
    ShopProductListViewModel,
    FragmentShopProductListBinding>(),
    TrackableScreen {

    override fun getViewModelClass(): Class<ShopProductListViewModel> =
        ShopProductListViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_shop_product_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemAnimator = binding.productList.itemAnimator as? DefaultItemAnimator
        itemAnimator?.supportsChangeAnimations = false
    }

    override fun execute(action: ShopProductListAction) {
        when (action) {
            is ShopProductListAction.ScrollToPosition -> {
                binding.productList.smoothScrollToPosition(action.position)
            }
        }
    }

    override fun getScreenName(): AnalyticsEvent = TAB_PRODUCTS
}
