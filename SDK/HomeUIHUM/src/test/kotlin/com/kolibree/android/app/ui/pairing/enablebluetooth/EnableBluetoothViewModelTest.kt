/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.enablebluetooth

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class EnableBluetoothViewModelTest : BaseUnitTest() {
    private val pairingNavigator: PairingNavigator = mock()
    private val bluetoothUtils: IBluetoothUtils = mock()
    private val pairingFlowSharedFacade: PairingFlowSharedFacade = mock()

    private val testScheduler = TestScheduler()

    private lateinit var viewModel: EnableBluetoothViewModel

    @Test
    fun `viewmodel subscribes to bluetooth state changes with delay in onResume and unsubscribes on pause`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false, pushToOnResume = false)

        assertFalse(bluetoothStateSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        assertFalse(bluetoothStateSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_START)

        assertFalse(bluetoothStateSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        assertFalse(bluetoothStateSubject.hasObservers())

        advanceTimeBluetoothSubscription()

        assertTrue(bluetoothStateSubject.hasObservers())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_PAUSE)

        assertFalse(bluetoothStateSubject.hasObservers())
    }

    @Test
    fun `viewModel navigates to next screen as soon as bluetooth is enabled`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        bluetoothStateSubject.onNext(false)

        verifyNoMoreInteractions(pairingNavigator)

        bluetoothStateSubject.onNext(true)

        verify(pairingNavigator).navigateFromEnableBluetoothToWakeYourBrush()
    }

    @Test
    fun `onEnableBluetoothClicked emits RequestBluetoothPermission and hide the error`() {
        init(popToScanListOnSuccess = false)

        val observer = viewModel.actionsObservable.test().assertEmpty()

        viewModel.onEnableBluetoothClicked()

        verify(pairingFlowSharedFacade).hideError()
        observer.assertValue(EnableBluetoothActions.RequestBluetoothPermission)
    }

    /*
    bluetoothPermissionDenied
     */
    @Test
    fun `bluetoothPermissionDenied shows an Error to the user`() {
        init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionDenied()

        verify(pairingFlowSharedFacade).showError(Error.from(R.string.pairing_enable_bluetooth))
    }

    /*
    bluetoothPermissionGranted
     */
    @Test
    fun `bluetoothPermissionGranted requests to enable bluetooth and listens to bluetooth state changes`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionGranted()

        assertTrue(bluetoothStateSubject.hasObservers())

        verify(bluetoothUtils).enableBluetooth(true)
    }

    @Test
    fun `bluetoothPermissionGranted navigates to WakeYourBrush after bluetooth reports enabled and popToScanListOnSuccess = false`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionGranted()

        verify(pairingNavigator, never()).navigateFromEnableBluetoothToWakeYourBrush()

        bluetoothStateSubject.onNext(false)

        verify(pairingNavigator, never()).navigateFromEnableBluetoothToWakeYourBrush()

        bluetoothStateSubject.onNext(true)

        verify(pairingNavigator).navigateFromEnableBluetoothToWakeYourBrush()

        verify(pairingNavigator, never()).popToScanList()
    }

    @Test
    fun `bluetoothPermissionGranted navigates to scanList after bluetooth reports enabled and popToScanListOnSuccess = true`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = true)

        viewModel.onBluetoothPermissionGranted()

        verify(pairingNavigator, never()).popToScanList()

        bluetoothStateSubject.onNext(false)

        verify(pairingNavigator, never()).popToScanList()

        bluetoothStateSubject.onNext(true)

        verify(pairingNavigator).popToScanList()

        verify(pairingNavigator, never()).navigateFromEnableBluetoothToWakeYourBrush()
    }

    @Test
    fun `bluetoothPermissionGranted sends Activate event after bluetooth reports enabled `() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionGranted()

        verify(eventTracker, never()).sendEvent(EnableBluetoothAnalytics.activate())

        bluetoothStateSubject.onNext(false)

        verify(eventTracker, never()).sendEvent(EnableBluetoothAnalytics.activate())

        bluetoothStateSubject.onNext(true)

        verify(eventTracker).sendEvent(EnableBluetoothAnalytics.activate())
    }

    @Test
    fun `bluetoothPermissionGranted doesn't process duplicate bluetooth state events`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionGranted()

        bluetoothStateSubject.onNext(true)
        bluetoothStateSubject.onNext(true)

        verify(eventTracker).sendEvent(EnableBluetoothAnalytics.activate())
    }

    @Test
    fun `bluetoothPermissionGranted hide the error if bluetooth is enable`() {
        val bluetoothStateSubject = init(popToScanListOnSuccess = false)

        viewModel.onBluetoothPermissionGranted()

        bluetoothStateSubject.onNext(true)

        verify(pairingFlowSharedFacade).hideError()
    }

    /*
    Utils
     */

    private fun init(
        popToScanListOnSuccess: Boolean,
        pushToOnResume: Boolean = true
    ): PublishSubject<Boolean> {
        viewModel = EnableBluetoothViewModel(
            initialViewState = null,
            pairingNavigator = pairingNavigator,
            bluetoothUtils = bluetoothUtils,
            pairingFlowSharedFacade = pairingFlowSharedFacade,
            popToScanListOnSuccess = popToScanListOnSuccess,
            delayScheduler = testScheduler
        )

        val bluetoothSubject = bluetoothSubject()
        if (pushToOnResume) {
            viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

            advanceTimeBluetoothSubscription()
        }

        return bluetoothSubject
    }

    private fun bluetoothSubject(): PublishSubject<Boolean> {
        val bluetoothStateSubject = PublishSubject.create<Boolean>()
        whenever(bluetoothUtils.bluetoothStateObservable()).thenReturn(bluetoothStateSubject)
        return bluetoothStateSubject
    }

    private fun advanceTimeBluetoothSubscription() {
        testScheduler.advanceTimeBy(2000, TimeUnit.MILLISECONDS)
    }
}
