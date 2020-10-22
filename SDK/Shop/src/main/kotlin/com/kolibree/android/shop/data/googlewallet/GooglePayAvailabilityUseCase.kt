/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.GooglePayFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.shop.data.googlewallet.requests.IsReadyToPayRequestUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Caches if Google Pay is available for the current active session
 *
 * Internally, it invokes <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#isReadyToPay(com.google.android.gms.wallet.IsReadyToPayRequest)">IsReadyToPayRequest</a>
 */
internal interface GooglePayAvailabilityUseCase : DefaultLifecycleObserver {
    fun startWatch()
    fun isGooglePayAvailable(): Boolean
}

internal class GooglePayAvailabilityUseCaseImpl
@Inject constructor(
    activity: AppCompatActivity,
    private val cartRepository: CartRepository,
    private val isReadyToPayRequestUseCase: IsReadyToPayRequestUseCase,
    private val sessionFlags: SessionFlags,
    private val featureToggles: FeatureToggleSet
) : GooglePayAvailabilityUseCase {

    private val lifecycle: Lifecycle = activity.lifecycle

    @VisibleForTesting
    var disposable: Disposable? = null

    private val isGooglePayActivated: Boolean by lazy {
        featureToggles.toggleForFeature(GooglePayFeature).value
    }

    override fun startWatch() {
        if (!isGooglePayActivated)
            return

        lifecycle.addObserver(this)

        checkAvailabilityOnProductAdded()
    }

    /**
     * On first invocation, checks if Google Pay is available and caches the result scoped to the
     * user session
     *
     * This method is not thread safe. Multiple invocations will result on multiple checks of
     * Google Pay availability
     *
     * @return [Boolean] true if Google Pay is available, false otherwise
     */
    private fun checkAvailabilityOnProductAdded() {
        if (shouldCheckAvailability()) {
            disposable = cartHasProductsCompletable()
                .andThen(checkAvailabilityAndCacheResultSingle())
                .doFinally { disposable = null }
                .subscribe(
                    {
                        Timber.d(
                            "Cached google pay availability %s",
                            readGooglePaySessionAvailability()
                        )
                    },
                    Timber::e
                )
        }
    }

    /**
     * @return [Completable] that completes when the cart has at least 1 product
     */
    private fun cartHasProductsCompletable(): Completable {
        return cartRepository.getCartProductsCount()
            .subscribeOn(Schedulers.io())
            .filter { count -> count > 0 }
            .take(1)
            .ignoreElements()
    }

    private fun shouldCheckAvailability() =
        disposable == null && readGooglePaySessionAvailability() == null

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        disposable.forceDispose()
    }

    /**
     * @return [Boolean] true if Google Pay is available, false otherwise
     */
    override fun isGooglePayAvailable(): Boolean =
        isGooglePayActivated &&
            readGooglePaySessionAvailability() ?: false

    /**
     * @return [Boolean] if there's a value stored for user's session, null if no value has been set
     */
    private fun readGooglePaySessionAvailability() =
        sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY)

    private fun checkAvailabilityAndCacheResultSingle(): Single<Boolean> {
        return Single.defer { isReadyToPayRequestUseCase.isReadyToPayRequest() }
            .doOnSuccess { sessionFlags.setSessionFlag(IS_GPAY_AVAILABLE_KEY, it) }
    }
}

private const val IS_GPAY_AVAILABLE_KEY = "is_gpay_available"
