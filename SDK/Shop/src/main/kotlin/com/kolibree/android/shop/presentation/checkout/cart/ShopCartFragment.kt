/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.widget.snackbar.snackbar
import com.kolibree.android.shop.CART_ACTIVITY
import com.kolibree.android.shop.R
import com.kolibree.android.shop.databinding.FragmentShopCartBinding
import com.kolibree.android.shop.presentation.checkout.cart.view.SwipeToRemoveBackgroundItemDecoration
import com.kolibree.android.shop.presentation.checkout.cart.view.SwipeToRemoveCartItemCallback
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen
import timber.log.Timber

internal class ShopCartFragment :
    BaseMVIFragment<
        ShopCartViewState,
        ShopCartAction,
        ShopCartViewModel.Factory,
        ShopCartViewModel,
        FragmentShopCartBinding>(),
    TrackableScreen {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemAnimator = binding.productList.itemAnimator as? DefaultItemAnimator
        itemAnimator?.supportsChangeAnimations = false
        setupSwipeToRemove(binding.productList)
    }

    override fun getViewModelClass(): Class<ShopCartViewModel> = ShopCartViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_shop_cart

    override fun execute(action: ShopCartAction) = when (action) {
        is ShopCartAction.ProductRemovedBySwipe -> enableSwipeToRemoveBackground()
        is ShopCartAction.ProductRemovedByDecreasingQuantity -> disableSwipeToRemoveBackground()
        is ShopCartAction.ShowProductRemovedSnackbar -> showUndoSnackbar(action.isEmptyCart)
        is ShopCartAction.ScrollDownShowingRates -> scrollToRates()
    }

    @SuppressLint("WrongConstant")
    private fun showUndoSnackbar(isEmptyCart: Boolean) {
        snackbar(binding.productList) {
            duration(SNACKBAR_DURATION)
            message(R.string.cart_undo_title)
            icon(R.drawable.ic_shop_trash_small)
            action(R.string.cart_undo_action_title) {
                disableSwipeToRemoveBackground()
                viewModel.onUndoClick()
            }
            val anchor = if (isEmptyCart) binding.cartVisitShop else binding.cartPayment
            anchor(anchor)
        }.show()
    }

    private fun disableSwipeToRemoveBackground() = setSwipeToRemoveBackgroundEnabled(false)

    private fun enableSwipeToRemoveBackground() = setSwipeToRemoveBackgroundEnabled(true)

    private fun setSwipeToRemoveBackgroundEnabled(enabled: Boolean) {
        val size = binding.productList.itemDecorationCount
        for (index in 0 until size) {
            val itemDecoration = binding.productList.getItemDecorationAt(index)
            if (itemDecoration is SwipeToRemoveBackgroundItemDecoration) {
                itemDecoration.isEnabled = enabled
                break
            }
        }
    }

    private fun scrollToRates() {
        binding.productList.adapter?.let {
            binding.productList.smoothScrollToPosition(it.itemCount - 1)
        }
    }

    private fun setupSwipeToRemove(recyclerView: RecyclerView) {
        val swipeToRemove = SwipeToRemoveCartItemCallback(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeToRemove)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(SwipeToRemoveBackgroundItemDecoration(requireContext()))

        disposeOnDestroy {
            swipeToRemove.positionSwipedObservable()
                .subscribe(viewModel::removeAllProductsOnPosition, Timber::e)
        }
    }

    override fun getScreenName(): AnalyticsEvent = CART_ACTIVITY
}

private const val SNACKBAR_DURATION = 10_000
