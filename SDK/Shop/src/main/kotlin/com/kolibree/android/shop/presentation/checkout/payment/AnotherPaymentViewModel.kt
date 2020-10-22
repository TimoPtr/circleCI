/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.checkout.payment.view.AnotherPaymentListener
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import javax.inject.Inject

internal class AnotherPaymentViewModel(
    initialViewState: AnotherPaymentViewState?,
    private val sharedViewModel: CheckoutSharedViewModel,
    private val webViewCheckout: WebViewCheckout
) : BaseViewModel<AnotherPaymentViewState, AnotherPaymentAction>(
    initialViewState ?: AnotherPaymentViewState.initial()
), AnotherPaymentListener, CheckoutSharedViewModel by sharedViewModel {

    val paymentUrl: LiveData<String> = map(viewStateLiveData) { viewState ->
        webViewCheckout.webUrl
    }.distinctUntilChanged()

    override fun onViewPrepared() {
        showProgress(false)
        pushAction(AnotherPaymentAction.SetPaymentProcessingResult(webViewCheckout))
    }

    class Factory @Inject constructor(
        private val sharedViewModel: CheckoutSharedViewModel,
        private val webViewCheckout: WebViewCheckout
    ) : BaseViewModel.Factory<AnotherPaymentViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AnotherPaymentViewModel(
                viewState,
                sharedViewModel,
                webViewCheckout
            ) as T
    }
}
