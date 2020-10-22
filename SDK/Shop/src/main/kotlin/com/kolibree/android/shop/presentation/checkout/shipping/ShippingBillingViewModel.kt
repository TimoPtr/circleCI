/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.R
import com.kolibree.android.shop.data.AddressProvider
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.request.query.error.ShopifyInputError
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.Address.Input.PROVINCE
import com.kolibree.android.shop.presentation.checkout.CheckoutNavigator
import com.kolibree.android.shop.presentation.checkout.CheckoutSharedViewModel
import com.kolibree.android.shop.presentation.checkout.shipping.AddressType.BILLING
import com.kolibree.android.shop.presentation.checkout.shipping.AddressType.SHIPPING
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

private typealias State = ShippingBillingViewState

@Suppress("LargeClass")
internal class ShippingBillingViewModel(
    initialViewState: ShippingBillingViewState,
    private val sharedViewModel: CheckoutSharedViewModel,
    private val navigator: CheckoutNavigator,
    private val addressProvider: AddressProvider,
    private val shopifyClientWrapper: ShopifyClientWrapper
) : BaseViewModel<ShippingBillingViewState, ShippingBillingAction>(initialViewState),
    CheckoutSharedViewModel by sharedViewModel {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy { listenToShippingAddress() }
        disposeOnDestroy { listenToBillingAddress() }
        disposeOnDestroy { listenToBillingSameAsShipping() }
    }

    private fun listenToBillingSameAsShipping(): Disposable =
        addressProvider.getBillingSameAsShippingAddress()
            .subscribeOn(Schedulers.io())
            .subscribe({ updateViewState { copy(isBillingSameAsShipping = it) } }, Timber::e)

    private fun listenToBillingAddress(): Disposable =
        addressProvider.getBillingAddress()
            .subscribeOn(Schedulers.io())
            .subscribe({ updateViewState { copy(billingAddress = it) } }, Timber::e)

    private fun listenToShippingAddress(): Disposable =
        addressProvider.getShippingAddress()
            .subscribeOn(Schedulers.io())
            .subscribe({ updateViewState { copy(shippingAddress = it) } }, Timber::e)

    //region SHIPPING Live Data

    val shippingFirstName = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.firstName },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(firstName = it)) } }
    )

    val shippingLastName = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.lastName },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(lastName = it)) } }
    )

    val shippingCompany = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.company },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(company = it)) } }
    )

    val shippingStreet = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.street },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(street = it)) } }
    )

    val shippingCity = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.city },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(city = it)) } }
    )

    val shippingPostalCode = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.postalCode },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(postalCode = it)) } }
    )

    val shippingProvince = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.province },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(province = it)) } }
    )

    val shippingCountry = twoWayMap(viewStateLiveData,
        { state -> state?.shippingAddress?.country ?: Address.DEFAULT_COUNTRY },
        { updateViewState { copy(shippingAddress = shippingAddress.copy(country = it)) } }
    )

    //endregion SHIPPING Live Data

    val isBillingSameAsShipping = twoWayMap(
        viewStateLiveData,
        { state -> state?.isBillingSameAsShipping },
        ::onBillingSameAsShipping
    )

    private fun onBillingSameAsShipping(result: Boolean?) {
        updateViewState {
            val sameAsShipping = result ?: isBillingSameAsShipping
            ShippingBillingAnalytics.shippingAndBillingAddressDiff(!sameAsShipping)
            copy(isBillingSameAsShipping = sameAsShipping)
        }
    }

    //region BILLING Live Data

    val billingFirstName = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.firstName },
        { updateViewState { copy(billingAddress = billingAddress.copy(firstName = it)) } }
    )

    val billingLastName = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.lastName },
        { updateViewState { copy(billingAddress = billingAddress.copy(lastName = it)) } }
    )

    val billingCompany = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.company },
        { updateViewState { copy(billingAddress = billingAddress.copy(company = it)) } }
    )

    val billingStreet = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.street },
        { updateViewState { copy(billingAddress = billingAddress.copy(street = it)) } }
    )

    val billingCity = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.city },
        { updateViewState { copy(billingAddress = billingAddress.copy(city = it)) } }
    )

    val billingPostalCode = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.postalCode },
        { updateViewState { copy(billingAddress = billingAddress.copy(postalCode = it)) } }
    )

    val billingProvince = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.province },
        { updateViewState { copy(billingAddress = billingAddress.copy(province = it)) } }
    )

    val billingCountry = twoWayMap(viewStateLiveData,
        { state -> state?.billingAddress?.country },
        { updateViewState { copy(billingAddress = billingAddress.copy(country = it)) } }
    )

    val billingVisibility =
        mapNonNull<State, Boolean>(viewStateLiveData, initialViewState.isBillingVisible) { state ->
            state.isBillingVisible
        }

    //endregion BILLING Live Data

    //region SHIPPING Error Live Data

    val errorShippingFirstName = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, FIRST_NAME)
    }

    val errorShippingLastName = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, LAST_NAME)
    }

    val errorShippingStreet = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, ADDRESS_LINE_1)
    }

    val errorShippingCity = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, CITY)
    }

    val errorShippingPostalCode = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, POSTAL_CODE)
    }

    val errorShippingProvince = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, PROVINCE)
    }

    val errorShippingCountry = map(viewStateLiveData) { state ->
        state?.getError(SHIPPING, COUNTRY)
    }

    //endregion SHIPPING Error Live Data

    //region BILLING Error Live Data

    val errorBillingFirstName = map(viewStateLiveData) { state ->
        state?.getError(BILLING, FIRST_NAME)
    }

    val errorBillingLastName = map(viewStateLiveData) { state ->
        state?.getError(BILLING, LAST_NAME)
    }

    val errorBillingStreet = map(viewStateLiveData) { state ->
        state?.getError(BILLING, ADDRESS_LINE_1)
    }

    val errorBillingCity = map(viewStateLiveData) { state ->
        state?.getError(BILLING, CITY)
    }

    val errorBillingPostalCode = map(viewStateLiveData) { state ->
        state?.getError(BILLING, POSTAL_CODE)
    }

    val errorBillingProvince = map(viewStateLiveData) { state ->
        state?.getError(BILLING, PROVINCE)
    }

    val errorBillingCountry = map(viewStateLiveData) { state ->
        state?.getError(BILLING, COUNTRY)
    }

    //endregion BILLING Error Live Data

    fun onUserClickProceed() {
        getViewState()?.let { state ->
            if (state.isShippingValid() && state.isBillingValid()) {
                createCheckoutAddress(
                    state.shippingAddress, state.billingAddress, state.isBillingSameAsShipping
                )
            } else {
                updateViewState { withErrorActivated() }

                getViewState()?.getFirstError()?.let {
                    pushAction(ShippingBillingAction.ScrollToError(it))
                }
            }
        }
    }

    private fun createCheckoutAddress(
        address: Address,
        billingAddress: Address,
        billingSameAsShipping: Boolean
    ) {
        disposeOnCleared {
            checkoutStream
                .firstOrError()
                .flatMap { shopifyClientWrapper.updateCheckoutAddress(it, address) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showProgress(true) }
                .doFinally { showProgress(false) }
                .subscribe({
                    onCheckoutAddressUpdated(address, billingAddress, billingSameAsShipping)
                }, ::onCheckoutAddressError)
        }
    }

    private fun onCheckoutAddressError(error: Throwable) {
        sharedViewModel.showError(
            if (error is ShopifyInputError) {
                if (error.message != null) {
                    Error.from(error)
                } else {
                    Error.from(R.string.form_shipping_generic_error)
                }
            } else {
                FailEarly.fail("Unexpected Shopify error response", error)
                Error.from(R.string.something_went_wrong)
            }
        )
    }

    private fun onCheckoutAddressUpdated(
        shippingAddress: Address,
        billingAddress: Address,
        billingSameAsShipping: Boolean
    ) {
        addressProvider.updateShippingAddress(shippingAddress)
        addressProvider.updateBillingAddress(billingAddress)
        addressProvider.setBillingSameAsShippingAddress(billingSameAsShipping)

        ShippingBillingAnalytics.goToPayment()
        navigator.navigateBack()
    }

    class Factory @Inject constructor(
        private val sharedViewModel: CheckoutSharedViewModel,
        private val navigator: CheckoutNavigator,
        private val addressProvider: AddressProvider,
        private val shopifyClientWrapper: ShopifyClientWrapper
    ) : BaseViewModel.Factory<ShippingBillingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            ShippingBillingViewModel(
                viewState ?: ShippingBillingViewState(),
                sharedViewModel,
                navigator,
                addressProvider,
                shopifyClientWrapper
            ) as T
    }
}
