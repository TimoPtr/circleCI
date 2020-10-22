/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.interactor

import androidx.annotation.Keep
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.interactor.LifecycleAwareInteractor
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Holder of KolibreeService connection, allows others to listen to service connection/disconnection events.
 * Logic was moved from KolibreeServiceActivity, so it could be reused both in activities and fragments.
 *
 * @author lookashc
 * @date 28/03/19
 * @see com.kolibree.android.app.ui.activity.KolibreeServiceActivity
 */
@Keep
class KolibreeServiceInteractor @Inject constructor(
    private val serviceProvider: ServiceProvider
) : LifecycleAwareInteractor<KolibreeServiceInteractor.Listener>() {

    /**
     * Public listener interface
     */
    @Keep
    interface Listener {

        /**
         * Called right after connection to service was established
         * @param service instance of connected service
         */
        fun onKolibreeServiceConnected(service: KolibreeService)

        /**
         * Called right after connection to service is gone
         */
        fun onKolibreeServiceDisconnected()
    }

    var service: KolibreeService? = null
        private set

    private val serviceStateDisposable = CompositeDisposable()

    @Deprecated(
        "This is kept for temporary compatibility and will be removed",
        ReplaceWith("none"),
        DeprecationLevel.WARNING
    )
    var shouldConnectToServiceDelegate: () -> Boolean = { true }

    override fun onStartInternal() {
        super.onStartInternal()

        Timber.d("onStartInternal in $this")

        if (shouldConnectToServiceDelegate()) {
            serviceStateDisposable.addSafely(
                serviceProvider
                    .connectStream()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onServiceConnectionChanged, Timber::e)
            )
        }
    }

    override fun onStopInternal() {
        Timber.d("onStopInternal in $this")
        serviceStateDisposable.clear()
        super.onStopInternal()
    }

    override fun onDestroyInternal() {
        serviceStateDisposable.dispose()
        super.onDestroyInternal()
    }

    private fun onServiceConnectionChanged(serviceProvisionResult: ServiceProvisionResult) {
        when (serviceProvisionResult) {
            is ServiceConnected -> onKolibreeServiceConnected(serviceProvisionResult.service)
            is ServiceDisconnected -> onKolibreeServiceDisconnected()
        }
    }

    private fun onKolibreeServiceConnected(service: KolibreeService) {
        this.service = service
        forEachListener { listener ->
            callSafely { listener.onKolibreeServiceConnected(service) }
        }
    }

    private fun onKolibreeServiceDisconnected() {
        service = null
        forEachListener { listener ->
            callSafely { listener.onKolibreeServiceDisconnected() }
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    private inline fun callSafely(toCall: () -> Unit) {
        try {
            toCall()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
