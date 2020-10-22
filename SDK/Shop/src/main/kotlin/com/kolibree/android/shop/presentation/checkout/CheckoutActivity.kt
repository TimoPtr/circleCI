/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.activity.getCurrentNavFragment
import com.kolibree.android.shop.R
import com.kolibree.android.shop.data.googlewallet.GooglePayClientWrapper
import com.kolibree.android.shop.databinding.ActivityCheckoutBinding
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.presentation.checkout.cart.ShopCartFragment
import com.kolibree.android.shop.presentation.checkout.shipping.ShippingBillingFragment
import javax.inject.Inject

internal class CheckoutActivity : BaseMVIActivity<
    CheckoutActivityViewState,
    CheckoutActivityAction,
    CheckoutActivityViewModel.Factory,
    CheckoutActivityViewModel,
    ActivityCheckoutBinding>() {

    @Inject
    lateinit var googlePayClientWrapper: GooglePayClientWrapper

    override fun getViewModelClass(): Class<CheckoutActivityViewModel> =
        CheckoutActivityViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_checkout

    override fun execute(action: CheckoutActivityAction) {
        when (action) {
            is ToolbarIconClickAction -> onToolbarClick()
        }
    }

    private fun onToolbarClick() {
        when (getCurrentNavFragment(R.id.nav_host_fragment)) {
            is ShopCartFragment -> viewModel.onToolbarIconClickOnCart()
            is ShippingBillingFragment -> viewModel.onToolbarClickOnBilling()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        googlePayClientWrapper.maybeProcessActivityResult(requestCode, resultCode, data)
    }

    fun updateToolbarName(label: CharSequence?) {
        binding.toolbarName.text = label
    }

    fun updateToolbarIcon(@DrawableRes iconRes: Int) {
        binding.toolbarIcon.setImageResource(iconRes)
    }
}

@Keep
fun startCheckoutActivity(activity: AppCompatActivity) {
    val checkout = Intent(activity, CheckoutActivity::class.java)

    activity.startActivityForResult(checkout, CHECKOUT_REQUEST_CODE)
}

@Keep
fun isCheckoutFlow(requestCode: Int) = requestCode == CHECKOUT_REQUEST_CODE

@Keep
fun extractCheckout(data: Intent?): WebViewCheckout? {
    return data?.getParcelableExtra(EXTRA_CHECKOUT_RESULT)
}

@Keep
fun createCheckoutResult(webViewCheckout: WebViewCheckout): Intent {
    return Intent().apply {
        this.putExtra(EXTRA_CHECKOUT_RESULT, webViewCheckout)
    }
}

@Keep
fun isCheckoutProcessing(resultCode: Int) = resultCode == Activity.RESULT_OK

@Keep
fun isVisitShop(resultCode: Int) = resultCode == VISIT_SHOP_RESULT

internal const val VISIT_SHOP_RESULT = 123
private const val CHECKOUT_REQUEST_CODE = 7389
private const val EXTRA_CHECKOUT_RESULT = "EXTRA_CHECKOUT_RESULT"
