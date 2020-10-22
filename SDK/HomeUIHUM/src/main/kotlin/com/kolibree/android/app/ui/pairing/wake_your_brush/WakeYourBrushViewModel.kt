/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.wake_your_brush

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.BluetoothDisabled
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.ConnectionAllowed
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationPermissionNotGranted
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationServiceDisabled
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

internal class WakeYourBrushViewModel(
    initialViewState: WakeYourBrushViewState?,
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val navigator: PairingNavigator,
    private val wakeYourBrushPrerequisitesUseCase: WakeYourBrushPrerequisitesUseCase,
    private val bluetoothUtils: IBluetoothUtils,
    private val blinkFirstScanResultUseCase: BlinkFirstScanResultUseCase,
    private val timeoutScheduler: Scheduler,
    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase
) : BaseViewModel<WakeYourBrushViewState, BaseAction>(
    initialViewState ?: WakeYourBrushViewState.initial()
), PairingFlowSharedFacade by pairingFlowSharedFacade {

    val showNothingIsHappening: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.showNothingHappening ?: false
    }

    @VisibleForTesting
    var isForeground: Boolean = false

    @VisibleForTesting
    var scanningDisposable: Disposable? = null

    private val startScanRelay = PublishRelay.create<Boolean>()

    @VisibleForTesting
    val postponeNothingHappeningRelay = PublishRelay.create<Boolean>()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        wakeYourBrushPrerequisitesUseCase.validateOrNavigate()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        isForeground = true

        disposeOnPause(::startScan)

        disposeOnPause(::showNothingHappening)

        disposeOnPause(::listenToPrerequisitesChanges)
    }

    private fun listenToPrerequisitesChanges(): Disposable =
        checkConnectionPrerequisitesUseCase.checkOnceAndStream()
            .subscribeOn(Schedulers.io())
            .distinctUntilChanged()
            .subscribe(
                ::onPrerequisiteStateChanged,
                Timber::e
            )

    private fun onPrerequisiteStateChanged(newState: ConnectionPrerequisitesState) {
        when (newState) {
            BluetoothDisabled,
            LocationServiceDisabled,
            LocationPermissionNotGranted -> wakeYourBrushPrerequisitesUseCase.validateOrNavigate()
            ConnectionAllowed -> {
                // no-op
            }
        }
    }

    private fun showNothingHappening(): Disposable {
        return showNothingHappeningCompletable()
            .doOnSubscribe { updateViewState { copy(showNothingHappening = false) } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                updateViewState { copy(showNothingHappening = true) }
            }, Timber::e)
    }

    override fun onPause(owner: LifecycleOwner) {
        isForeground = false

        stopScanning()

        super.onPause(owner)
    }

    override fun onCleared() {
        try {
            // We need this to block or it'll be disposed in super.onCleared
            unpairBlinkingConnectionCompletable().blockingAwait()
        } finally {
            super.onCleared()
        }
    }

    fun onNothingHappeningClick() {
        navigator.navigateToIsBrushReady()
    }

    @VisibleForTesting
    fun startScan(): Disposable =
        Completable.fromAction { stopScanning() }
            .andThen(startScanObservable())
            .switchMap {
                unpairBlinkingConnectionCompletable()
                    .andThen(startScanOnBluetoothReady())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onBlinkEvent, this::onBlinkError)
            .also { disposable -> scanningDisposable = disposable }

    @VisibleForTesting
    fun showNothingHappeningCompletable(): Completable = postponeNothingHappeningRelay
        .startWith(true)
        .switchMapSingle {
            Single.timer(
                SHOW_NOTHING_HAPPENING_AFTER_SECONDS,
                TimeUnit.SECONDS,
                timeoutScheduler
            )
        }
        .take(1)
        .ignoreElements()

    private fun startScanOnBluetoothReady(): Observable<BlinkEvent> {
        return bluetoothUtils
            .bluetoothStateObservable()
            .startWith(bluetoothUtils.isBluetoothEnabled)
            .switchMap { blinkObservable() }
    }

    private fun startScanObservable(): Observable<Boolean> {
        return startScanRelay
            .subscribeOn(Schedulers.io())
            .startWith(true)
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun blinkObservable(): Observable<BlinkEvent> {
        return Observable.defer {
            return@defer if (canStartScanning()) {
                // forgetPendingToothbrushes()

                blinkFirstScanResultUseCase.blinkFirstScanResult()
            } else {
                Observable.empty()
            }
        }
    }

    @VisibleForTesting
    fun canStartScanning() = isForeground && bluetoothUtils.isBluetoothEnabled

    private fun onBlinkEvent(blinkEvent: BlinkEvent) {
        postponeNothingHappeningRelay.accept(true)

        when (blinkEvent) {
            BlinkEvent.InProgress -> {
                // no-op
            }
            is BlinkEvent.Success -> onBlinkSuccess(blinkEvent)
            is BlinkEvent.Error -> onBlinkError(blinkEvent.throwable)
            is BlinkEvent.Timeout -> onBlinkTimeout()
        }
    }

    private fun onBlinkSuccess(blinkEvent: BlinkEvent.Success) {
        setBlinkingConnection(blinkEvent.connection)

        navigator.navigateToBrushFound()
    }

    private fun onBlinkError(throwable: Throwable) {
        Timber.e(throwable)

        updateViewState { copy(showNothingHappening = true) }
    }

    private fun onBlinkTimeout() {
        updateViewState { copy(showNothingHappening = true) }
        restartScan()
    }

    private fun restartScan() {
        startScanRelay.accept(true)
    }

    private fun stopScanning() {
        scanningDisposable.forceDispose()
        scanningDisposable = null
    }

    class Factory @Inject constructor(
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val navigator: PairingNavigator,
        private val wakeYourBrushPrerequisitesUseCase: WakeYourBrushPrerequisitesUseCase,
        private val blinkFirstScanResultUseCase: BlinkFirstScanResultUseCase,
        private val bluetoothUtils: IBluetoothUtils,
        @SingleThreadScheduler private val timeoutScheduler: Scheduler,
        private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase
    ) : BaseViewModel.Factory<WakeYourBrushViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WakeYourBrushViewModel(
                initialViewState = viewState,
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator,
                bluetoothUtils = bluetoothUtils,
                blinkFirstScanResultUseCase = blinkFirstScanResultUseCase,
                wakeYourBrushPrerequisitesUseCase = wakeYourBrushPrerequisitesUseCase,
                checkConnectionPrerequisitesUseCase = checkConnectionPrerequisitesUseCase,
                timeoutScheduler = timeoutScheduler
            ) as T
    }
}

@VisibleForTesting
const val SHOW_NOTHING_HAPPENING_AFTER_SECONDS = 10L
