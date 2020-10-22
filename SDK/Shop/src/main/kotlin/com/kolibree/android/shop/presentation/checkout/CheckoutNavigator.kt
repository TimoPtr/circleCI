/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController.OnDestinationChangedListener
import androidx.navigation.Navigation.findNavController
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.utils.navigateSafe
import com.kolibree.android.shop.R
import com.kolibree.android.shop.domain.model.WebViewCheckout

internal class CheckoutNavigator : BaseNavigator<CheckoutActivity>() {

    private val listener: OnDestinationChangedListener by lazy {
        getOnDestinationChangedListener()
    }

    /**
     * Set up a navigation listener to change the Toolbar title & icon accordingly with the user navigation.
     * It allow support of back pressed navigation
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onCreate(owner)
        withOwner { navController().addOnDestinationChangedListener(listener) }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        withOwner { navController().removeOnDestinationChangedListener(listener) }
    }

    fun showAnotherPaymentScreen(webViewCheckout: WebViewCheckout) {
        withOwner {
            navController().navigateSafe(
                R.id.action_cart_navigation_to_another_payment_navigation,
                webViewCheckout.asArguments()
            )
        }
    }

    fun showOurShop() {
        withOwner {
            setResult(VISIT_SHOP_RESULT)
            finish()
        }
    }

    fun showShippingAndBilling() {
        withOwner {
            navController().navigateSafe(R.id.action_cart_navigation_to_shipping_and_billing)
        }
    }

    fun navigateBack() {
        withOwner {
            if (!navController().popBackStack()) {
                finish()
            }
        }
    }

    private fun getOnDestinationChangedListener(): OnDestinationChangedListener {
        return OnDestinationChangedListener { _, destination, _ ->
            withOwner {
                updateToolbarName(destination.label)
                updateToolbarIcon(getIconResource(destination.id))
            }
        }
    }

    @DrawableRes
    private fun getIconResource(destinationId: Int): Int {
        return when (destinationId) {
            R.id.shipping_billing_navigation -> R.drawable.ic_back_arrow
            else -> R.drawable.ic_leading_icon
        }
    }

    private fun CheckoutActivity.navController() =
        findNavController(this, R.id.nav_host_fragment)
}
