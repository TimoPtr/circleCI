/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.googlewallet.GooglePayClientWrapper
import com.kolibree.android.shop.data.googlewallet.exceptions.GooglePayNotReadyException
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.kolibree.android.shop.domain.model.Price
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class GooglePayCheckoutUseCase @Inject constructor(
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val googlePayClientWrapper: GooglePayClientWrapper
) {

    fun onBuyWithGooglePayClick(checkout: Checkout, price: Price): Maybe<GooglePayCheckout> {
        return googlePayClientWrapper.isReadyToPayRequest()
            .subscribeOn(Schedulers.io())
            .flatMapMaybe { isReady -> maybeValidatePayment(isReady, price) }
            .flatMap { googlePayment -> googlePayCheckoutSingle(checkout, googlePayment) }
    }

    private fun googlePayCheckoutSingle(
        checkout: Checkout,
        googlePayment: GoogleWalletPayment
    ): Maybe<GooglePayCheckout> =
        shopifyClientWrapper.createGooglePayCheckout(checkout, googlePayment).toMaybe()

    private fun maybeValidatePayment(isReady: Boolean, price: Price): Maybe<GoogleWalletPayment> {
        return if (isReady) {
            googlePayClientWrapper.validatePayment(price)
        } else {
            Maybe.error(GooglePayNotReadyException)
        }
    }
}
