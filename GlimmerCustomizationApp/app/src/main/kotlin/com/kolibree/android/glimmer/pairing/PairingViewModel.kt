/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.glimmer.GlimmerApplication
import com.kolibree.android.glimmer.pairing.PairingActions.ShowError
import com.kolibree.android.location.LocationStatus
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode.UserDefined
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class PairingViewModel(
    initialViewState: PairingViewState,
    private val pairingAssistant: PairingAssistant,
    private val navigator: PairingNavigator,
    private val bluetoothUtils: IBluetoothUtils,
    private val locationUtils: LocationStatus,
    private val app: GlimmerApplication
) : BaseViewModel<PairingViewState, PairingActions>(
    initialViewState
) {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        bluetoothUtils.enableBluetooth(true)
    }

    val showResultsLiveData: LiveData<Boolean> = mapNonNull(viewStateLiveData, false) { state ->
        !state.isConnecting && state.scanResults.isNotEmpty()
    }

    val scanResultListLiveData: LiveData<List<ToothbrushScanResult>> =
        mapNonNull(viewStateLiveData, listOf()) { state ->
            state.scanResults
        }

    @VisibleForTesting
    fun onScanResults(scanResults: List<ToothbrushScanResult>) = updateViewState {
        copy(scanResults = scanResults.filter { it.model == GLINT })
    }

    fun onScanResultSelected(scanResult: ToothbrushScanResult) {
        updateViewState { copy(isConnecting = true) }

        disposeOnStop {
            pairingAssistant.pair(scanResult)
                .map { it.connection() }
                .flatMap { it.brushingMode().set(UserDefined).andThen(Single.just(it)) }
                .subscribeOn(Schedulers.io())
                .subscribe(::onToothbrushPaired, ::onToothbrushPairingError)
        }
    }

    fun onBluetoothPermissionState(isGranted: Boolean) {
        if (!isGranted) {
            navigator.askForBluetoothPermission()
        } else {
            navigator.askForLocationPermission()
        }
    }

    fun onLocationPermissionState(isGranted: Boolean) {
        if (!isGranted) {
            navigator.askForLocationPermission()
        } else {
            maybeAskForLocationThenStartScan()
        }
    }

    fun onLocationSettingsClosed() {
        maybeAskForLocationThenStartScan()
    }

    private fun maybeAskForLocationThenStartScan() {
        if (locationUtils.shouldEnableLocation()) {
            navigator.navigateToLocationSettings()
        } else {
            startScan()
        }
    }

    private fun startScan() = disposeOnStop {
        pairingAssistant
            .realTimeScannerObservable()
            .subscribeOn(Schedulers.io())
            .subscribe(this::onScanResults, Timber::e)
    }

    private fun onToothbrushPaired(connection: KLTBConnection) {
        updateViewState { copy(isConnecting = false) }

        app.buildWithConnectionComponent(connection)

        navigator.navigateToTweakerActivity()
    }

    private fun onToothbrushPairingError(throwable: Throwable) {
        Timber.e(throwable)
        updateViewState { copy(isConnecting = false) }
        pushAction(ShowError(throwable))
    }

    class Factory @Inject constructor(
        private val pairingAssistant: PairingAssistant,
        private val navigator: PairingNavigator,
        private val bluetoothUtils: IBluetoothUtils,
        private val locationUtils: LocationStatus,
        private val app: GlimmerApplication
    ) : BaseViewModel.Factory<PairingViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PairingViewModel(
                initialViewState = PairingViewState.initial(),
                pairingAssistant = pairingAssistant,
                navigator = navigator,
                bluetoothUtils = bluetoothUtils,
                app = app,
                locationUtils = locationUtils
            ) as T
    }
}
