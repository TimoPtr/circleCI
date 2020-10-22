/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.google.common.base.Optional.fromNullable
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Address.Companion.empty
import com.kolibree.android.shop.domain.model.Cart
import com.kolibree.android.shop.domain.model.Checkout
import com.kolibree.android.shop.presentation.checkout.shipping.ShippingBillingAnalytics
import com.kolibree.android.shop.quit
import com.kolibree.android.shop.quitOnEmptyCart
import com.kolibree.android.tracker.EventTracker
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class CheckoutActivityViewModel(
    initialViewState: CheckoutActivityViewState,
    private val navigator: CheckoutNavigator,
    private val addressProvider: AddressProvider,
    private val cartRepository: CartRepository,
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val eventTracker: EventTracker
) : BaseViewModel<CheckoutActivityViewState, CheckoutActivityAction>(initialViewState),
    CheckoutSharedViewModel {

    override val sharedViewStateLiveData: LiveData<CheckoutActivityViewState> = viewStateLiveData

    @SuppressLint("ExperimentalClassUse")
    val isLoading = mapNonNull<CheckoutActivityViewState, Boolean>(
        viewStateLiveData,
        initialViewState.progressVisible
    ) { state ->
        state.progressVisible
    }

    val snackbarConfiguration = twoWayMap(viewStateLiveData,
        { state -> state?.snackbarConfiguration },
        { configuration -> configuration?.let { updateViewState { copy(snackbarConfiguration = configuration) } } })

    override fun getSharedViewState(): CheckoutActivityViewState? = getViewState()

    override fun showError(error: Error) = updateViewState {
        copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
    }

    override fun hideError() {
        getViewState()?.takeIf { it.snackbarConfiguration.isShown }?.let {
            updateViewState { withSnackbarDismissed() }
        }
    }

    override fun showProgress(show: Boolean) {
        updateViewState { copy(progressVisible = show) }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy { addressProvider.observeForAddress() }
        disposeOnDestroy(::getCartFromRepository)
    }

    /**
     * This function retrieve the carts from the repository as a Flowable continuously updated
     */
    private fun getCartFromRepository(): Disposable {
        return cartRepository.getCartProducts().map { Cart(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ cart ->
                updateViewState { copy(cart = cart) }
            }, Timber::e)
    }

    /**
     * This function expose the [Cart] from the SharedViewState, retrieving the most recent one.
     * Having a nullable [Cart] protect us to do an empty Shopify update query on screen launch.
     */
    override val cartFlowable: Flowable<Cart> =
        viewStateFlowable.map { fromNullable(it.cart) }
            .flatMapSingle { it.asSingle() }
            .distinctUntilChanged()

    /**
     * Create the current session [Checkout], and update it each time the cart changes.
     * New subscribers will receive the [Checkout] emitted.
     */
    override val checkoutStream: Flowable<Checkout> =
        createCheckoutSingle()
            .flatMapPublisher(::updateCheckoutFlowable)
            .replay(1).also { disposeOnCleared { it.connect() } }

    /**
     * Create a [Checkout] and with an [Address] if possible
     */
    private fun createCheckoutSingle(): Single<Checkout> {
        return getAddressSingle()
            .flatMap { address -> shopifyClientWrapper.createCheckout(address) }
    }

    /**
     * Update the [Checkout] each time the [Cart] changes
     */
    private fun updateCheckoutFlowable(checkout: Checkout): Flowable<Checkout> {
        return cartFlowable
            .onBackpressureLatest()
            .concatMapSingle {
                shopifyClientWrapper.updateCheckoutCart(checkout, it)
            }
    }

    private fun getAddressSingle(): Single<Address> =
        addressProvider.getShippingAddress().first(empty())

    fun onToolbarIconClick() {
        pushAction(ToolbarIconClickAction)
    }

    fun onToolbarClickOnBilling() {
        ShippingBillingAnalytics.quit()
        navigator.navigateBack()
    }

    fun onToolbarIconClickOnCart() {
        if (getViewState()?.cart?.products?.isEmpty() == true) {
            eventTracker.quitOnEmptyCart()
        } else {
            eventTracker.quit()
        }
        navigator.navigateBack()
    }

    class Factory @Inject constructor(
        private val navigator: CheckoutNavigator,
        private val addressProvider: AddressProvider,
        private val cartRepository: CartRepository,
        private val shopifyClientWrapper: ShopifyClientWrapper,
        private val eventTracker: EventTracker
    ) : BaseViewModel.Factory<CheckoutActivityViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CheckoutActivityViewModel(
                viewState ?: CheckoutActivityViewState.initial(),
                navigator,
                addressProvider,
                cartRepository,
                shopifyClientWrapper,
                eventTracker
            ) as T
    }
}

/**
 * Return a [Single] matching the given [Optional]. The [Single] emits the value contained by the
 * [Optional], or it emits nothing.
 */
private fun <T> Optional<T>.asSingle(): Single<T> {
    return this.orNull()?.let { Single.just(it) } ?: Single.never()
}
