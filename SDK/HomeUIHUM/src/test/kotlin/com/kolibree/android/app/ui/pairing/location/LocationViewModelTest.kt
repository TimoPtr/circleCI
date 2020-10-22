/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.location.EnableLocation
import com.kolibree.android.location.LocationAction
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.location.NoAction
import com.kolibree.android.sdk.location.LocationStatusListener
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Test

internal class LocationViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: LocationViewModel
    private val pairingNavigator: PairingNavigator = mock()
    private val locationNavigator: LocationNavigator = mock()
    private val pairingFlowSharedFacade: PairingFlowSharedFacade = mock()
    private val locationStatus: LocationStatus = mock()
    private val locationStatusListener: LocationStatusListener = mock()

    @Test
    fun `onGrantLocationPermission emits action RequestLocationPermission if permission not granted`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.shouldAskPermission()).thenReturn(true)
        val actions = viewModel.actionsObservable.test()

        viewModel.onGrantLocationPermission()

        actions.assertValue(LocationActions.RequestLocationPermission)
    }

    @Test
    fun `onGrantLocationPermission navigates to WakeYourBrush screen if permission already granted and popToScanListOnSuccess = false`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        viewModel.onGrantLocationPermission()

        verify(pairingNavigator).navigateFromLocationToWakeYourBrush()

        verify(pairingNavigator, never()).popToScanList()
    }

    @Test
    fun `onGrantLocationPermission navigates to ScanList screen if permission already granted and popToScanListOnSuccess = true`() {
        init(popToScanListOnSuccess = true)

        whenever(locationStatus.shouldAskPermission()).thenReturn(false)

        viewModel.onGrantLocationPermission()

        verify(pairingNavigator).popToScanList()

        verify(pairingNavigator, never()).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `onEnableLocationClick navigates to LocationSettings screen if location not enabled`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)

        viewModel.onEnableLocationClick()

        verify(locationNavigator).navigateToLocationSettings()
    }

    @Test
    fun `onLocationPermissionGranted navigates to WakeYourBrush screen if popToScanListOnSuccess=false`() {
        init(popToScanListOnSuccess = false)

        viewModel.onLocationPermissionGranted()

        verify(pairingNavigator).navigateFromLocationToWakeYourBrush()
        verify(pairingNavigator, never()).popToScanList()
    }

    @Test
    fun `onLocationPermissionGranted pops to ScanList screen if popToScanListOnSuccess=true`() {
        init(popToScanListOnSuccess = true)

        viewModel.onLocationPermissionGranted()

        verify(pairingNavigator).popToScanList()
        verify(pairingNavigator, never()).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `onLocationPermissionGranted sends locationPermissionGranted event`() {
        init(popToScanListOnSuccess = false)

        viewModel.onLocationPermissionGranted()

        verify(eventTracker).sendEvent(LocationAnalytics.locationPermissionGranted())
    }

    @Test
    fun `onLocationPermissionDenied shows error if app is in foreground`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = true

        viewModel.onLocationPermissionDenied()

        val expectedError = Error.from(R.string.pairing_grant_location_permission_error)
        verify(pairingFlowSharedFacade).showError(expectedError)
    }

    @Test
    fun `onLocationPermissionDenied doesn't show error if app is in background`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = false

        viewModel.onLocationPermissionDenied()

        val expectedError = Error.from(R.string.pairing_grant_location_permission_error)
        verify(pairingFlowSharedFacade, never()).showError(expectedError)
    }

    @Test
    fun `onLocationDisabled shows error if app is in foreground`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = true

        viewModel.onLocationDisabled()

        val expectedError = Error.from(R.string.pairing_enable_location)
        verify(pairingFlowSharedFacade).showError(expectedError)
    }

    @Test
    fun `onLocationDisabled doesn't show error if app is in background`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = false

        viewModel.onLocationDisabled()

        val expectedError = Error.from(R.string.pairing_enable_location)
        verify(pairingFlowSharedFacade, never()).showError(expectedError)
    }

    @Test
    fun `onLocationEnabled navigates to WakeYourBrush screen`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = true
        viewModel.onLocationEnabled()

        verify(pairingNavigator).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `onLocationEnabled sends locationEnabled event`() {
        init(popToScanListOnSuccess = false)

        viewModel.isForeground = true
        viewModel.onLocationEnabled()

        verify(eventTracker).sendEvent(LocationAnalytics.locationEnabled())
    }

    @Test
    // proceedWhenLocationReady will deal with this
    fun `onLocationSettingsClose never navigates to WakeYourBrush screen if location is enabled`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.isReadyToScan()).thenReturn(true)

        viewModel.isForeground = true
        viewModel.onLocationSettingsClose()

        verify(pairingNavigator, never()).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `onLocationSettingsClose shows error if location is disabled`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.isReadyToScan()).thenReturn(false)

        viewModel.isForeground = true
        viewModel.onLocationSettingsClose()

        val expectedError = Error.from(R.string.pairing_enable_location)
        verify(pairingFlowSharedFacade).showError(expectedError)
    }

    @Test
    fun `onLocationActionClick should hide the error`() {
        init(popToScanListOnSuccess = false)

        viewModel.onLocationActionClick()

        verify(pairingFlowSharedFacade).hideError()
    }

    @Test
    fun `when location is enabled when the screen is displayed and we haven't already invoked navigate, then automatically navigate to next screen`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.getLocationAction()).thenReturn(NoAction)

        mockLocationStatusListener()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker).sendEvent(LocationAnalytics.locationEnabled())
        verify(pairingNavigator).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `when location is enabled after the screen is displayed, then automatically navigate to next screen`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.getLocationAction()).thenReturn(EnableLocation)

        val locationActionSubject = mockLocationStatusListener()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(eventTracker, never()).sendEvent(LocationAnalytics.locationEnabled())
        verify(pairingNavigator, never()).navigateFromLocationToWakeYourBrush()

        locationActionSubject.onNext(NoAction)

        verify(eventTracker).sendEvent(LocationAnalytics.locationEnabled())
        verify(pairingNavigator).navigateFromLocationToWakeYourBrush()
    }

    @Test
    fun `when location is enabled when the screen is displayed and we already invoked navigate, then never navigate to next screen`() {
        init(popToScanListOnSuccess = false)

        whenever(locationStatus.getLocationAction()).thenReturn(NoAction)

        mockLocationStatusListener()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        viewModel.onLocationEnabled()

        verify(pairingNavigator, never()).navigateFromLocationToWakeYourBrush()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(pairingNavigator, times(1)).navigateFromLocationToWakeYourBrush()
    }

    /*
    Utils
     */

    private fun init(popToScanListOnSuccess: Boolean) {
        viewModel = LocationViewModel(
            initialViewState = LocationViewState(LocationScreenType.EnableLocation),
            pairingNavigator = pairingNavigator,
            locationNavigator = locationNavigator,
            pairingFlowSharedFacade = pairingFlowSharedFacade,
            locationStatus = locationStatus,
            screenType = LocationScreenType.GrantLocationPermission,
            popToScanListOnSuccess = popToScanListOnSuccess,
            locationStatusListener = locationStatusListener
        )
    }

    private fun mockLocationStatusListener(): PublishSubject<LocationAction> {
        val subject = PublishSubject.create<LocationAction>()
        whenever(locationStatusListener.locationActionStream())
            .thenReturn(subject)

        return subject
    }
}
