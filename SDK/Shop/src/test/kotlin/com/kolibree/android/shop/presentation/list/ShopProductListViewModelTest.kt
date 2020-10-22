/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.kolibree.android.shop.domain.model.StoreDetails
import com.kolibree.android.shop.onProductListAddToCartClick
import com.kolibree.android.shop.onProductListDecreaseQuantityClick
import com.kolibree.android.shop.onProductListIncreaseQuantityClick
import com.kolibree.android.test.lifecycleTester
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.android.tracker.EventTracker
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

class ShopProductListViewModelTest : BaseUnitTest() {

    private val storeDetails = mock<StoreDetails>()
    private val products = spy(listOf(buildProduct()))
    private val productBindingModels = listOf(ShopProductBindingModel(buildProduct(), 0))

    private val initialViewState = ShopProductListViewState.initial()

    private val shopifyClientWrapper: ShopifyClientWrapper = mock()

    private val cartRepository: CartRepository = mock()

    private val googlePayAvailabilityUseCase: GooglePayAvailabilityUseCase = mock()

    private val tracker: EventTracker = mockk(relaxed = true)

    private val shopListScrollUseCase: ShopListScrollUseCase = mock()

    private lateinit var viewModel: ShopProductListViewModel

    override fun setup() {
        whenever(shopifyClientWrapper.getProducts()).thenReturn(Single.just(products))
        whenever(shopListScrollUseCase.getItemIdToScroll()).thenReturn(Observable.empty())

        viewModel = spy(
            ShopProductListViewModel(
                initialViewState,
                shopifyClientWrapper,
                cartRepository,
                tracker,
                googlePayAvailabilityUseCase,
                shopListScrollUseCase
            )
        )
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
    onCreate
     */

    @Test
    fun `onCreate invokes googlePayAvailabilityWatcher startWatch`() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))

        verify(googlePayAvailabilityUseCase, never()).startWatch()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(googlePayAvailabilityUseCase).startWatch()
    }

    @Test
    fun `onCreate fetches Shopify data`() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(shopifyClientWrapper).getProducts()
    }

    @Test
    fun `fetchProducts updates view state and live data`() {
        viewModel.updateViewState { copy(products = productBindingModels) }

        assertEquals(productBindingModels, viewModel.getViewState()!!.products)
    }

    @Test
    fun `fetchProducts updates view state to ProductsAvailable if has items`() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))

        viewModel.fetchProducts()

        assertEquals(ProductsResult.ProductsAvailable, viewModel.getViewState()!!.productsResult)
    }

    @Test
    fun `fetchProducts updates view state to NoProducts if no items`() {
        whenever(shopifyClientWrapper.getProducts()).thenReturn(Single.just(emptyList()))
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))

        viewModel.fetchProducts()

        assertEquals(ProductsResult.NoProducts, viewModel.getViewState()!!.productsResult)
    }

    @Test
    fun `onAddToCartClick invokes addProduct on cartRepository and tracker onProductListAddToCartClick`() {
        val productToAdd = buildProduct()

        mockkTrackerConstant()

        every {
            tracker.onProductListAddToCartClick(productToAdd)
        } returns Unit

        whenever(cartRepository.addProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onAddToCartClick(productToAdd)

        verify(cartRepository).addProduct(productToAdd)
        io.mockk.verify {
            tracker.onProductListAddToCartClick(productToAdd)
        }
    }

    @Test
    fun `onIncreaseQuantityClick invokes addProduct on cartRepository and tracker onProductListIncreaseQuantityClick`() {
        val productToAdd = buildProduct(variantOrdinal = 123)

        mockkTrackerConstant()

        every {
            tracker.onProductListIncreaseQuantityClick(productToAdd)
        } returns Unit

        whenever(cartRepository.addProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onIncreaseQuantityClick(productToAdd)

        verify(cartRepository).addProduct(productToAdd)
        io.mockk.verify {
            tracker.onProductListIncreaseQuantityClick(productToAdd)
        }
    }

    @Test
    fun `onDecreaseQuantityClick invokes removeProduct on cartRepository and tracker onProductListDecreaseQuantityClick`() {
        val productToAdd = buildProduct(variantOrdinal = 123)

        mockkTrackerConstant()

        every {
            tracker.onProductListDecreaseQuantityClick(productToAdd)
        } returns Unit

        whenever(cartRepository.removeProduct(productToAdd)).thenReturn(Single.just(1))

        viewModel.onDecreaseQuantityClick(productToAdd)

        verify(cartRepository).removeProduct(productToAdd)
        io.mockk.verify {
            tracker.onProductListDecreaseQuantityClick(productToAdd)
        }
    }

    @Test
    fun `does not scroll the screen if there are no items`() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))
        whenever(shopListScrollUseCase.getItemIdToScroll()).thenReturn(Observable.just("1"))

        val testObserver = viewModel.actionsObservable.test()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        testObserver.assertNoValues()
        testObserver.assertNoErrors()
    }

    @Test
    fun `does not scroll the screen if item not found`() {
        val products = mockShopifyProducts().map { QuantityProduct(1, it) }
        assumeTrue(products.isNotEmpty())

        whenever(cartRepository.getCartProducts())
            .thenReturn(Flowable.just(products))
        whenever(shopListScrollUseCase.getItemIdToScroll())
            .thenReturn(Observable.just("not_existing_id"))

        val testObserver = viewModel.actionsObservable.test()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        testObserver.assertNoValues()
        testObserver.assertNoErrors()
    }

    @Test
    fun `scroll the screen if item available`() {
        val products = mockShopifyProducts().map { QuantityProduct(1, it) }
        assumeTrue(products.isNotEmpty())

        val testId = products.first().product.productId

        whenever(cartRepository.getCartProducts())
            .thenReturn(Flowable.just(products))
        whenever(shopListScrollUseCase.getItemIdToScroll())
            .thenReturn(Observable.just(testId))

        val testObserver = viewModel.actionsObservable.test()

        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        val scrollActions = testObserver
            .values()
            .filterIsInstance(ShopProductListAction.ScrollToPosition::class.java)
            .toList()

        assertEquals(1, scrollActions.size)
        assertEquals(0, scrollActions.first().position)
    }

    /*
    onCreate
     */

    @Test
    fun `onCreate subscribes to expected events`() {
        whenever(cartRepository.getCartProducts()).thenReturn(Flowable.just(emptyList()))
        viewModel.onCreate(mock())

        verify(viewModel).fetchProducts()
        verify(viewModel).watchScrollToItem()
    }

    /*
    fetchProductsAndFillWithQuantity
     */

    @Test
    fun `fetchProductsAndFillWithQuantity returns all products with quantity 0 if cart is empty`() {
        val products = mockShopifyProducts()
        val expectedResult = listOf(
            ShopProductBindingModel(products[0], 0),
            ShopProductBindingModel(products[1], 0),
            ShopProductBindingModel(products[2], 0)
        )
        viewModel.fetchProductsAndFillWithQuantity(emptyMap())
            .test().assertValue(expectedResult)
    }

    @Test
    fun `fetchProductsAndFillWithQuantity returns products with an appropriate quantity`() {
        val products = mockShopifyProducts()
        val productQuantityMap = mapOf(
            products[0] to 7,
            products[2] to 3
        )
        val expectedResult = listOf(
            ShopProductBindingModel(products[0], 7),
            ShopProductBindingModel(products[1], 0),
            ShopProductBindingModel(products[2], 3)
        )
        viewModel.fetchProductsAndFillWithQuantity(productQuantityMap)
            .test().assertValue(expectedResult)
    }

    private fun mockShopifyProducts(): List<Product> {
        val products = listOf(
            buildProduct(productOrdinal = 1, variantOrdinal = 1),
            buildProduct(productOrdinal = 2, variantOrdinal = 2),
            buildProduct(productOrdinal = 3, variantOrdinal = 3)
        )
        whenever(shopifyClientWrapper.getProducts()).thenReturn(Single.just(products))
        return products
    }

    private fun mockkTrackerConstant() {
        mockkStatic("com.kolibree.android.shop.TrackerConstantKt")
    }
}
