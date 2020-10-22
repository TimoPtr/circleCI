/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.VoucherProvider
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.BasicCheckout
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.GooglePayCheckout
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.kolibree.android.shop.domain.model.Voucher
import com.kolibree.android.shop.domain.model.WebViewCheckout
import com.kolibree.android.shop.googlePayCheckout
import com.kolibree.android.shop.prepareCartWithProducts
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.list.ShopProductBindingModel
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.EventTracker
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.processors.ReplayProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.MaybeSubject
import java.math.BigDecimal
import java.util.Currency
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class ShopCartViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ShopCartViewModel

    private val cartRepository = mock<CartRepository>()

    private val shopifyClientWrapper = mock<ShopifyClientWrapper>()

    private val checkoutNavigator = mock<CheckoutNavigator>()

    private val voucherProvider = mock<VoucherProvider>()

    private val addressProvider = mock<AddressProvider>()

    private val smilesUseCase = mock<SmilesUseCase>()

    private val tracker = mock<EventTracker>()

    private val googlePayCheckoutUseCase: GooglePayCheckoutUseCase = mock()

    private val googlePayAvailabilityWatcher = FakeGooglePayAvailabilityUseCase()

    private val euroCurrency = Currency.getInstance("EUR")

    private val timeScheduler: Scheduler = mock()

    private val sharedViewModel: CheckoutSharedViewModel = mock()

    override fun setup() {
        super.setup()

        whenever(voucherProvider.getVoucher()).thenReturn(Single.just(Voucher("")))

        viewModel = init()
    }

    @Before
    fun doBefore() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @After
    fun doAfter() {
        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    initial view state
     */
    @Test
    fun `viewState isGooglePayButtonVisible is false if googlePayAvailabilityWatcher returns false`() {
        googlePayAvailabilityWatcher.googlePayAvailable = false

        val expectedViewState = ShopCartViewState.initial(false)

        assertEquals(expectedViewState, init().getViewState())
    }

    @Test
    fun `viewState isGooglePayButtonVisible is true if googlePayAvailabilityWatcher returns true`() {
        googlePayAvailabilityWatcher.googlePayAvailable = true

        val expectedViewState = ShopCartViewState.initial(true)

        assertEquals(expectedViewState, init().getViewState())
    }

    @Test
    fun `viewState isBuyAnotherMethodButtonVisible is false if play button is not visible`() {
        val viewState = ShopCartViewState.initial(false)

        viewModel.updateViewState { viewState }

        viewModel.isBuyAnotherMethodButtonVisible.test().assertValue(false)
    }

    @Test
    fun `viewState isBuyAnotherMethodButtonVisible is true if play button is visible`() {
        val viewState = ShopCartViewState.initial(true)

        viewModel.updateViewState { viewState }

        viewModel.isBuyAnotherMethodButtonVisible.test().assertValue(true)
    }

    @Test
    fun `viewState isProceedCheckoutButtonVisible is true if play button is not visible`() {
        val viewState = ShopCartViewState.initial(false)

        viewModel.updateViewState { viewState }

        viewModel.isProceedCheckoutButtonVisible.test().assertValue(true)
    }

    @Test
    fun `viewState isProceedCheckoutButtonVisible is false if play button is visible`() {
        val viewState = ShopCartViewState.initial(true)

        viewModel.updateViewState { viewState }

        viewModel.isProceedCheckoutButtonVisible.test().assertValue(false)
    }

    /*
    cartProducts
     */

    @Test
    fun `cartItems emits emptyList if viewState null`() {
        viewModel.cartItems.test().assertValue(emptyList())
    }

    @Test
    fun `cartItems emits viewState cartProducts and paymentDetails`() {
        val cartProducts = listOf(ShopProductBindingModel(buildProduct(), 42))
        val paymentDetails =
            ShopPaymentDetailsBindingModel(actualDiscount = "1", subtotal = "1.23$")
        viewModel.updateViewState {
            copy(
                cartProducts = cartProducts,
                paymentDetails = paymentDetails
            )
        }
        val expectedCartItems = cartProducts + paymentDetails
        viewModel.cartItems.test().assertValue(expectedCartItems)
    }

    /*
    cartProductsResult
     */

    @Test
    fun `cartProductsResult emits Loading if viewState null`() {
        viewModel.cartProductsResult.test().assertValue(CartProductsResult.Loading)
    }

    @Test
    fun `cartProductsResult emits viewState cartResult`() {
        viewModel.updateViewState { copy(cartResult = CartProductsResult.EmptyCart) }
        viewModel.cartProductsResult.test().assertValue(CartProductsResult.EmptyCart)
    }

    /*
    onCreate
    */

    @Test
    fun `onCreate fetches products from cart and smiles`() {
        mockDefaultStreams()
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(sharedViewModel).cartFlowable
        verify(smilesUseCase).smilesAmountStream()
    }

    @Test
    fun `onResume polls rates when a cart exists and user has a valid address`() {
        mockDefaultStreams()
        val checkout = pollShippingRateHappyPath()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper).pollShippingRates(checkout.checkoutId)
    }

    @Test
    fun `onResume polls rates as soon as a new checkout is received`() {
        mockDefaultStreams()
        val checkout = pollShippingRateHappyPath()

        val publishProcessor = PublishProcessor.create<Checkout>()

        whenever(sharedViewModel.checkoutStream).thenReturn(publishProcessor)

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper, never()).pollShippingRates(any())
        publishProcessor.offer(checkout)
        verify(shopifyClientWrapper).pollShippingRates(checkout.checkoutId)
    }

    @Test
    fun `onResume does not polls rates if a voucher request is still ongoing`() {
        mockDefaultStreams()
        val checkout = pollShippingRateHappyPath()

        // Here the Voucher request never finish
        whenever(shopifyClientWrapper.removeVoucher(checkout)).thenReturn(Completable.never())

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper, never()).pollShippingRates(any())
    }

    @Test
    fun `onResume does not polls rates when a checkout does not exists`() {
        mockDefaultStreams()
        pollShippingRateHappyPath()

        // Checkout does not exists, the happy path should not succeed
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.never())

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper, never()).pollShippingRates(any())
    }

    @Test
    fun `onResume does not polls rates when the address is invalid`() {
        mockDefaultStreams()
        pollShippingRateHappyPath()
        val invalidAddress = getFullAddress().copy(city = null)

        whenever(addressProvider.getShippingAddress()).thenReturn(Observable.just(invalidAddress))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper, never()).pollShippingRates(any())
    }

    @Test
    fun `onResume does not polls rates when the voucher process is not finished`() {
        mockDefaultStreams()
        val checkout = pollShippingRateHappyPath()

        whenever(shopifyClientWrapper.removeVoucher(checkout)).thenReturn(Completable.never())

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(shopifyClientWrapper, never()).pollShippingRates(any())
    }

    @Test
    fun `updateVoucherOnChange apply voucher by calling shopifyClient`() {
        mockDefaultStreams()

        val expectedVoucher: Voucher = mock()
        val expectedCheckout: BasicCheckout = mock()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(expectedCheckout))
        whenever(voucherProvider.getVoucher()).thenReturn(Single.just(expectedVoucher))
        whenever(shopifyClientWrapper.applyVoucher(expectedVoucher, expectedCheckout)).thenReturn(
            Completable.complete()
        )

        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(shopifyClientWrapper).applyVoucher(expectedVoucher, expectedCheckout)
        assertTrue(viewModel.getViewState()!!.voucherApplied!!)
    }

    @Test
    fun `updateVoucherOnChange digest the last received voucher if the view is updating too fast`() {
        mockDefaultStreams()

        val testScheduler = TestScheduler()
        val expectedVoucher: Voucher = mock()
        val expectedCheckout: BasicCheckout = mock()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(expectedCheckout))
        whenever(voucherProvider.getVoucher()).thenReturn(Single.just(expectedVoucher))
        whenever(shopifyClientWrapper.applyVoucher(expectedVoucher, expectedCheckout)).thenReturn(
            Completable.timer(100, TimeUnit.MILLISECONDS, testScheduler)
        )
        whenever(shopifyClientWrapper.removeVoucher(expectedCheckout)).thenReturn(
            Completable.timer(100, TimeUnit.MILLISECONDS, testScheduler)
        )

        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.updateViewState { copy(useSmiles = false) }
        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.updateViewState { copy(useSmiles = false) }
        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.updateViewState { copy(useSmiles = false) }
        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.updateViewState { copy(useSmiles = false) }
        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.updateViewState { copy(useSmiles = false) }

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        verify(shopifyClientWrapper).removeVoucher(expectedCheckout)
    }

    @Test
    fun `updateVoucherOnChange should query only one time the voucher provider`() {
        mockDefaultStreams()

        val expectedVoucher = Voucher("1234")
        val expectedCheckout: BasicCheckout = mock()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(expectedCheckout))
        whenever(voucherProvider.getVoucher()).thenReturn(Single.just(expectedVoucher))
        whenever(shopifyClientWrapper.applyVoucher(expectedVoucher, expectedCheckout))
            .thenReturn(Completable.complete())
        whenever(shopifyClientWrapper.removeVoucher(expectedCheckout))
            .thenReturn(Completable.complete())

        viewModel.updateViewState { copy(useSmiles = true) }
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(shopifyClientWrapper).applyVoucher(expectedVoucher, expectedCheckout)
        assertTrue(viewModel.getViewState()!!.voucherApplied!!)

        viewModel.updateViewState { copy(useSmiles = false) }

        verify(shopifyClientWrapper).removeVoucher(expectedCheckout)
        assertFalse(viewModel.getViewState()!!.voucherApplied!!)

        viewModel.updateViewState { copy(useSmiles = true) }

        verify(shopifyClientWrapper, times(2)).applyVoucher(expectedVoucher, expectedCheckout)
        assertTrue(viewModel.getViewState()!!.voucherApplied!!)

        verify(voucherProvider, times(1)).getVoucher()
    }

    @Test
    fun `updateVoucherOnChange remove voucher by calling shopifyClient`() {
        mockDefaultStreams()

        val expectedCheckout: BasicCheckout = mock()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(expectedCheckout))
        whenever(shopifyClientWrapper.removeVoucher(expectedCheckout)).thenReturn(
            Completable.complete()
        )

        viewModel.updateViewState { copy(useSmiles = false) }
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(shopifyClientWrapper).removeVoucher(expectedCheckout)
        assertFalse(viewModel.getViewState()!!.voucherApplied!!)
    }

    /*
    setSmilesAmount
     */

    @Test
    fun `setSmilesAmount updates availableSmiles`() {
        val smiles = 101

        viewModel.setSmilesAmount(smiles)

        assertTrue(viewModel.getViewState()?.isPossibleToUseSmiles == true)
        assertEquals(smiles, viewModel.getViewState()?.availableSmiles)
    }

    /*
    refreshProducts
     */
    @Test
    fun `refreshProducts updates cartResult to EmptyCart if no products`() {
        viewModel.updateViewState { initialViewState() }

        viewModel.refreshProducts(Cart())

        assertEquals(CartProductsResult.EmptyCart, viewModel.getViewState()?.cartResult)
    }

    @Test
    fun `refreshProducts updates cartResult to CartProductsAvailable if there are products`() {
        val product = buildProduct()
        val quantityProduct = QuantityProduct(2, product)
        viewModel.refreshProducts(Cart(listOf(quantityProduct)))

        assertEquals(CartProductsResult.CartProductsAvailable, viewModel.getViewState()?.cartResult)
    }

    /*
    refreshPaymentDetails
     */

    @Test
    fun `refreshPaymentDetails updates viewState paymentDetails`() {
        viewModel.updateViewState { initialViewState().copy(availableSmiles = 0) }
        assertFalse(viewModel.getViewState()!!.paymentDetails.isPossibleToUseSmiles)

        viewModel.refreshPaymentDetails(initialViewState().copy(availableSmiles = 10))
        assertTrue(viewModel.getViewState()!!.paymentDetails.isPossibleToUseSmiles)
    }

    /*
   paymentDetails
    */

    @Test
    fun `paymentDetails returns ShopPaymentDetailsBindingModel based on viewState`() {
        val vs = initialViewState().copy(
            cartProducts = listOf(product(1.0, 5), product(10.0, 2)),
            useSmiles = true,
            availableSmiles = 100
        )
        val paymentDetails = viewModel.paymentDetails(vs)

        val expectedSubtotal = Price.create(BigDecimal.valueOf(25.0), euroCurrency)
        assertEquals(expectedSubtotal.formattedPrice(), paymentDetails.subtotal)

        assertTrue(paymentDetails.useSmiles)

        assertTrue(paymentDetails.isPossibleToUseSmiles)

        val expectedSmileDiscount =
            Price.create(BigDecimal.valueOf(1.0), euroCurrency).times(-1).formattedPrice()
        assertEquals(expectedSmileDiscount, paymentDetails.actualDiscount)

        val expectedPotentialSmileDiscount =
            Price.create(BigDecimal.valueOf(1.0), euroCurrency).formattedPrice()
        assertEquals(expectedPotentialSmileDiscount, paymentDetails.potentialDiscountPrice)

        assertEquals("100", paymentDetails.potentialDiscountSmilePoints)
    }

    private fun product(price: Double, quantity: Int) = ShopProductBindingModel(
        product = buildProduct(
            price = Price.create(
                BigDecimal.valueOf(price),
                euroCurrency
            )
        ),
        quantity = quantity
    )

    /*
    refreshProductsFailure
     */

    @Test
    fun `refreshProductsFailure updates cartResult to EmptyCart`() {
        viewModel.refreshProductsFailure(mock())

        assertEquals(CartProductsResult.EmptyCart, viewModel.getViewState()?.cartResult)
    }

    /*
    onBuyWithAnotherMethodClick
     */

    @Test
    fun `onBuyWithAnotherMethodClick emits viewState with buttons clickable=false on subscription and clickable=true on checkout complete`() {
        mockDefaultStreams()

        val humCheckout = mock<BasicCheckout>()
        val viewStateObservable = viewModel.viewStateFlowable.test()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val checkoutProcessor = PublishProcessor.create<Checkout>()
        whenever(sharedViewModel.checkoutStream).thenReturn(checkoutProcessor)

        viewModel.onBuyWithAnotherMethodClick()

        viewStateObservable.assertLastValue(
            initialViewState(googlePayAvailabilityWatcher.isGooglePayAvailable()).copy(
                areButtonsClickable = false
            )
        )

        checkoutProcessor.offer(humCheckout)

        viewStateObservable.assertLastValue(
            initialViewState(googlePayAvailabilityWatcher.isGooglePayAvailable()).copy(
                areButtonsClickable = true
            )
        )
    }

    @Test
    fun `onBuyWithAnotherMethodClick emits loading states`() {
        mockDefaultStreams()

        val humCheckout = mock<BasicCheckout>()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val checkoutProcessor = PublishProcessor.create<Checkout>()
        whenever(sharedViewModel.checkoutStream).thenReturn(checkoutProcessor)

        viewModel.onBuyWithAnotherMethodClick()

        verify(sharedViewModel).showProgress(true)
        checkoutProcessor.offer(humCheckout)
        verify(sharedViewModel).showProgress(false)
    }

    @Test
    fun `onBuyWithAnotherMethodClick send the correct configured event`() {
        mockDefaultStreams()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel.onBuyWithAnotherMethodClick()

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_classic"))
    }

    @Test
    fun `onBuyWithAnotherMethodClick invokes showAnotherPaymentScreen on checkoutNavigator`() {
        mockDefaultStreams()

        val expectedBasicCheckout = mock<BasicCheckout>()
        val expectedWebCheckout = mock<WebViewCheckout>()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val checkoutProcessor = ReplayProcessor.create<Checkout>()
        checkoutProcessor.onNext(expectedBasicCheckout)

        whenever(sharedViewModel.checkoutStream).thenReturn(checkoutProcessor)
        whenever(shopifyClientWrapper.pollWebUrl(expectedBasicCheckout)).thenReturn(
            Single.just(expectedWebCheckout)
        )

        viewModel.onBuyWithAnotherMethodClick()

        verify(checkoutNavigator).showAnotherPaymentScreen(expectedWebCheckout)
    }

    /*
    onAddToCartClick
     */

    @Test
    fun `onAddToCartClick invokes addProduct on cartRepository`() {
        val productToAdd = buildProduct(variantOrdinal = 123)
        whenever(cartRepository.addProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onAddToCartClick(productToAdd)

        verify(cartRepository).addProduct(productToAdd)
    }

    /*
    onIncreaseQuantityClick
     */

    @Test
    fun `onIncreaseQuantityClick invokes addProduct on cartRepository and send the correct configured event`() {
        val productToAdd = buildProduct(variantOrdinal = 123)

        whenever(cartRepository.addProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onIncreaseQuantityClick(productToAdd)

        verify(cartRepository).addProduct(productToAdd)

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_plus").plus("variantId" to "Unknown"))
    }

    /*
    onDecreaseQuantityClick
     */

    @Test
    fun `onDecreaseQuantityClick invokes removeProduct on cartRepository and send the correct configured event`() {
        val productToAdd = buildProduct(variantOrdinal = 123)

        whenever(cartRepository.removeProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onDecreaseQuantityClick(productToAdd)

        verify(cartRepository).removeProduct(productToAdd)
        verify(tracker).sendEvent(AnalyticsEvent("Checkout_minus").plus("variantId" to "Unknown"))
    }

    @Test
    fun `onDecreaseQuantityClick emits ProductRemovedByDecreasingQuantity action`() {
        val product = buildProduct(variantOrdinal = 123)

        whenever(cartRepository.removeProduct(product)).thenReturn(Single.just(1))

        val action = viewModel.actionsObservable.test()
        viewModel.onDecreaseQuantityClick(product)
        action.assertValue(ShopCartAction.ProductRemovedByDecreasingQuantity)
    }

    /*
    onUseSmilesClick
     */

    @Test
    fun `onUseSmilesClick set useSmiles to false when shouldUseSmiles is false and send the correct configured event`() {
        viewModel.onUseSmilesClick(false)

        assertTrue(viewModel.getViewState()?.useSmiles == false)

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_UseMySmiles_Off"))
    }

    @Test
    fun `onUseSmilesClick set useSmiles to true when shouldUseSmiles true and send the correct configured event`() {

        viewModel.onUseSmilesClick(true)

        assertTrue(viewModel.getViewState()?.useSmiles == true)

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_UseMySmiles_On"))
    }

    /*
    onBuyWithGooglePayClick
     */

    @Test
    fun `onBuyWithGooglePayClick send the correct configured event`() {
        viewModel.onBuyWithGooglePayClick()

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_googlepay"))
    }

    @Test
    fun `onBuyWithGooglePayClick is called with the current session checkout`() {
        val viewState = prepareStateBuyWithGooglePay()
        val price = viewState.totalPrice()!!

        val checkout: Checkout = mock()

        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price))
            .thenReturn(Maybe.empty())

        viewModel.onBuyWithGooglePayClick()

        verify(googlePayCheckoutUseCase).onBuyWithGooglePayClick(checkout, price)
    }

    @Test
    fun `onBuyWithGooglePayClick never subscribes to googlePayCheckoutUseCase if there's no price`() {
        val subject = MaybeSubject.create<GooglePayCheckout>()

        val viewStateWithoutProducts =
            initialViewState(googlePayAvailabilityWatcher.isGooglePayAvailable()).copy(cartProducts = listOf())
        viewModel.updateViewState { viewStateWithoutProducts }

        viewModel.onBuyWithGooglePayClick()

        assertFalse(subject.hasObservers())
        verifyZeroInteractions(googlePayCheckoutUseCase, sharedViewModel)
    }

    @Test
    fun `onBuyWithGooglePayClick subscribes to googlePayCheckoutUseCase if price is not null`() {
        val viewState = prepareStateBuyWithGooglePay()
        val price = viewState.totalPrice()!!
        val checkout: Checkout = mock()

        val subject = MaybeSubject.create<GooglePayCheckout>()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price)).thenReturn(
            subject
        )

        viewModel.onBuyWithGooglePayClick()

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `onBuyWithGooglePayClick emits viewState with buttons clickable=false after subscribing to isReadyToPayRequest`() {
        val checkout: Checkout = mock()
        val viewState = prepareStateBuyWithGooglePay()
        val price = viewState.totalPrice()!!

        val subject = MaybeSubject.create<GooglePayCheckout>()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price)).thenReturn(
            subject
        )

        val viewStateObservable = viewModel.viewStateFlowable.test()

        viewModel.onBuyWithGooglePayClick()

        viewStateObservable.assertLastValue(
            viewState.copy(areButtonsClickable = false)
        )
    }

    @Test
    fun `onBuyWithGooglePayClick emits viewState with buttons clickable = true after onBuyWithGooglePayClick emits value`() {
        mockDefaultStreams()
        val checkout: Checkout = mock()
        val viewState = prepareStateBuyWithGooglePay()
        val price = viewState.totalPrice()!!

        val createGooglePayCheckoutSubject = MaybeSubject.create<GooglePayCheckout>()
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkout))
        whenever(googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price)).thenReturn(
            createGooglePayCheckoutSubject
        )

        val viewStateObservable = viewModel.viewStateFlowable.test()

        viewModel.onBuyWithGooglePayClick()

        viewStateObservable.assertLastValue(
            viewState.copy(areButtonsClickable = false)
        )

        createGooglePayCheckoutSubject.onSuccess(googlePayCheckout())

        viewStateObservable.assertLastValue(
            viewState.copy(areButtonsClickable = true)
        )
    }

    /*
     removeAllProductsOnPosition
     */

    @Test
    fun `removeAllProductsOnPosition emits ProductRemovedBySwipe action`() {
        val action = viewModel.actionsObservable.test()
        viewModel.removeAllProductsOnPosition(0)
        action.assertValue(ShopCartAction.ProductRemovedBySwipe)
    }

    @Test
    fun `removeAllProductsOnPosition sends analytics`() {
        whenever(cartRepository.removeAllProducts(any()))
            .thenReturn(Completable.complete())

        viewModel.actionsObservable.test()
        viewModel.updateViewState {
            ShopCartViewState.initial(false).copy(
                cartProducts = listOf(
                    ShopProductBindingModel(
                        product = buildProduct(),
                        quantity = 1
                    )
                )
            )
        }
        viewModel.removeAllProductsOnPosition(0)

        val details = mapOf("variantId" to "Unknown")
        verify(tracker).sendEvent(AnalyticsEvent("Checkout_delete", details))
    }

    @Test
    fun `removeAllProductsOnPosition invokes cartRepository removeAllProducts`() {
        val product = buildProduct(2)
        val cartProducts = listOf(
            ShopProductBindingModel(buildProduct(1), 11),
            ShopProductBindingModel(product, 22),
            ShopProductBindingModel(buildProduct(3), 33)
        )
        viewModel.updateViewState {
            ShopCartViewState(
                cartProducts = cartProducts,
                isGooglePayButtonVisible = true
            )
        }
        whenever(cartRepository.removeAllProducts(product)).thenReturn(Completable.complete())

        viewModel.removeAllProductsOnPosition(1)

        verify(cartRepository).removeAllProducts(product)
    }

    /*
    productRemovedBySwipe
     */

    @Test
    fun ` productRemovedBySwipe with 10 products emits ShowProductRemovedSnackbar action `() {
        val test = viewModel.actionsObservable.test()
        viewModel.productRemovedBySwipe(mock(), 10)
        test.assertValue(ShopCartAction.ShowProductRemovedSnackbar(isEmptyCart = false))
    }

    @Test
    fun ` productRemovedBySwipe with 0 product emits ShowProductRemovedSnackbar action `() {
        val test = viewModel.actionsObservable.test()
        viewModel.productRemovedBySwipe(mock(), 0)
        test.assertValue(ShopCartAction.ShowProductRemovedSnackbar(isEmptyCart = true))
    }

    @Test
    fun `productRemovedBySwipe assigns value to lastRemovedProduct`() {
        val productModel = ShopProductBindingModel(
            product = buildProduct(productOrdinal = 1),
            quantity = 13
        )
        viewModel.productRemovedBySwipe(productModel, 0)
        assertEquals(productModel, viewModel.lastRemovedProduct)
    }

    /*
   onUndoClick
    */

    @Test
    fun `onUndoClick invokes addProduct on cartRepository`() {
        val product = buildProduct(productOrdinal = 11)
        val quantity = 88
        val productModel = ShopProductBindingModel(
            product = product,
            quantity = quantity
        )
        whenever(cartRepository.addProduct(product, quantity))
            .thenReturn(Completable.complete())
        viewModel.lastRemovedProduct = productModel

        viewModel.onUndoClick()

        verify(cartRepository).addProduct(product, quantity)
    }

    @Test
    fun `onUndoClick does nothing if no lastRemovedProduct`() {
        viewModel.onUndoClick()

        verify(cartRepository, times(0)).addProduct(any(), any())
    }

    @Test
    fun `onUndoClick sends analytics events`() {
        viewModel.onUndoClick()

        verify(tracker).sendEvent(AnalyticsEvent("Checkout_delete_banner_undo"))
    }

    /*
    onVisitOurShopClick
     */

    @Test
    fun `onVisitOurShopClick invokes showOurShop`() {
        viewModel.onVisitOurShopClick()

        verify(checkoutNavigator).showOurShop()
    }

    @Test
    fun `onVisitOurShopClick should send event`() {
        viewModel.onVisitOurShopClick()

        verify(tracker).sendEvent(AnalyticsEvent("Shop_Cart_Empty_Visit"))
    }

    @Test
    fun `onShipmentEstimationClick invokes showShippingAndBilling`() {
        viewModel.onShipmentEstimationClick()

        verify(checkoutNavigator).showShippingAndBilling()
    }

    /*
    Utils
     */

    private fun init(): ShopCartViewModel {
        return ShopCartViewModel(
            initialViewState = initialViewState(googlePayAvailabilityWatcher.googlePayAvailable),
            cartRepository = cartRepository,
            shopifyClientWrapper = shopifyClientWrapper,
            checkoutNavigator = checkoutNavigator,
            voucherProvider = voucherProvider,
            addressProvider = addressProvider,
            smilesUseCase = smilesUseCase,
            tracker = tracker,
            googlePayCheckoutUseCase = googlePayCheckoutUseCase,
            sharedViewModel = sharedViewModel,
            timeScheduler = timeScheduler
        )
    }

    private fun mockDefaultStreams() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.never())
        whenever(smilesUseCase.smilesAmountStream()).thenReturn(Flowable.never())
        whenever(shopifyClientWrapper.pollShippingRates(any())).thenReturn(Single.never())
        whenever(addressProvider.getShippingAddress()).thenReturn(Observable.never())
        whenever(sharedViewModel.cartFlowable).thenReturn(Flowable.never())
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.never())
        whenever(shopifyClientWrapper.removeVoucher(any())).thenReturn(Completable.never())
    }

    private fun pollShippingRateHappyPath(): Checkout {
        val checkoutIdExpected = "checkoutId"

        val product = mock<Product>()
        val cartList = listOf(QuantityProduct(1, product))
        val cart = Cart(cartList)
        val checkoutMock = BasicCheckout(checkoutIdExpected, cart)

        whenever(product.price).thenReturn(
            Price.create(
                BigDecimal(10.10),
                Currency.getInstance("EUR")
            )
        )
        whenever(addressProvider.getShippingAddress()).thenReturn(Observable.just(getFullAddress()))
        whenever(sharedViewModel.checkoutStream).thenReturn(Flowable.just(checkoutMock))
        whenever(shopifyClientWrapper.removeVoucher(any())).thenReturn(Completable.complete())

        return checkoutMock
    }

    private fun getFullAddress() = Address(
        firstName = "Mister",
        lastName = "Bean",
        company = "TotoCompany",
        street = "147 Hanover St",
        city = "Boston",
        postalCode = "02108",
        country = "United States",
        province = "MA"
    )

    private fun prepareStateBuyWithGooglePay(): ShopCartViewState {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val cart = cartRepository.prepareCartWithProducts()

        val viewStateWithProducts =
            initialViewState(googlePayAvailabilityWatcher.isGooglePayAvailable()).copy(
                cartProducts = cart.products.map {
                    ShopProductBindingModel(
                        product = it.product,
                        quantity = it.quantity
                    )
                }
            )
        viewModel.updateViewState { viewStateWithProducts }

        return viewStateWithProducts
    }
}

private class FakeGooglePayAvailabilityUseCase : GooglePayAvailabilityUseCase {
    var googlePayAvailable = false

    override fun startWatch() {
        // no-op
    }

    override fun isGooglePayAvailable(): Boolean = googlePayAvailable
}
