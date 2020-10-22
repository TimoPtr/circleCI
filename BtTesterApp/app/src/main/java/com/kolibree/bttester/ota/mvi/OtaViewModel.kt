/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.ota.mvi

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.ota.OTA_ACTION_COMPLETED
import com.kolibree.android.app.ui.ota.OTA_ACTION_ERROR
import com.kolibree.android.app.ui.ota.OTA_ACTION_INSTALLING
import com.kolibree.android.app.ui.ota.OTA_ACTION_REBOOTING
import com.kolibree.android.app.ui.ota.OtaUpdateViewState
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.toothbrushupdate.CheckOtaUpdatePrerequisitesUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.bttester.BR
import com.kolibree.bttester.R
import com.kolibree.bttester.ota.logic.OtaPersistentState
import com.kolibree.bttester.ota.logic.OtaUpdateLogicWrapper
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.ItemBinding
import timber.log.Timber

internal class OtaViewModel(
    initialViewState: OtaViewState,
    private val logicWrapper: OtaUpdateLogicWrapper,
    private val otaPersistentState: OtaPersistentState,
    bluetoothUtils: IBluetoothUtils
) : BaseViewModel<OtaViewState, OtaAction>(initialViewState) {

    private val disposables = CompositeDisposable()

    private var reactOnSpinnerUpdate = false

    val otaInProgress = map(viewStateLiveData) { state -> state?.otaInProgress }

    val toothbrushModels = listOf(
        // Currently only those 3 models support OTA (or at least this is the info I got)
        ToothbrushModel.CONNECT_M1,
        ToothbrushModel.CONNECT_E1,
        ToothbrushModel.ARA
    )

    val toothbrushModelsBinding =
        ItemBinding.of<ToothbrushModel>(BR.item, R.layout.item_ota_toothbrush_model)

    val toothbrushModelPosition = twoWayMap(
        viewStateLiveData,
        mapper = { state -> toothbrushModels.indexOf(state?.toothbrushModel) },
        updateHandler = {
            if (reactOnSpinnerUpdate) it?.let {
                updateViewState { copy(toothbrushModel = toothbrushModels[it]) }
            }
        }
    )

    val macAddress = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.macAddress },
        updateHandler = { updateViewState { copy(macAddress = it) } }
    )

    val currentIteration = map(viewStateLiveData) { state -> state?.currentIteration }

    val numberOfIterations = map(viewStateLiveData) { state -> state?.numberOfIterations }

    val numberOfIterationsString = twoWayMap(
        viewStateLiveData,
        mapper = { state -> state?.numberOfIterations?.toString() },
        updateHandler = { updateViewState { copy(numberOfIterations = it?.toIntOrNull()) } }
    )

    val iterationsVisible = map(viewStateLiveData) { state ->
        state?.currentIteration != null && state.numberOfIterations != null && state.otaInProgress
    }

    val startButtonEnabled = map(viewStateLiveData) { state ->
        state?.canStartOta() == true
    }

    val statusMessage = map(viewStateLiveData) { state ->
        if (state?.bluetoothEnabled == false)
            "Please enable Bluetooth!"
        else if (state?.permissionsGranted == false)
            "Please grant all permissions!"
        else
            state?.statusMessage
    }

    val numberOfErrors = map(viewStateLiveData) { state -> state?.numberOfErrors }

    val errorsVisible = map(viewStateLiveData) { state ->
        state?.numberOfErrors != null && state.numberOfErrors > 0
    }

    private var logicWrapperDisposable: Disposable? = null

    init {
        disposables += bluetoothUtils.bluetoothStateObservable()
            .toFlowable(BackpressureStrategy.LATEST)
            .startWith(bluetoothUtils.isBluetoothEnabled)
            .doOnNext {
                updateViewState {
                    copy(
                        bluetoothEnabled = it
                    )
                }
            }.subscribe({}, Timber::e)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        reactOnSpinnerUpdate = true
        forceReloadSpinnerState()
    }

    override fun onPause(owner: LifecycleOwner) {
        reactOnSpinnerUpdate = false
        getViewState()?.let { otaPersistentState.storeState(it) }
        super.onPause(owner)
    }

    fun startClicked() {
        getViewState()?.let { otaPersistentState.storeState(it) }
        updateViewState { copy(numberOfErrors = null) }
        startOta(1)
    }

    fun scanClicked() {
        pushAction(ScanClicked)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
        cleanup()
    }

    private fun startOta(iteration: Int) {
        getViewState()?.let {
            if (it.macAddress == null) {
                updateViewState { copy(statusMessage = "Cannot perform OTA, MAC is null!") }
                return
            }

            updateViewState {
                copy(
                    currentIteration = iteration,
                    otaInProgress = true,
                    statusMessage = "Starting OTA iteration $iteration..."
                )
            }

            logicWrapperDisposable =
                logicWrapper.performOtaObservable(it.macAddress, it.toothbrushModel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ update ->
                        displayProgress(update)
                    }, { e ->
                        displayProgress(
                            OtaUpdateViewState.init(isRechargeable = it.toothbrushModel.isRechargeable())
                                .withOtaError(e.message)
                        )
                    })
        }
    }

    private fun displayProgress(state: OtaUpdateViewState) {
        val progressMessage = when (state.otaActionId) {
            OTA_ACTION_ERROR -> "Error! ${state.errorMessage}"
            OTA_ACTION_INSTALLING -> "Progress: ${state.otaActionProgressPercentage}%"
            OTA_ACTION_REBOOTING -> "Rebooting..."
            OTA_ACTION_COMPLETED -> "OTA Completed!"
            else -> ""
        }

        updateViewState { copy(statusMessage = progressMessage) }

        if (state.otaActionId == OTA_ACTION_ERROR) {
            updateViewState { copy(numberOfErrors = getViewState()?.numberOfErrors?.plus(1) ?: 1) }
        }

        if (state.otaActionId == OTA_ACTION_COMPLETED ||
            state.otaActionId == OTA_ACTION_ERROR
        ) {
            cleanup()
            val currentIteration = getViewState()?.currentIteration
            val numberOfIterations = getViewState()?.numberOfIterations
            if (currentIteration == null || numberOfIterations == null || currentIteration == numberOfIterations) {
                finishOta()
            } else {
                startOta(currentIteration + 1)
            }
        }
    }

    private fun cleanup() {
        logicWrapper.cleanup()
        logicWrapperDisposable?.dispose()
    }

    private fun finishOta() {
        updateViewState { copy(otaInProgress = false) }
    }

    private fun forceReloadSpinnerState() {
        // Spinner has one serious drawback - when it's created, it always sets its position to 0. This also happens
        // after screen rotation, screwing 2-way bindings. To overcome this, we need to disable the binding update
        // handler when we're not in resumed state (so spinner will not overwrite the value), and then force
        // reload the live data with correct value, so spinner will update itself.
        getViewState()?.let { toothbrushModelPosition.postValue(toothbrushModels.indexOf(it.toothbrushModel)) }
    }

    internal fun onDeviceSelected(scanResult: ToothbrushScanResult) {
        updateViewState { copy(toothbrushModel = scanResult.model, macAddress = scanResult.mac) }
    }

    class Factory @Inject constructor(
        private val context: Context,
        private val lifecycle: Lifecycle,
        private val otaChecker: OtaChecker,
        private val featureToggleSet: FeatureToggleSet,
        private val serviceProvider: ServiceProvider,
        private val otaPersistentState: OtaPersistentState,
        private val bluetoothUtils: IBluetoothUtils,
        private val checkOtaUpdatePrerequisitesUseCase: CheckOtaUpdatePrerequisitesUseCase
    ) : BaseViewModel.Factory<OtaViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val logicWrapper = OtaUpdateLogicWrapper(
                context.resources,
                lifecycle,
                otaChecker,
                featureToggleSet,
                serviceProvider,
                checkOtaUpdatePrerequisitesUseCase
            )
            val initialState = viewState ?: otaPersistentState.retrieveState()
            return OtaViewModel(
                initialState,
                logicWrapper,
                otaPersistentState,
                bluetoothUtils
            ) as T
        }
    }
}
