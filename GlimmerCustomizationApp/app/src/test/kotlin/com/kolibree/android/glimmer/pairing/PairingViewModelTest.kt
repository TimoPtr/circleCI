/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.glimmer.GlimmerApplication
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.test.extensions.assertLastValueWithPredicate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.pairing.assistant.PairingAssistant
import com.kolibree.pairing.session.PairingSession
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

/** [PairingViewModel] unit tests */
internal class PairingViewModelTest : BaseUnitTest() {

    private val app: GlimmerApplication = mock()

    private val pairingAssistant: PairingAssistant = mock()

    private val navigator: PairingNavigator = mock()

    private val bluetoothUtils: IBluetoothUtils = mock()

    private val locationUtils: LocationStatus = mock()

    private lateinit var viewModel: PairingViewModel

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel = PairingViewModel(
            initialViewState = PairingViewState.initial(),
            pairingAssistant = pairingAssistant,
            navigator = navigator,
            bluetoothUtils = bluetoothUtils,
            locationUtils = locationUtils,
            app = app
        )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onStart
     */

    @Test
    fun `onStart enables bluetooth`() {
        viewModel.onStart(mock())

        verify(bluetoothUtils).enableBluetooth(true)
    }

    /*
    showResultsLiveData
     */

    @Test
    fun `showResultsLiveData emits false when isConnecting is true`() {
        val testObserver = viewModel.showResultsLiveData.test()

        viewModel.updateViewState { copy(isConnecting = true) }

        testObserver.assertValue(false)
    }

    @Test
    fun `showResultsLiveData emits false when scan result list is empty`() {
        val testObserver = viewModel.showResultsLiveData.test()

        viewModel.updateViewState {
            copy(isConnecting = false, scanResults = listOf())
        }

        testObserver.assertValue(false)
    }

    @Test
    fun `showResultsLiveData emits true when scan result list is not empty`() {
        val testObserver = viewModel.showResultsLiveData.test()

        viewModel.updateViewState {
            copy(isConnecting = false, scanResults = listOf(mock()))
        }

        testObserver.assertValue(true)
    }

    /*
    scanResultListLiveData
     */

    @Test
    fun `scanResultListLiveData filters Glint devices`() {
        val glintResult = createScanResult(GLINT)

        val testObserver = viewModel.scanResultListLiveData.test()

        viewModel.onScanResults(
            listOf(
                glintResult,
                createScanResult(CONNECT_E2),
                createScanResult(CONNECT_B1)
            )
        )

        testObserver.assertValue(listOf(glintResult))
    }

    /*
    onScanResultSelected
     */

    @Test
    fun `onScanResultSelected pairs scan result`() {
        val expectedScanResult = mock<ToothbrushScanResult>()
        val expectedConnection = mock<KLTBConnection>()

        val pairingSession = mock<PairingSession>()
        whenever(pairingSession.connection()).thenReturn(expectedConnection)

        whenever(pairingAssistant.pair(expectedScanResult))
            .thenReturn(Single.just(pairingSession))

        viewModel.onScanResultSelected(expectedScanResult)

        verify(pairingAssistant).pair(expectedScanResult)
    }

    @Test
    fun `onScanResultSelected emits isConnecting = false when toothbrush is paired`() {
        val expectedConnection = mock<KLTBConnection>()

        val pairingSession = mock<PairingSession>()
        whenever(pairingSession.connection()).thenReturn(expectedConnection)

        whenever(pairingAssistant.pair(any()))
            .thenReturn(Single.just(pairingSession))

        val testObserver = viewModel.viewStateFlowable.test()

        viewModel.onScanResultSelected(mock())

        testObserver.assertNoErrors().assertLastValueWithPredicate { !it.isConnecting }
    }

    @Test
    fun `onScanResultSelected invokes navigator's navigateToTweakerActivity() after buildWithConnectionComponent`() {
        val expectedConnection = KLTBConnectionBuilder
            .createAndroidLess()
            .withBrushingMode()
            .build()

        val pairingSession = mock<PairingSession>()
        whenever(pairingSession.connection()).thenReturn(expectedConnection)

        whenever(pairingAssistant.pair(any())).thenReturn(Single.just(pairingSession))

        viewModel.onScanResultSelected(createScanResult())

        inOrder(app, navigator) {
            verify(app).buildWithConnectionComponent(expectedConnection)
            verify(navigator).navigateToTweakerActivity()
        }
    }

    /*
    onBluetoothPermissionState
     */

    @Test
    fun `onBluetoothPermissionState invokes askForBluetoothPermission when not granted`() {
        viewModel.onBluetoothPermissionState(false)

        verify(navigator).askForBluetoothPermission()
    }

    @Test
    fun `onBluetoothPermissionState invokes askForLocationPermission when granted`() {
        viewModel.onBluetoothPermissionState(true)

        verify(navigator).askForLocationPermission()
    }

    /*
    onLocationPermissionState
     */

    @Test
    fun `onLocationPermissionState invokes askForLocationPermission when not granted`() {
        viewModel.onLocationPermissionState(false)

        verify(navigator).askForLocationPermission()
    }

    @Test
    fun `onLocationPermissionState invokes navigateToLocationSettings when granted but not enabled`() {
        whenever(locationUtils.shouldEnableLocation()).thenReturn(true)

        viewModel.onLocationPermissionState(true)

        verify(navigator).navigateToLocationSettings()
    }

    @Test
    fun `onLocationPermissionState starts scanning when granted and enabled`() {
        whenever(locationUtils.shouldEnableLocation()).thenReturn(false)
        whenever(pairingAssistant.realTimeScannerObservable()).thenReturn(Observable.never())

        viewModel.onLocationPermissionState(true)

        verify(pairingAssistant).realTimeScannerObservable()
    }

    /*
    Utils
     */

    private fun createScanResult(toothbrushModel: ToothbrushModel = GLINT) =
        mock<ToothbrushScanResult>().apply {
            whenever(model).thenReturn(toothbrushModel)
        }
}
