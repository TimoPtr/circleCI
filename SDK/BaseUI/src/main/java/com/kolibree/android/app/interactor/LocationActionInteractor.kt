/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.interactor

import android.os.Bundle
import androidx.annotation.Keep
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.location.LocationActionChecker
import com.kolibree.android.interactor.LifecycleAwareInteractor
import com.kolibree.android.sdk.core.KolibreeService
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

/**
 * @author lookashc
 * @date 04/04/19
 */
@VisibleForApp
class LocationActionInteractor @Inject constructor(
    private val kolibreeServiceInteractor: KolibreeServiceInteractor,
    private val locationActionChecker: LocationActionChecker
) : LifecycleAwareInteractor<LocationActionInteractor.Listener>(), KolibreeServiceInteractor.Listener {

    /**
     * Public listener interface
     */
    @Keep
    interface Listener {

        fun onEnableLocationActionNeeded()
    }

    private var disposable: Disposable? = null

    override fun setLifecycleOwnerInternal(lifecycleOwner: LifecycleOwner) {
        // Because we have composition of interactors, we need to pass the same owner to
        // kolibreeServiceInteractor - and we need to do that before setting our own,
        // so kolibreeServiceInteractor's create() will call first.
        kolibreeServiceInteractor.setLifecycleOwner(lifecycleOwner)
        super.setLifecycleOwnerInternal(lifecycleOwner)
    }

    override fun onCreateInternal(savedInstanceState: Bundle?) {
        super.onCreateInternal(savedInstanceState)
        kolibreeServiceInteractor.addListener(this)
    }

    override fun onDestroyInternal() {
        kolibreeServiceInteractor.removeListener(this)
        disposable?.dispose()
        super.onDestroyInternal()
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        maybeRequireEnableLocationAction()
    }

    override fun onKolibreeServiceDisconnected() {
        disposable?.dispose()
    }

    /** Given a KolibreeService, ask if there's any action required to enable location  */
    private fun maybeRequireEnableLocationAction() {
        disposable = locationActionChecker
            .enableLocationActionSingle()
            .filter { enableLocationAction ->
                enableLocationAction.requestEnableLocation || enableLocationAction.requestPermission
            }
            .onTerminateDetach()
            .subscribe(
                { forEachListener { listener -> listener.onEnableLocationActionNeeded() } },
                { e -> Timber.e(e) }
            )
    }
}
