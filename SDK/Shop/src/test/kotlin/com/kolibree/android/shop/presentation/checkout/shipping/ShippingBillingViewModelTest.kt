/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import androidx.lifecycle.LiveData
import com.jraska.livedata.test
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.R
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.request.query.error.ShopifyCheckoutUserError
import com.kolibree.android.shop.data.request.query.error.ShopifyInputError
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.checkout.shipping.AddressType.BILLING
import com.kolibree.android.shop.presentation.checkout.shipping.AddressType.SHIPPING
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mock

internal class ShippingBillingViewModelTest : BaseUnitTest() {

    @Mock
    private lateinit var navigator: CheckoutNavigator

    @Mock
    private lateinit var sharedViewModel: CheckoutSharedViewModel

    @Mock
    private lateinit var addressProvider: AddressProvider

    @Mock
    private lateinit var viewModel: ShippingBillingViewModel

    @Mock
    private lateinit var shopifyClientWrapper: ShopifyClientWrapper

    override fun setup() {
        super.setup()
        viewModel =
            ShippingBillingViewModel(
                ShippingBillingViewState(),
                sharedViewModel,
                navigator,
                addressProvider,
                shopifyClientWrapper
            )
        viewModel.actionsObservable.test()
    }

    @Test
    fun `onUserClickProceed should trigger an Navigation To Payment Action if shipping & billing are valid and different`() {
        val checkout: BasicCheckout = mock()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(shopifyClientWrapper.updateCheckoutAddress(checkout, getCompleteAddress()))
            .thenReturn(Single.just(checkout))

        val isBillingSameAsShipping = false
        val viewState = ShippingBillingViewState(
            getCompleteAddress(),
            getCompleteAddress(),
            isBillingSameAsShipping
        )

        viewModel.updateViewState { viewState }
        viewModel.onUserClickProceed()

        verify(navigator).navigateBack()
    }

    @Test
    fun `onUserClickProceed should trigger an Navigation To Payment Action if shipping is valid and same as billing`() {
        val checkout: BasicCheckout = mock()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(shopifyClientWrapper.updateCheckoutAddress(checkout, getCompleteAddress()))
            .thenReturn(Single.just(checkout))

        val isBillingSameAsShipping = true
        val viewState = ShippingBillingViewState(
            getCompleteAddress(),
            Address.empty(),
            isBillingSameAsShipping
        )

        viewModel.updateViewState { viewState }
        viewModel.onUserClickProceed()

        verify(navigator).navigateBack()
    }

    @Test
    fun `onUserClickProceed send analytics if shipping is valid and same as billing`() {
        val checkout: BasicCheckout = mock()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(shopifyClientWrapper.updateCheckoutAddress(checkout, getCompleteAddress()))
            .thenReturn(Single.just(checkout))

        val isBillingSameAsShipping = true
        val viewState = ShippingBillingViewState(
            getCompleteAddress(),
            Address.empty(),
            isBillingSameAsShipping
        )

        viewModel.updateViewState { viewState }
        viewModel.onUserClickProceed()

        verify(eventTracker).sendEvent(AnalyticsEvent("Billing_GoToPayment"))
    }

    @Test
    fun `onUserClickProceed should update the shopify address and the provider addresses`() {
        val checkout: BasicCheckout = mock()
        val shippingAddress = getCompleteAddress()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(shopifyClientWrapper.updateCheckoutAddress(checkout, shippingAddress))
            .thenReturn(Single.just(checkout))

        val isBillingSameAsShipping = true
        val billingAddress = Address.empty()
        val viewState =
            ShippingBillingViewState(shippingAddress, billingAddress, isBillingSameAsShipping)

        viewModel.updateViewState { viewState }
        viewModel.onUserClickProceed()

        with(inOrder(sharedViewModel)) {
            verify(sharedViewModel).showProgress(true)
            verify(sharedViewModel).showProgress(false)
        }

        verify(shopifyClientWrapper).updateCheckoutAddress(checkout, shippingAddress)

        verify(addressProvider).updateShippingAddress(shippingAddress)
        verify(addressProvider).updateBillingAddress(billingAddress)
        verify(addressProvider).setBillingSameAsShippingAddress(isBillingSameAsShipping)
    }

    @Test
    fun `onUserClickProceed triggers an error if Shopify throws ShopifyInputError`() {
        val shopifyInputError = ShopifyInputError("Readable Error")
        verifyErrorForUserClickProceedAction(
            expectedError = Error.from(shopifyInputError),
            shopifyFailure = shopifyInputError
        )
    }

