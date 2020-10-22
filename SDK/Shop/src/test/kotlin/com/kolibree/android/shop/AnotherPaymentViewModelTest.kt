/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.checkout.payment.AnotherPaymentAction
import com.kolibree.android.shop.presentation.checkout.payment.AnotherPaymentViewModel
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class AnotherPaymentViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: AnotherPaymentViewModel

    private val sharedViewModel: CheckoutSharedViewModel = mock()

    private val webViewCheckout = WebViewCheckout(
        checkoutId = "id_123",
        cart = Cart(),
        webUrl = "web.url.test"
    )

    override fun setup() {
        super.setup()

        viewModel = AnotherPaymentViewModel(null, sharedViewModel, webViewCheckout)
    }

    @Test
    fun `onViewPrepared() pushes SetPaymentProcessingResult`() {
        val action = viewModel.actionsObservable.test()

        viewModel.onViewPrepared()

        action.assertValue(AnotherPaymentAction.SetPaymentProcessingResult(webViewCheckout))
    }

    @Test
    fun `hide progress when checkout page is ready`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel.onViewPrepared()

        verify(sharedViewModel).showProgress(false)
    }
}
