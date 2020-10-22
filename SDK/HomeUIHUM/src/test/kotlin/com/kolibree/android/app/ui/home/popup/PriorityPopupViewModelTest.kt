/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup

import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import com.google.common.base.Optional
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Bluetooth
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Location
import com.kolibree.android.app.ui.home.popup.DisplayedItem.LocationPermissionError
import com.kolibree.android.app.ui.home.popup.KLSnackbarItem.SnackbarBluetoothItem
import com.kolibree.android.app.ui.home.popup.KLSnackbarItem.SnackbarLocationItem
import com.kolibree.android.app.ui.home.popup.snackbar.LocationPopupViewState
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Disabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Enabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.PermissionDenied
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarBluetoothViewModel
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarLocationViewModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.KLQueue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import junit.framework.Assert.assertEquals
import org.junit.Test

class PriorityPopupViewModelTest : BaseUnitTest() {

    private val navigator: HumHomeNavigator = mock()

    private val priorityQueue: KLQueue = mock()

    private val snackbarLocationViewModel: SnackbarLocationViewModel = mock()

    private val snackbarBluetoothViewModel: SnackbarBluetoothViewModel = mock()

    private lateinit var viewModelSnackbarsDisplay: SnackbarsPriorityDisplayViewModel

    override fun setup() {
        super.setup()

        viewModelSnackbarsDisplay =
            SnackbarsPriorityDisplayViewModel(
                SnackbarsPriorityDisplayViewState.initial(),
                snackbarLocationViewModel,
                snackbarBluetoothViewModel,
                priorityQueue,
                navigator
            )
    }

    @Test
    fun `bluetooth snackbar should be shown when priority queue digest a SnackbarBluetoothItem item`() {
        mockDefaultStreams()

        whenever(priorityQueue.stream()).thenReturn(
            Observable.just<Optional<KLItem>>(Optional.of(SnackbarBluetoothItem))
        )

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)

        val expectedConfiguration = SnackbarConfiguration(
            isShown = true,
            error = Error.from(
                messageId = R.string.home_bluetooth_unavailable_snackbar,
                buttonTextId = R.string.home_snackbar_enable_button
            )
        )

        assertEquals(expectedConfiguration, viewModelSnackbarsDisplay.getViewState()!!.snackbarConfiguration)
    }

    @Test
    fun `location snackbar should be shown when priority queue digest a SnackbarBluetoothItem item`() {
        mockDefaultStreams()

        whenever(priorityQueue.stream()).thenReturn(
            Observable.just<Optional<KLItem>>(Optional.of(SnackbarLocationItem))
        )

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)

        val expectedConfiguration = SnackbarConfiguration(
            isShown = true,
            error = Error.from(
                messageId = R.string.home_location_unavailable_snackbar,
                buttonTextId = R.string.home_snackbar_enable_button
            )
        )

        assertEquals(expectedConfiguration, viewModelSnackbarsDisplay.getViewState()!!.snackbarConfiguration)
    }

    @Test
    fun `snackbar location click call location navigator`() {
        mockDefaultStreams()

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)
        viewModelSnackbarsDisplay.updateViewState { withDisplayedItem(Location) }
        viewModelSnackbarsDisplay.onSnackBarClicked()

        verify(navigator).launchLocationPermission()
    }

    @Test
    fun `snackbar location dismiss set session flag to false`() {
        mockDefaultStreams()

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)
        viewModelSnackbarsDisplay.updateViewState { withDisplayedItem(Location) }
        viewModelSnackbarsDisplay.onSnackBarDismissed()

        verify(priorityQueue).consume(SnackbarLocationItem)
    }

    @Test
    fun `snackbar bluetooth dismiss set session flag to false`() {
        mockDefaultStreams()

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)
        viewModelSnackbarsDisplay.updateViewState { withDisplayedItem(Bluetooth) }
        viewModelSnackbarsDisplay.onSnackBarDismissed()

        verify(priorityQueue).consume(SnackbarBluetoothItem)
    }

    @Test
    fun `snackbar bluetooth click call bluetooth navigator`() {
        mockDefaultStreams()

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_RESUME)
        viewModelSnackbarsDisplay.updateViewState { withDisplayedItem(Bluetooth) }
        viewModelSnackbarsDisplay.onSnackBarClicked()

        verify(navigator).launchBluetoothPermission()
    }

    @Test
    fun `onLocationStateChanged navigate to location settings if location is disabled`() {
        mockDefaultStreams()

        whenever(snackbarLocationViewModel.viewStateFlowable).thenReturn(
            Flowable.just(
                LocationPopupViewState(
                    locationState = Disabled
                )
            )
        )

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_CREATE)

        verify(navigator).navigateToLocationSettings()
        verify(priorityQueue, never()).consume(any())
    }

    @Test
    fun `onLocationStateChanged consume the item if permission is granted and location is already enabled`() {
        mockDefaultStreams()

        whenever(snackbarLocationViewModel.viewStateFlowable).thenReturn(
            Flowable.just(LocationPopupViewState(locationState = Enabled))
        )

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_CREATE)

        verifyZeroInteractions(navigator)
        verify(priorityQueue).consume(SnackbarLocationItem)
    }

    @Test
    fun `onLocationStateChanged consume the item, set the session flag to false and show a snackbar if the permission is denied`() {
        mockDefaultStreams()

        whenever(snackbarLocationViewModel.viewStateFlowable).thenReturn(
            Flowable.just(LocationPopupViewState(locationState = PermissionDenied))
        )

        viewModelSnackbarsDisplay.pushLifecycleTo(ON_CREATE)

        verify(priorityQueue).consume(SnackbarLocationItem)
        assertEquals(LocationPermissionError, viewModelSnackbarsDisplay.getViewState()!!.displayedItem)
    }

    @Test
    fun `onLocationSettingsClosed consume the item`() {

        viewModelSnackbarsDisplay.onLocationSettingsClosed()

        verify(priorityQueue).consume(SnackbarLocationItem)
    }

    private fun mockDefaultStreams() {
        whenever(snackbarBluetoothViewModel.startBluetoothSnackbarChecker()).thenReturn(Flowable.empty())
        whenever(snackbarLocationViewModel.startLocationSnackbarChecker()).thenReturn(Flowable.empty())
        whenever(priorityQueue.stream()).thenReturn(Observable.empty())
        whenever(snackbarLocationViewModel.viewStateFlowable).thenReturn(Flowable.empty())
    }
}