    @Test
    fun `onUserClickProceed triggers an error if Shopify throws an unknown exception`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)
        verifyErrorForUserClickProceedAction(
            expectedError = Error.from(R.string.something_went_wrong),
            shopifyFailure = ShopifyCheckoutUserError(emptyList())
        )
    }

    private fun verifyErrorForUserClickProceedAction(
        expectedError: Error,
        shopifyFailure: Throwable
    ) {
        val checkout: BasicCheckout = mock()
        val shippingAddress = getCompleteAddress()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(shopifyClientWrapper.updateCheckoutAddress(checkout, shippingAddress))
            .thenReturn(Single.error(shopifyFailure))

        val isBillingSameAsShipping = true
        val billingAddress = Address.empty()
        val viewState =
            ShippingBillingViewState(shippingAddress, billingAddress, isBillingSameAsShipping)

        viewModel.updateViewState { viewState }
        viewModel.onUserClickProceed()

        with(inOrder(sharedViewModel)) {
            verify(sharedViewModel).showProgress(true)
            verify(sharedViewModel).showProgress(false)
        }

        verify(sharedViewModel).showError(expectedError)
        verifyZeroInteractions(addressProvider)
    }

    @Test
    fun `errorShippingFirstName should have the correct value if error is activated and shippingName is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(firstName = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingFirstName.assertLastValue(viewState.getError(SHIPPING, FIRST_NAME))
    }

    @Test
    fun `errorShippingLastName should have the correct value if error is activated and shippingName is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(lastName = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingLastName.assertLastValue(viewState.getError(SHIPPING, LAST_NAME))
    }

    @Test
    fun `errorShippingStreet should have the correct value if error is activated and shippingStreet is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(street = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingStreet.assertLastValue(viewState.getError(SHIPPING, ADDRESS_LINE_1))
    }

    @Test
    fun `errorShippingCity should have the correct value if error is activated and shippingCity is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(city = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingCity.assertLastValue(viewState.getError(SHIPPING, CITY))
    }

    @Test
    fun `errorShippingPostalCode should have the correct value if error is activated and shippingPostalCode is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(postalCode = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingPostalCode.assertLastValue(viewState.getError(SHIPPING, POSTAL_CODE))
    }

    @Test
    fun `errorShippingCountry should have the correct value if error is activated and shippingCountry is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress().copy(country = ""),
            billingAddress = getCompleteAddress()
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorShippingCountry.assertLastValue(viewState.getError(SHIPPING, COUNTRY))
    }

    @Test
    fun `errorBillingFirstName should have the correct value if error is activated and billingFirstName is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(firstName = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingFirstName.assertLastValue(viewState.getError(BILLING, FIRST_NAME))
    }

    @Test
    fun `errorBillingLastName should have the correct value if error is activated and billingLastName is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(lastName = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingLastName.assertLastValue(viewState.getError(BILLING, LAST_NAME))
    }

    @Test
    fun `errorBillingStreet should have the correct value if error is activated and billingStreet is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(street = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingStreet.assertLastValue(viewState.getError(BILLING, ADDRESS_LINE_1))
    }

    @Test
    fun `errorBillingCity should have the correct value if error is activated and billingCity is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(city = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingCity.assertLastValue(viewState.getError(BILLING, CITY))
    }

    @Test
    fun `errorBillingPostalCode should have the correct value if error is activated and billingPostalCode is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(postalCode = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingPostalCode.assertLastValue(viewState.getError(BILLING, POSTAL_CODE))
    }

    @Test
    fun `errorBillingCountry should have the correct value if error is activated and billingCountry is empty`() {
        val viewState = ShippingBillingViewState(
            shippingAddress = getCompleteAddress(),
            billingAddress = getCompleteAddress().copy(country = "")
        ).withErrorActivated()

        viewModel.updateViewState { viewState }

        viewModel.errorBillingCountry.assertLastValue(viewState.getError(BILLING, COUNTRY))
    }

    /*
    onBillingSameAsShipping
     */
    @Test
    fun `onBillingSameAsShipping enable sends analytics`() {
        val observer = viewModel.isBillingSameAsShipping.test()

        viewModel.isBillingSameAsShipping.postValue(true)

        observer.assertValue(true)

        verify(eventTracker).sendEvent(AnalyticsEvent("Billing_ShoppingAddressDiff_Off"))
    }

    @Test
    fun `onBillingSameAsShipping not enable sends analytics`() {
        val observer = viewModel.isBillingSameAsShipping.test()

        viewModel.isBillingSameAsShipping.postValue(false)

        observer.assertValue(false)

        verify(eventTracker).sendEvent(AnalyticsEvent("Billing_ShoppingAddressDiff_On"))
    }

    private fun getCompleteAddress() =
        Address(
            firstName = "FirstName",
            lastName = "LastName",
            street = "Address",
            city = "City",
            postalCode = "Postal Code",
            country = "Country",
            province = "Province"
        )

    private fun <T> LiveData<T>.assertLastValue(errorAddress: T?) {
        test().assertValue(errorAddress)
    }
}
