/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.location.LocationAnalytics.locationEnabled
import com.kolibree.android.app.ui.pairing.location.LocationAnalytics.locationPermissionGranted
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.NoAction
import com.kolibree.android.sdk.location.LocationStatusListener
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

internal class LocationViewModel(
    initialViewState: LocationViewState?,
    private val pairingNavigator: PairingNavigator,
    private val locationNavigator: LocationNavigator,
    private val pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val locationStatus: LocationStatus,
    private val screenType: LocationScreenType,
    private val popToScanListOnSuccess: Boolean,
    private val locationStatusListener: LocationStatusListener
) : BaseViewModel<LocationViewState, LocationActions>(
    initialViewState ?: LocationViewState.initial(screenType)
) {

    private var navigatedToNextScreen = false

    @VisibleForTesting
    var isForeground = false

    private var pendingForegroundAction: () -> Unit = {}

    val title: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.screenType?.title ?: R.string.empty
    }

    val description: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.screenType?.description ?: R.string.empty
    }

    val action: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.screenType?.action ?: R.string.empty
    }

    val icon: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.screenType?.icon
    }

    fun onLocationActionClick() {
        pairingFlowSharedFacade.hideError()
        when (screenType) {
            LocationScreenType.GrantLocationPermission -> onGrantLocationPermission()
            LocationScreenType.EnableLocation -> onEnableLocationClick()
        }
    }

    @VisibleForTesting
    fun onGrantLocationPermission() = when {
        locationStatus.shouldAskPermission() -> pushAction(LocationActions.RequestLocationPermission)
        else -> onLocationPermissionGranted()
    }

    @VisibleForTesting
    fun onEnableLocationClick() = when {
        locationStatus.shouldEnableLocation() -> locationNavigator.navigateToLocationSettings()
        else -> onLocationEnabled()
    }

    fun onLocationPermissionGranted() {
        Analytics.send(locationPermissionGranted())

        navigateToNextScreen()
    }

    fun onLocationPermissionDenied() = whenResumed {
        pairingFlowSharedFacade.showError(Error.from(R.string.pairing_grant_location_permission_error))
    }

    fun onLocationSettingsClose() {
        if (!locationStatus.isReadyToScan()) {
            onLocationDisabled()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        isForeground = true
        pendingForegroundAction()
        pendingForegroundAction = {}

        disposeOnPause(::proceedWhenLocationReady)
    }

    private fun proceedWhenLocationReady(): Disposable {
        return locationStatusListener.locationActionStream()
            .startWith(locationStatus.getLocationAction())
            .filter { it is NoAction }
            .take(1)
            .subscribe(
                { onLocationEnabled() },
                Timber::e
            )
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        isForeground = false
    }

    @VisibleForTesting
    fun onLocationDisabled() = whenResumed {
        pairingFlowSharedFacade.showError(Error.from(R.string.pairing_enable_location))
    }

    @VisibleForTesting
    fun onLocationEnabled() = whenResumed {
        if (!navigatedToNextScreen) {
            Analytics.send(locationEnabled())

            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        navigatedToNextScreen = true

        if (popToScanListOnSuccess) {
            pairingNavigator.popToScanList()
        } else {
            pairingNavigator.navigateFromLocationToWakeYourBrush()
        }
    }

    /**
     *  Action block will be invoke immediately if fragment is already resumed or just after onResume.
     * Method is needed for:
     * - displaying error by [pushAction] after rejecting location permission,
     * because [Activity.onRequestPermissionsResult] method is invoked before onResume
     *
     * - navigating to next screen after enabling Location in Location System Settings,
     * because navigating to next screen doesn't work when it's invoked immediately
     * after [Activity.onActivityResult] method
     */
    private fun whenResumed(action: () -> Unit) {
        if (isForeground) {
            action()
        } else {
            pendingForegroundAction = action
        }
    }

    class Factory @Inject constructor(
        private val pairingNavigator: PairingNavigator,
        private val locationNavigator: LocationNavigator,
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val locationStatus: LocationStatus,
        private val screenType: LocationScreenType,
        private val popToScanListOnSuccess: Boolean,
        private val locationStatusListener: LocationStatusListener
    ) : BaseViewModel.Factory<LocationViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LocationViewModel(
                initialViewState = viewState,
                pairingNavigator = pairingNavigator,
                locationNavigator = locationNavigator,
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                locationStatus = locationStatus,
                popToScanListOnSuccess = popToScanListOnSuccess,
                locationStatusListener = locationStatusListener,
                screenType = screenType
            ) as T
    }
}
