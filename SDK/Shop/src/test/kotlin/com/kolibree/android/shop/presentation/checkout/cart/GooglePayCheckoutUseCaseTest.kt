/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.googlewallet.GooglePayClientWrapper
import com.kolibree.android.shop.data.googlewallet.exceptions.GooglePayNotReadyException
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.GoogleWalletPayment
import com.kolibree.android.shop.googlePayCheckout
import com.kolibree.android.shop.googleWalletPayment
import com.kolibree.android.shop.price
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.MaybeSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GooglePayCheckoutUseCaseTest : BaseUnitTest() {
    private val shopifyClientWrapper: ShopifyClientWrapper = mock()
    private val googlePayClientWrapper: GooglePayClientWrapper = mock()

    private val googlePayCheckoutUseCase = GooglePayCheckoutUseCase(
        shopifyClientWrapper,
        googlePayClientWrapper
    )

    /*
    onBuyWithGooglePayClick
     */

    @Test
    fun `onBuyWithGooglePayClick subscribes to validatePayment after isReadyToPayRequest emits true`() {
        val checkout = BasicCheckout("checkoutId", Cart())
        val price = price()

        val isReadySubject = SingleSubject.create<Boolean>()
        whenever(googlePayClientWrapper.isReadyToPayRequest()).thenReturn(isReadySubject)

        val validatePaymentSubject = MaybeSubject.create<GoogleWalletPayment>()
        whenever(googlePayClientWrapper.validatePayment(price)).thenReturn(
            validatePaymentSubject
        )

        googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price).test()

        assertFalse(validatePaymentSubject.hasObservers())
        isReadySubject.onSuccess(true)
        assertTrue(validatePaymentSubject.hasObservers())
    }

    @Test
    fun `onBuyWithGooglePayClick emits GooglePayNotReadyException without validating payment after isReadyToPayRequest emits false`() {
        val checkout = BasicCheckout("checkoutId", Cart())
        whenever(googlePayClientWrapper.isReadyToPayRequest()).thenReturn(Single.just(false))

        googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price()).test()
            .assertError(GooglePayNotReadyException::class.java)

        verify(googlePayClientWrapper, never()).validatePayment(any())
    }

    @Test
    fun `onBuyWithGooglePayClick doesn't proceed to Google Pay Checkout after validatePayment emits completed`() {
        val checkout = BasicCheckout("checkoutId", Cart())
        val price = price()

        whenever(googlePayClientWrapper.isReadyToPayRequest()).thenReturn(Single.just(true))
        whenever(googlePayClientWrapper.validatePayment(price)).thenReturn(Maybe.empty())

        googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price).test().assertComplete()

        verify(shopifyClientWrapper, never()).createGooglePayCheckout(any(), any())
    }

    @Test
    fun `onBuyWithGooglePayClick proceeds to Google Pay Checkout after validatePayment emits googleWalletPayment`() {
        val checkout = BasicCheckout("checkoutId", Cart())
        val price = price()

        whenever(googlePayClientWrapper.isReadyToPayRequest()).thenReturn(Single.just(true))

        val googleWalletPayment = googleWalletPayment()
        whenever(googlePayClientWrapper.validatePayment(price))
            .thenReturn(Maybe.just(googleWalletPayment))

        val createGooglePayCheckoutSubject = SingleSubject.create<GooglePayCheckout>()
        whenever(shopifyClientWrapper.createGooglePayCheckout(checkout, googleWalletPayment))
            .thenReturn(createGooglePayCheckoutSubject)

        val observer =
            googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price).test().assertNotComplete()

        assertTrue(createGooglePayCheckoutSubject.hasObservers())

        val expectedGooglePayCheckout = googlePayCheckout()
        createGooglePayCheckoutSubject.onSuccess(expectedGooglePayCheckout)

        observer.assertValue(expectedGooglePayCheckout)
    }
}
