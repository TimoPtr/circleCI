/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.cart

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.shop.BR
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.VoucherProvider
import com.kolibree.android.shop.data.googlewallet.GooglePayAvailabilityUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.kolibree.android.shop.domain.model.Voucher
import com.kolibree.android.shop.onBuyWithAnotherMethodClick
import com.kolibree.android.shop.onBuyWithGooglePayClick
import com.kolibree.android.shop.onCheckoutDecreaseQuantityClick
import com.kolibree.android.shop.onCheckoutDelete
import com.kolibree.android.shop.onCheckoutDeleteUndo
import com.kolibree.android.shop.onCheckoutIncreaseQuantityClick
import com.kolibree.android.shop.onUseSmilesClick
import com.kolibree.android.shop.onVisitOurShopClick
import com.kolibree.android.shop.presentation.ShopItemBindingModel
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.list.ShopProductBindingModel
import com.kolibree.android.shop.presentation.list.ShopProductBindingModelDiffUtils
import com.kolibree.android.tracker.EventTracker
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

@SuppressLint("ExperimentalClassUse")
@Suppress("TooManyFunctions", "LargeClass")
internal class ShopCartViewModel(
    initialViewState: ShopCartViewState,
    private val sharedViewModel: CheckoutSharedViewModel,
    private val cartRepository: CartRepository,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val checkoutNavigator: CheckoutNavigator,
    private val voucherProvider: VoucherProvider,
    private val addressProvider: AddressProvider,
    private val smilesUseCase: SmilesUseCase,
    private val tracker: EventTracker,
    private val googlePayCheckoutUseCase: GooglePayCheckoutUseCase,
    private val timeScheduler: Scheduler
) : BaseViewModel<ShopCartViewState, ShopCartAction>(initialViewState),
    ShopCartInteraction, CheckoutSharedViewModel by sharedViewModel {

    @VisibleForTesting
    var lastRemovedProduct: ShopProductBindingModel? = null

    private val cachedVoucherSingle: Single<Voucher> by lazy {
        voucherProvider.getVoucher().cache()
    }

    val cartItems = mapNonNull<ShopCartViewState, List<ShopItemBindingModel>>(
        viewStateLiveData,
        defaultValue = initialViewState.cartItems
    ) { viewState -> viewState.cartItems }

    val cartProductsBinding = object : OnItemBindModel<ShopItemBindingModel>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: ShopItemBindingModel?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, this@ShopCartViewModel)
            itemBinding.bindExtra(BR.smilePointInteraction, this@ShopCartViewModel)
            itemBinding.bindExtra(BR.shipmentEstimationInteraction, this@ShopCartViewModel)
        }
    }

    /**
     * Gives the totalPrice of the cart, this amount is what the final user will have to paid
     */
    val totalPrice: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.totalPrice()?.formattedPrice() ?: ""
    }

    val initialCartProductsResult = getViewState()?.cartResult ?: CartProductsResult.Loading

    val cartProductsResult: LiveData<CartProductsResult> = map(viewStateLiveData) { viewState ->
        viewState?.cartResult ?: CartProductsResult.Loading
    }

    /**
     * Whether buttons are clickable or not
     */
    val areButtonsClickable: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.areButtonsClickable ?: true
    }

    /**
     * Whether Google Pay is available for the user session or not
     */
    val isGooglePayButtonVisible = mapNonNull<ShopCartViewState, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.isGooglePayButtonVisible
    ) { viewState -> viewState.isGooglePayButtonVisible }

    val isBuyAnotherMethodButtonVisible = mapNonNull<ShopCartViewState, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.isGooglePayButtonVisible
    ) { viewState -> viewState.isGooglePayButtonVisible }

    val isProceedCheckoutButtonVisible = mapNonNull<ShopCartViewState, Boolean>(
        viewStateLiveData,
        defaultValue = initialViewState.isGooglePayButtonVisible
    ) { viewState -> !viewState.isGooglePayButtonVisible }

    val diffConfig = ShopProductBindingModelDiffUtils

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        disposeOnDestroy { fetchSmilesAmount() }
        disposeOnDestroy { fetchCartProducts() }
        disposeOnDestroy { updateVoucherOnChange() }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        disposeOnPause { fetchTaxes() }
    }

    fun removeAllProductsOnPosition(position: Int) {
        pushAction(ShopCartAction.ProductRemovedBySwipe)

        val cartProducts = getViewState()?.cartProducts ?: emptyList()
        cartProducts.getOrNull(position)?.let {
            tracker.onCheckoutDelete(it.product)
            disposeOnCleared {
                cartRepository.removeAllProducts(it.product)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        productRemovedBySwipe(it, cartProducts.size - 1)
                    }, Timber::e)
            }
        }
    }

    @VisibleForTesting
    fun productRemovedBySwipe(removedProduct: ShopProductBindingModel, productsInCart: Int) {
        lastRemovedProduct = removedProduct
        pushAction(ShopCartAction.ShowProductRemovedSnackbar(productsInCart <= 0))
    }

    fun onUndoClick() {
        tracker.onCheckoutDeleteUndo()

        lastRemovedProduct?.let {
            disposeOnCleared {
                cartRepository.addProduct(it.product, it.quantity).subscribe({ }, Timber::e)
            }
        }
    }

    private fun fetchSmilesAmount() =
        smilesUseCase
            .smilesAmountStream()
            .subscribeOn(Schedulers.io())
            .subscribe(this::setSmilesAmount, Timber::e)

    private fun fetchCartProducts() =
        cartFlowable
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { updateViewState { copy(cartResult = CartProductsResult.Loading) } }
            .doOnSubscribe { showProgress(true) }
            .doFinally { showProgress(false) }
            .subscribe(this::refreshProducts, this::refreshProductsFailure)

    /**
     * Apply a voucher to the checkout each time the UseSmiles Switch and the checkout changes
     */
    private fun updateVoucherOnChange() =
        checkoutStream.firstElement().flatMapPublisher { checkout ->
            viewStateFlowable.map { it.useSmiles }.distinctUntilChanged().map { it to checkout }
        }
            .onBackpressureLatest()
            .concatMapSingle { (applyVoucher, checkout) ->
                updateShopifyVoucher(checkout, applyVoucher).andThen(Single.just(applyVoucher))
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ voucherApplied ->
                updateViewState { withVoucherApplied(voucherApplied) }
            }, Timber::e)

    private fun updateShopifyVoucher(checkout: Checkout, applyVoucher: Boolean): Completable {
        return if (applyVoucher) {
            cachedVoucherSingle.flatMapCompletable { voucher ->
                shopifyClientWrapper.applyVoucher(voucher, checkout)
            }
        } else {
            shopifyClientWrapper.removeVoucher(checkout)
        }
    }

    /**
     * Get the current session [Checkout] and fetch the the taxes each time
     * the voucher or the address changes.
     * It retrieves the first Shipping Rate available and then apply it to the current [Checkout]
     * A valid address is mandatory.
     */
    private fun fetchTaxes() = Flowable.combineLatest(
        checkoutStream, voucherStream(), validAddressStream(),
        Function3 { checkout: Checkout, _: Boolean, _: Address -> checkout }
    )
        .switchMapSingle { checkout ->
            shopifyClientWrapper.pollShippingRates(checkout.checkoutId)
                .flatMap { shopifyClientWrapper.updateShippingLine(checkout, it) }
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ newRates ->
            maybeScrollDownToRatesOnce()
            updateViewState { withAvailableRates(newRates) }
        }, Timber::e)

    /**
     * This Voucher [Boolean] can be null because we want it to be set only
     * when the viewState is changing. It avoids this way an unnecessary Shopify call at launch.
     */
    private fun voucherStream(): Flowable<Boolean> =
        viewStateFlowable.flatMapSingle { viewState ->
            viewState.voucherApplied?.let { Single.just(it) } ?: Single.never()
        }.distinctUntilChanged()

    /**
     * Get an [Address] stream which send us a full valid address
     */
    private fun validAddressStream(): Flowable<Address> =
        addressProvider
            .getShippingAddress()
            .toFlowable(BackpressureStrategy.LATEST)
            .filter { it.hasAllMandatoryFields() }

    @VisibleForTesting
    fun setSmilesAmount(smiles: Int) {
        updateViewState { withAvailableSmiles(smiles) }
    }

    /**
     * This function scroll to the item if rates is not fetched
     */
    private fun maybeScrollDownToRatesOnce() {
        if (getViewState()?.estimatedTaxes == null) {
            disposeOnPause {
                Completable.timer(100, TimeUnit.MILLISECONDS, timeScheduler)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete { pushAction(ShopCartAction.ScrollDownShowingRates) }
                    .subscribe({}, Timber::e)
            }
        }
    }

    @VisibleForTesting
    fun refreshProducts(cart: Cart) {
        val newCartProducts = mapToCartProducts(cart.products)
        val cartResult = when {
            newCartProducts.isNotEmpty() -> CartProductsResult.CartProductsAvailable
            else -> CartProductsResult.EmptyCart
        }

        updateViewState {
            copy(
                cartProducts = newCartProducts,
                cartResult = cartResult,
                paymentDetails = paymentDetails(this)
            )
        }
    }

    private fun mapToCartProducts(quantityProducts: List<QuantityProduct>) = quantityProducts
        .mapIndexed { index: Int, quantityProduct: QuantityProduct ->
            ShopProductBindingModel(
                product = quantityProduct.product,
                quantity = quantityProduct.quantity,
                withBottomDivider = index != quantityProducts.size - 1
            )
        }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        showProgress(false)
        disposeOnStop {
            viewStateFlowable.subscribe(::refreshPaymentDetails, Timber::e)
        }
    }

    @VisibleForTesting
    fun refreshPaymentDetails(viewState: ShopCartViewState) {
        updateViewState { copy(paymentDetails = paymentDetails(viewState)) }
    }

    @VisibleForTesting
    fun paymentDetails(viewState: ShopCartViewState) = ShopPaymentDetailsBindingModel(
        subtotal = viewState.subTotalPrice()?.formattedPrice() ?: "",
        actualDiscount = viewState.actualDiscount?.times(-1)?.formattedPrice() ?: "",
        isPossibleToUseSmiles = viewState.isPossibleToUseSmiles,
        useSmiles = viewState.useSmiles,
        potentialDiscountPrice = viewState.potentialDiscountPrice?.formattedPrice() ?: "",
        potentialDiscountSmilePoints = viewState.potentialDiscountPrice?.smilePoints.toString(),
        taxes = viewState.estimatedTaxes
    )

    @VisibleForTesting
    fun refreshProductsFailure(throwable: Throwable) {
        Timber.e(throwable)

        updateViewState { copy(cartResult = CartProductsResult.EmptyCart) }
    }

    override fun onAddToCartClick(product: Product) = onIncreaseQuantityClick(product)

    override fun onIncreaseQuantityClick(product: Product) {
        tracker.onCheckoutIncreaseQuantityClick(product)
        disposeOnCleared {
            cartRepository.addProduct(product).subscribe({ }, Timber::e)
        }
    }

    override fun onDecreaseQuantityClick(product: Product) {
        tracker.onCheckoutDecreaseQuantityClick(product)

        pushAction(ShopCartAction.ProductRemovedByDecreasingQuantity)

        disposeOnCleared {
            cartRepository.removeProduct(product).subscribe({ }, Timber::e)
        }
    }

    override fun onShipmentEstimationClick() {
        checkoutNavigator.showShippingAndBilling()
    }

    fun onVisitOurShopClick() {
        tracker.onVisitOurShopClick()
        checkoutNavigator.showOurShop()
    }

    fun onBuyWithGooglePayClick() {
        tracker.onBuyWithGooglePayClick()

        getViewState()?.totalPrice()?.let { price ->
            checkoutStream.firstOrError().flatMapMaybe { checkout ->
                googlePayCheckoutUseCase.onBuyWithGooglePayClick(checkout, price)
            }
                .doOnSubscribe { hideError() }
                .doOnSubscribe { showProgress(true) }
                .doOnSubscribe { updateViewState { copy(areButtonsClickable = false) } }
                .doFinally { updateViewState { copy(areButtonsClickable = true) } }
                .doFinally { showProgress(false) }
                .subscribe(
                    { Timber.d("Google pay token is $it") },
                    ::onGooglePayError,
                    { Timber.v("User canceled google pay checkout") }
                )
        } ?: Timber.w("ViewState or Price is null (${getViewState()})")
    }

    private fun onGooglePayError(throwable: Throwable) {
        if (throwable is NoSuchElementException) {
            Timber.v(throwable)
            // no-op, user cancelled
        } else {
            Timber.e(throwable)
            showError(Error.from(throwable))
        }
    }

    fun onBuyWithAnotherMethodClick() {
        tracker.onBuyWithAnotherMethodClick()

        disposeOnStop {
            checkoutStream
                .firstOrError()
                .doOnSubscribe { hideError() }
                .doOnSubscribe { showProgress(true) }
                .doOnSubscribe { updateViewState { copy(areButtonsClickable = false) } }
                .flatMap { checkout -> shopifyClientWrapper.pollWebUrl(checkout) }
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { updateViewState { copy(areButtonsClickable = true) } }
                .doOnError { showProgress(false) }
                .subscribe(
                    { checkoutNavigator.showAnotherPaymentScreen(it) },
                    { e -> Timber.e(e); showError(Error.from(e)) }
                )
        }
    }

    override fun onUseSmilesClick(shouldUseSmiles: Boolean) {
        updateViewState { copy(useSmiles = shouldUseSmiles) }
        tracker.onUseSmilesClick(shouldUseSmiles)
    }

    /**
     * Because VM is preserved on configuration change, this is recommended way of keeping
     * the list in a correct position between rotations.
     */
    val adapter = BindingRecyclerViewAdapter<ShopItemBindingModel>()

    class Factory @Inject constructor(
        private val sharedViewModel: CheckoutSharedViewModel,
        private val cartRepository: CartRepository,
        private val shopifyClientWrapper: ShopifyClientWrapper,
        private val checkoutNavigator: CheckoutNavigator,
        private val voucherProvider: VoucherProvider,
        private val addressProvider: AddressProvider,
        private val smilesUseCase: SmilesUseCase,
        private val googlePayCheckoutUseCase: GooglePayCheckoutUseCase,
        private val tracker: EventTracker,
        private val googlePayAvailabilityUseCase: GooglePayAvailabilityUseCase,
        @SingleThreadScheduler private val timeScheduler: Scheduler
    ) : BaseViewModel.Factory<ShopCartViewState>() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShopCartViewModel(
                initialViewState = viewState ?: initialViewState(googlePayAvailabilityUseCase),
                sharedViewModel = sharedViewModel,
                cartRepository = cartRepository,
                shopifyClientWrapper = shopifyClientWrapper,
                checkoutNavigator = checkoutNavigator,
                voucherProvider = voucherProvider,
                addressProvider = addressProvider,
                smilesUseCase = smilesUseCase,
                googlePayCheckoutUseCase = googlePayCheckoutUseCase,
                tracker = tracker,
                timeScheduler = timeScheduler
            ) as T
    }
}

private fun initialViewState(googlePayAvailabilityUseCase: GooglePayAvailabilityUseCase) =
    ShopCartViewState.initial(googlePayAvailabilityUseCase.isGooglePayAvailable())
