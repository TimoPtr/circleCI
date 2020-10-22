/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.snackbar

import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.toolbartoothbrush.NoLocation
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.persistence.SessionFlags.Companion.SHOULD_NOTIFY_LOCATION_NEEDED
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationPermissionNotGranted
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationServiceDisabled
import io.reactivex.BackpressureStrategy.LATEST
import io.reactivex.Flowable
import io.reactivex.Single.fromCallable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

@VisibleForApp
interface LocationPermissionCallback {
    fun onLocationPermissionRetrieved(permissionGranted: Boolean)
}

@VisibleForApp
class SnackbarLocationViewModel(
    initialViewState: LocationPopupViewState?,
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
    private val sessionFlags: SessionFlags,
    private val locationStatus: LocationStatus
) : BaseViewModel<LocationPopupViewState, HomeScreenAction>(
    initialViewState ?: LocationPopupViewState.initial()
), LocationPermissionCallback {

    /**
     * [Flowable] of [Boolean] sending `true` if the user already connected a ToothBrush
     * which need the location before being paired, or returns `false` otherwise.
     * It also depends on the sessionFlag, which returns `true` if the Snackbar
     * can be displayed and has not been dismissed
     */
    fun startLocationSnackbarChecker(): Flowable<Boolean> {
        return Flowable.combineLatest(
            getToothBrushStateFlowable(),
            checkConnectionPrerequisitesUseCase.checkOnceAndStream().toFlowable(LATEST),
            BiFunction(::isToothBrushWaitingForLocation)
        )
            .pairWithSessionFlag(SHOULD_NOTIFY_LOCATION_NEEDED)
            .map { (displayMessage, correctState) -> displayMessage && correctState }
    }

    private fun getToothBrushStateFlowable(): Flowable<ToothbrushConnectionState> =
        toothbrushConnectionStateViewModel
            .viewStateFlowable
            .map { it.state }

    private fun isToothBrushWaitingForLocation(
        connectionState: ToothbrushConnectionState,
        pairingState: ConnectionPrerequisitesState
    ): Boolean {
        return connectionState is NoLocation && connectionState.toothbrushes > 0 &&
            (pairingState == LocationPermissionNotGranted || pairingState == LocationServiceDisabled)
    }

    private fun <V> Flowable<V>.pairWithSessionFlag(sessionFlag: String): Flowable<Pair<Boolean, V>> =
        flatMapSingle { value ->
            fromCallable { sessionFlags.readSessionFlag(sessionFlag) }
                .map { sessionFlag -> sessionFlag to value }
        }

    fun onLocationDismiss() {
        sessionFlags.setSessionFlag(SHOULD_NOTIFY_LOCATION_NEEDED, false)
    }

    override fun onLocationPermissionRetrieved(permissionGranted: Boolean) {
        updateViewState { withCurrentStateUnknown() }
        updateViewState {
            when {
                !permissionGranted -> withPermissionDenied()
                locationStatus.shouldEnableLocation() -> withLocationDisabled()
                else -> withLocationEnabled()
            }
        }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
        private val sessionFlags: SessionFlags,
        private val locationStatus: LocationStatus
    ) : BaseViewModel.Factory<LocationPopupViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SnackbarLocationViewModel(
                viewState,
                toothbrushConnectionStateViewModel,
                checkConnectionPrerequisitesUseCase,
                sessionFlags,
                locationStatus
            ) as T
    }
}
