/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.kolibree.android.shop.presentation.checkout.CheckoutActivityViewModel
import com.kolibree.android.shop.presentation.checkout.CheckoutActivityViewState
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class CheckoutActivityViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: CheckoutActivityViewModel
    private val navigator: CheckoutNavigator = mock()
    private val addressProvider: AddressProvider = mock()

    private val shopifyClientWrapper: ShopifyClientWrapper = mock()
    private val cartRepository: CartRepository = mock()

    override fun setup() {
        super.setup()

        whenever(addressProvider.getShippingAddress()).thenReturn(Observable.empty())
    }

    @Test
    fun `snackbarConfig offers 2-way binding`() {
        createViewModel()

        val observer = viewModel.snackbarConfiguration.testTwoWay()
        observer.assertValue(SnackbarConfiguration(false, null))

        val config1 = SnackbarConfiguration(true, Error.from("exception"))
        observer.update(config1)
        observer.assertValue(config1)
        assertEquals(config1, viewModel.getSharedViewState()!!.snackbarConfiguration)

        val config2 = SnackbarConfiguration(false, Error.from("exception2"))
        observer.update(config2)
        observer.assertValue(config2)
        assertEquals(config2, viewModel.getSharedViewState()!!.snackbarConfiguration)
    }

    @Test
    fun `errors are shown as snackbars`() {
        createViewModel()

        val observer = viewModel.snackbarConfiguration.test()
        val error = Error.from("some error")

        viewModel.showError(error)
        observer.assertValue(SnackbarConfiguration(isShown = true, error = error))

        viewModel.hideError()
        observer.assertValue(SnackbarConfiguration(isShown = false, error = error))
    }

    @Test
    fun `addressProvider observeForAddress is subscribed in onCreate`() {
        createViewModel()

        val disposableInitialize: Disposable = mock()

        whenever(addressProvider.observeForAddress()).thenReturn(disposableInitialize)
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.never())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        val isDeleted =
            viewModel.onDestroyDisposables.compositeDisposable.delete(disposableInitialize)

        // If the deletion succeed, means that the observeForAddress has been well added into the
        // CompositeDisposable
        assertTrue(isDeleted)
    }

    @Test
    fun `getCartFromRepository update the viewState according to the cart received and cartFlowable receive the value`() {
        createViewModel()

        val quantityProducts = listOf(QuantityProduct(1, mock()))
        val expectedCart = Cart(quantityProducts)

        whenever(addressProvider.observeForAddress()).thenReturn(mock())
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(quantityProducts))

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertEquals(expectedCart, viewModel.getViewState()!!.cart)
        viewModel.cartFlowable.test().assertValue(expectedCart)
    }

    @Test
    fun `cartFlowable receive the values of the state changed`() {
        createViewModel()

        val mockCart1 = mock<Cart>()
        val mockCart2 = mock<Cart>()
        val testFlowable = viewModel.cartFlowable.test()

        viewModel.updateViewState { copy(cart = mockCart1) }
        testFlowable.assertValueCount(1)
        testFlowable.assertLastValue(mockCart1)

        viewModel.updateViewState { copy(cart = mockCart2) }
        testFlowable.assertValueCount(2)
        testFlowable.assertLastValue(mockCart2)

        // Insure the same Cart is not digested twice in a row
        viewModel.updateViewState { copy(cart = mockCart2) }
        testFlowable.assertValueCount(2)
    }

    @Test
    fun `createCheckout is called with the expected address`() {
        val expectedAddress = getAddress()

        whenever(addressProvider.observeForAddress()).thenReturn(mock())
        whenever(addressProvider.getShippingAddress()).thenReturn(Observable.just(expectedAddress))
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.never())

        createViewModel()

        verify(shopifyClientWrapper).createCheckout(expectedAddress)
    }

    @Test
    fun `createCheckout should have an empty Address if the addressProvider have not emitted any values`() {
        createViewModel()

        verify(shopifyClientWrapper).createCheckout(Address.empty())
    }

    @Test
    fun `checkoutStream should update the checkout when the cart changes`() {
        val expectedCheckout: Checkout = mock()
        val expectedCart = Cart(listOf(mock(), mock(), mock()))
        val expectedCart2 = Cart(listOf(mock()))

        whenever(shopifyClientWrapper.createCheckout(any())).thenReturn(Single.just(expectedCheckout))
        whenever(shopifyClientWrapper.updateCheckoutCart(eq(expectedCheckout), any()))
            .thenReturn(Single.just(expectedCheckout))

        createViewModel()

        viewModel.checkoutStream.test()

        viewModel.updateViewState { copy(cart = expectedCart) }
        verify(shopifyClientWrapper).updateCheckoutCart(expectedCheckout, expectedCart)

        viewModel.updateViewState { copy(cart = expectedCart2) }
        verify(shopifyClientWrapper).updateCheckoutCart(expectedCheckout, expectedCart2)
    }

    @Test
    fun `checkoutStream should not update the checkout when the cart is null`() {
        whenever(shopifyClientWrapper.createCheckout(any())).thenReturn(Single.just(mock()))
        createViewModel()

        viewModel.checkoutStream.test()
        viewModel.updateViewState { copy(cart = null) }

        verify(shopifyClientWrapper).createCheckout(any())
        verifyNoMoreInteractions(shopifyClientWrapper)
    }

    private fun getAddress(): Address {
        return Address(
            firstName = "Freddie",
            lastName = "Mercury",
            street = "123 Street",
            city = "Boston",
            postalCode = "456 789",
            country = "United States",
            province = "MA",
            email = "bohemian@rhapsody.ma",
            phoneNumber = "+33123456789"
        )
    }

    @Test
    fun `onCloseClick on Cart screen pushes FinishScreen action`() {
        createViewModel()

        viewModel.onToolbarIconClickOnCart()

        verify(navigator).navigateBack()
    }

    @Test
    fun `onCloseClick on Billing screen pushes FinishScreen action`() {
        createViewModel()

        viewModel.onToolbarClickOnBilling()

        verify(navigator).navigateBack()
    }

    @Test
    fun `onCloseClick on Billing screen sends Analytics event`() {
        createViewModel()

        viewModel.onToolbarClickOnBilling()

        verify(eventTracker).sendEvent(AnalyticsEvent("Billing_quit"))
    }

    @Test
    fun `onCloseClick sends analytics when cart empty`() {
        createViewModel()

        viewModel.updateViewState { copy(cart = Cart(products = emptyList())) }
        viewModel.onToolbarIconClickOnCart()

        verify(eventTracker).sendEvent(AnalyticsEvent("Shop_Cart_Empty_GoBack"))
    }

    @Test
    fun `onCloseClick sends analytics when cart not empty`() {
        createViewModel()

        viewModel.updateViewState { copy(cart = Cart(products = listOf(mock()))) }
        viewModel.onToolbarIconClickOnCart()

        verify(eventTracker).sendEvent(AnalyticsEvent("Shop_Cart_Quit"))
    }

    private fun createViewModel() {
        viewModel =
            CheckoutActivityViewModel(
                CheckoutActivityViewState.initial(),
                navigator,
                addressProvider,
                cartRepository,
                shopifyClientWrapper,
                eventTracker
            )
    }
}
