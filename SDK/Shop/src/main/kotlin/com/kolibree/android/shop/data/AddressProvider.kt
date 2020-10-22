/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data

import androidx.annotation.Keep
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.shop.domain.model.Address
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import timber.log.Timber

@Keep
interface AddressProvider {

    fun observeForAddress(): Disposable

    fun getShippingAddress(): Observable<Address>

    fun getBillingAddress(): Observable<Address>

    fun getBillingSameAsShippingAddress(): Observable<Boolean>

    fun updateShippingAddress(address: Address)

    fun updateBillingAddress(address: Address)

    fun setBillingSameAsShippingAddress(same: Boolean)
}

/**
 * Extremely simple in-mem version of address holder, because
 * we cannot save addresses to database for legal reasons.
 */
internal class InMemoryAddressProvider @Inject constructor(
    private val kolibreeConnector: IKolibreeConnector,
    private val currentProfileProvider: CurrentProfileProvider
) : AddressProvider {

    private var shippingAddress: Address? = null
    private var billingAddress: Address? = null
    private var billingSameAsShippingAddress = AtomicBoolean(true)

    private val shippingAddressRelay = BehaviorRelay.create<Address>()
    private val billingAddressRelay = BehaviorRelay.create<Address>()
    private val billingSameAsShippingAddressRelay = BehaviorRelay.create<Boolean>().also {
        it.accept(billingSameAsShippingAddress.get())
    }

    override fun observeForAddress(): Disposable {
        return currentProfileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .subscribe(::onProfileLoaded, Timber::e)
    }

    override fun getShippingAddress(): Observable<Address> =
        shippingAddressRelay.hide().distinctUntilChanged()

    override fun getBillingAddress(): Observable<Address> =
        billingAddressRelay.hide().distinctUntilChanged()

    override fun getBillingSameAsShippingAddress(): Observable<Boolean> =
        billingSameAsShippingAddressRelay.hide().distinctUntilChanged()

    override fun updateShippingAddress(address: Address) {
        synchronized(this) {
            shippingAddress = address
            shippingAddressRelay.accept(address)
            if (billingSameAsShippingAddress.get()) {
                billingAddressRelay.accept(address)
            }
        }
    }

    override fun updateBillingAddress(address: Address) {
        synchronized(this) {
            billingAddress = address
            if (!billingSameAsShippingAddress.get()) {
                billingAddressRelay.accept(address)
            }
        }
    }

    override fun setBillingSameAsShippingAddress(same: Boolean) {
        if (billingSameAsShippingAddress.compareAndSet(!same, same)) {
            if (same) {
                shippingAddress?.let { billingAddressRelay.accept(it) }
            } else {
                billingAddress?.let { billingAddressRelay.accept(it) }
            }
            billingSameAsShippingAddressRelay.accept(same)
        }
    }

    private fun onProfileLoaded(profile: Profile) {
        val address = Address(
            firstName = profile.firstName,
            country = profile.country,
            email = kolibreeConnector.email
        )

        if (shippingAddress == null || shippingAddress?.isEmpty() == true) {
            updateShippingAddress(address)
        }

        if (billingAddress == null || billingAddress?.isEmpty() == true) {
            updateBillingAddress(address)
        }
    }
}
