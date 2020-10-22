/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.snackbar

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Disabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Enabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.PermissionDenied
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Unknown
import com.kolibree.android.app.ui.toolbartoothbrush.MultiToothbrushDisconnected
import com.kolibree.android.app.ui.toolbartoothbrush.NoLocation
import com.kolibree.android.app.ui.toolbartoothbrush.NoService
import com.kolibree.android.app.ui.toolbartoothbrush.NoToothbrushes
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnecting
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewState
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class SnackbarLocationViewModelTest : BaseUnitTest() {

    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()

    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel = mock()

    private val locationStatus: LocationStatus = mock()

    private val sessionFlags: SessionFlags = mock()

    private lateinit var viewModel: SnackbarLocationViewModel

    override fun setup() {
        super.setup()

        whenever(sessionFlags.readSessionFlag(SESSION_FLAG_LOCATION)).thenReturn(true)

        viewModel =
            SnackbarLocationViewModel(
                LocationPopupViewState.initial(),
                toothbrushConnectionStateViewModel,
                checkConnectionPrerequisitesUseCase,
                sessionFlags,
                locationStatus
            )
    }

    @Test
    fun `startLocationSnackbarChecker emits true if state is NoLocation and pairing state is LocationServiceDisabled`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoLocation(1, "")))
        )

        whenever(checkConnectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(
            Observable.just(ConnectionPrerequisitesState.LocationServiceDisabled)
        )

        viewModel.startLocationSnackbarChecker()
            .test()
            .assertValue(true)
    }

    @Test
    fun `startLocationSnackbarChecker emits false if session flag is false`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoLocation(1, "")))
        )

        whenever(checkConnectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(
            Observable.just(ConnectionPrerequisitesState.LocationServiceDisabled)
        )

        whenever(sessionFlags.readSessionFlag(SESSION_FLAG_LOCATION)).thenReturn(false)

        viewModel.startLocationSnackbarChecker()
            .test()
            .assertValue(false)
    }

    @Test
    fun `startLocationSnackbarChecker emits true if state is NoLocation and pairing state is LocationPermissionNotGranted`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(ToothbrushConnectionStateViewState(NoLocation(1, "")))
        )

        whenever(checkConnectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(
            Observable.just(ConnectionPrerequisitesState.LocationPermissionNotGranted)
        )

        viewModel.startLocationSnackbarChecker()
            .test()
            .assertValue(true)
    }

    @Test
    fun `startLocationSnackbarChecker emits the right values`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(NoLocation(toothbrushes = 1, mac = ""))
            )
        )

        whenever(checkConnectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(
            Observable.just(
                ConnectionPrerequisitesState.BluetoothDisabled,
                ConnectionPrerequisitesState.LocationPermissionNotGranted,
                ConnectionPrerequisitesState.ConnectionAllowed,
                ConnectionPrerequisitesState.LocationServiceDisabled
            )
        )

        viewModel.startLocationSnackbarChecker()
            .test()
            .assertValues(false, true, false, true)
    }

    @Test
    fun `startLocationSnackbarChecker emits false if state is not NoLocation and pairing state is correct`() {

        whenever(toothbrushConnectionStateViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                ToothbrushConnectionStateViewState(NoService(toothbrushes = 1, mac = "")),
                ToothbrushConnectionStateViewState(MultiToothbrushDisconnected(macs = listOf(""))),
                ToothbrushConnectionStateViewState(SingleToothbrushConnecting(mac = "")),
                ToothbrushConnectionStateViewState(NoToothbrushes(toothbrushes = 1))
            )
        )

        whenever(checkConnectionPrerequisitesUseCase.checkOnceAndStream()).thenReturn(
            Observable.just(ConnectionPrerequisitesState.LocationPermissionNotGranted)
        )

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        viewModel.startLocationSnackbarChecker()
            .test()
            .assertValue(false)
    }

    @Test
    fun `onLocationDismiss should set the session flag to false`() {
        viewModel.onLocationDismiss()

        verify(sessionFlags).setSessionFlag(SESSION_FLAG_LOCATION, false)
    }

    @Test
    fun `onLocationPermissionRetrieved should update the right state if permission is denied`() {
        viewModel.onLocationPermissionRetrieved(false)

        assertEquals(PermissionDenied, viewModel.getViewState()!!.locationState)
    }

    @Test
    fun `onLocationPermissionRetrieved should update the right state if permission is granted and location status should be enabled`() {
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)

        viewModel.onLocationPermissionRetrieved(true)

        assertEquals(Disabled, viewModel.getViewState()!!.locationState)
    }

    @Test
    fun `onLocationPermissionRetrieved should update the right state if permission is granted and location is enabled `() {
        whenever(locationStatus.shouldEnableLocation()).thenReturn(false)

        viewModel.onLocationPermissionRetrieved(true)

        assertEquals(Enabled, viewModel.getViewState()!!.locationState)
    }

    @Test
    fun `onLocationPermissionRetrieved should reset the location to unknown before updating a new item`() {
        whenever(locationStatus.shouldEnableLocation()).thenReturn(true)

        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onLocationPermissionRetrieved(true)

        testObserver.assertValues(
            LocationPopupViewState(Unknown),
            LocationPopupViewState(Disabled)
        )

        viewModel.onLocationPermissionRetrieved(true)

        testObserver.assertValues(
            LocationPopupViewState(Unknown),
            LocationPopupViewState(Disabled),
            LocationPopupViewState(Unknown),
            LocationPopupViewState(Disabled)
        )
    }
}

private const val SESSION_FLAG_LOCATION = "should_notify_location_needed"
