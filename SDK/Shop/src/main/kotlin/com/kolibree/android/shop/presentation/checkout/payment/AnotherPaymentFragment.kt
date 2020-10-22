/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.payment

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.dialog.alertDialog
import com.kolibree.android.shop.R
import com.kolibree.android.shop.databinding.FragmentAnotherPaymentBinding
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.presentation.checkout.createCheckoutResult

internal class AnotherPaymentFragment :
    BaseMVIFragment<
        AnotherPaymentViewState,
        AnotherPaymentAction,
        AnotherPaymentViewModel.Factory,
        AnotherPaymentViewModel,
        FragmentAnotherPaymentBinding>() {

    private fun setCheckoutProcessingResult(checkout: WebViewCheckout) {
        activity?.setResult(Activity.RESULT_OK, createCheckoutResult(checkout))
    }

    fun extraWebViewCheckout(): WebViewCheckout {
        return WebViewCheckout.extractArguments(arguments)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun getViewModelClass(): Class<AnotherPaymentViewModel> =
        AnotherPaymentViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_another_payment

    override fun execute(action: AnotherPaymentAction) {
        when (action) {
            is AnotherPaymentAction.SetPaymentProcessingResult ->
                setCheckoutProcessingResult(action.checkout)
        }
    }

    val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            showBackToPreviousPageDialog()
        }
    }

    @Suppress("LongMethod")
    private fun showBackToPreviousPageDialog() {
        activity?.let {
            alertDialog(it) {
                title(R.string.shop_web_payment_dialog_title)
                body(R.string.shop_web_payment_dialog_content)
                containedButton {
                    title(R.string.shop_web_payment_dialog_positive)
                    action {
                        onBackPressedCallback.isEnabled = false
                        requireActivity().onBackPressed()
                        dismiss()
                    }
                }
                textButton {
                    title(R.string.shop_web_payment_dialog_negative)
                    action { dismiss() }
                }
            }.show()
        }
    }
}
