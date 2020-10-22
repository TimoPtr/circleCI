/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.shop.BR
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.onProductListAddToCartClick
import com.kolibree.android.shop.onProductListDecreaseQuantityClick
import com.kolibree.android.shop.onProductListIncreaseQuantityClick
import com.kolibree.android.shop.presentation.ShopItemBindingModel
import com.kolibree.android.tracker.EventTracker
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

private typealias State = ShopProductListViewState

@SuppressLint("ExperimentalClassUse")
internal class ShopProductListViewModel(
    initialViewState: State,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val cartRepository: CartRepository,
    private val tracker: EventTracker,
    private val googlePayAvailabilityUseCase: GooglePayAvailabilityUseCase,
    private val shopListScrollUseCase: ShopListScrollUseCase
) : BaseViewModel<State, ShopProductListAction>(
    initialViewState
), ShopProductInteraction {

    val products = mapNonNull<State, List<ShopItemBindingModel>>(
        viewStateLiveData,
        initialViewState.products
    ) { viewState ->
        viewState.products
    }

    val productsResult = mapNonNull<State, ProductsResult>(
        viewStateLiveData,
        defaultValue = initialViewState.productsResult
    ) { viewState -> viewState.productsResult }

    val productBinding = object : OnItemBindModel<ShopItemBindingModel>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: ShopItemBindingModel?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, this@ShopProductListViewModel)
        }
    }

    val diffConfig = ShopProductBindingModelDiffUtils

    /**
     * Because VM is preserved on configuration change, this is recommended way of keeping
     * the list in a correct position between rotations.
     */
    val adapter = BindingRecyclerViewAdapter<ShopItemBindingModel>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        googlePayAvailabilityUseCase.startWatch()

        disposeOnDestroy { fetchProducts() }
        disposeOnDestroy { watchScrollToItem() }
    }

    override fun onAddToCartClick(product: Product) {
        tracker.onProductListAddToCartClick(product)
        onIncreaseQuantityClick(product)
    }

    override fun onIncreaseQuantityClick(product: Product) {
        tracker.onProductListIncreaseQuantityClick(product)
        disposeOnDestroy {
            cartRepository.addProduct(product).subscribe({}, Timber::e)
        }
    }

    override fun onDecreaseQuantityClick(product: Product) {
        tracker.onProductListDecreaseQuantityClick(product)
        disposeOnDestroy {
            cartRepository.removeProduct(product).subscribe({}, Timber::e)
        }
    }

    @VisibleForTesting
    fun fetchProducts(): Disposable = cartRepository.getCartProducts()
        .switchMap { cartProducts ->
            val productsWithQuantity = cartProducts.map {
                it.product to it.quantity
            }.toMap()
            fetchProductsAndFillWithQuantity(productsWithQuantity)
        }.doOnSubscribe { updateViewState { copy(productsResult = ProductsResult.Loading) } }
        .subscribe(
            { products -> updateViewState { withProducts(products) } },
            { e ->
                Timber.e(e)
                updateViewState { copy(productsResult = ProductsResult.NoProducts) }
            }
        )

    @VisibleForTesting
    fun fetchProductsAndFillWithQuantity(cartProductsWithQuantity: Map<Product, Int>) =
        shopifyClientWrapper.getProducts().toFlowable().map { allProducts ->
            allProducts.map { product ->
                val quantity = cartProductsWithQuantity[product] ?: 0
                ShopProductBindingModel(product, quantity)
            }
        }

    @VisibleForTesting
    fun watchScrollToItem(): Disposable {
        val completeWhenLoadingIsDone = viewStateFlowable
            .takeWhile { it.productsResult == ProductsResult.Loading }
            .toObservable()

        return Completable
            .fromObservable(completeWhenLoadingIsDone)
            .andThen(shopListScrollUseCase.getItemIdToScroll())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::scrollToItem, Timber::e)
    }

    private fun scrollToItem(productId: String) {
        val products = getViewState()?.products
        if (products == null || products.isEmpty()) {
            return
        }

        val position = products.indexOfFirst { it.product.productId == productId }
        if (position >= 0) {
            pushActionWhenResumed(ShopProductListAction.ScrollToPosition(position))
        }
    }

    class Factory @Inject constructor(
        private val shopifyClientWrapper: ShopifyClientWrapper,
        private val cartRepository: CartRepository,
        private val googlePayAvailabilityUseCase: GooglePayAvailabilityUseCase,
        private val tracker: EventTracker,
        private val shopListScrollUseCase: ShopListScrollUseCase
    ) : BaseViewModel.Factory<ShopProductListViewState>() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShopProductListViewModel(
            initialViewState = viewState ?: State.initial(),
            shopifyClientWrapper = shopifyClientWrapper,
            cartRepository = cartRepository,
            googlePayAvailabilityUseCase = googlePayAvailabilityUseCase,
            tracker = tracker,
            shopListScrollUseCase = shopListScrollUseCase
        ) as T
    }
}
