/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.list

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.finishPairingFlow
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.blink
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.goBack
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.noBrushFoundClose
import com.kolibree.android.app.ui.pairing.list.ScanToothbrushListAnalytics.noBrushFoundGetIt
import com.kolibree.android.app.ui.pairing.usecases.BlinkEvent
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.app.ui.pairing.usecases.NextNavigationActionUseCase
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.extensions.runOnMainThread
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.BluetoothDisabled
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.ConnectionAllowed
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationPermissionNotGranted
import com.kolibree.android.sdk.connection.ConnectionPrerequisitesState.LocationServiceDisabled
import com.kolibree.android.shop.BR
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

internal class ScanToothbrushListViewModel(
    initialViewState: ScanToothbrushListViewState?,
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val navigator: PairingNavigator,
    private val timeoutScheduler: Scheduler,
    private val scanListBlinkConnectionUseCase: ScanListBlinkConnectionUseCase,
    private val autoExpireScanner: AutoExpireScanner,
    private val scanListConfirmResultUseCase: ScanListConfirmResultUseCase,
    private val nextNavigationActionUseCase: NextNavigationActionUseCase,
    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
    private val scanToothbrushListPrerequisitesUseCase: ScanToothbrushListPrerequisitesUseCase
) : BaseViewModel<ScanToothbrushListViewState, ScanToothbrushListActions>(
    initialViewState ?: ScanToothbrushListViewState.initial()
),
    PairingFlowSharedFacade by pairingFlowSharedFacade,
    ScanToothbrushInteraction,
    NoBrushFoundInteraction {

    /**
     * Because VM is preserved on configuration change, this is recommended way of keeping
     * the list in a correct position between rotations.
     */
    val adapter = BindingRecyclerViewAdapter<ScanToothbrushItemBindingModel>()

    @VisibleForTesting
    var scanDisposable: Disposable? = null

    val items: LiveData<List<ScanToothbrushItemBindingModel>> =
        map(viewStateLiveData) { viewState ->
            viewState?.items ?: emptyList()
        }

    val itemsBinding = object : OnItemBindModel<ScanToothbrushItemBindingModel>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: ScanToothbrushItemBindingModel?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, this@ScanToothbrushListViewModel)
            itemBinding.bindExtra(BR.position, position)
        }
    }

    override val showNoBrushFound: LiveData<Boolean> =
        map(viewStateLiveData) { viewState ->
            viewState?.showNoBrushFound ?: false
        }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        scanToothbrushListPrerequisitesUseCase.validateOrNavigate()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        disposeOnPause(::listenToPrerequisiteState)

        disposeOnPause(::checkForEmptyScanResult)

        disposeOnPause(::scanForToothbrushes)

        showHostBackNavigation(false)
    }

    override fun onPause(owner: LifecycleOwner) {
        showHostBackNavigation(true)
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        hideError()
        super.onStop(owner)
    }

    private fun listenToPrerequisiteState() = checkConnectionPrerequisitesUseCase.checkOnceAndStream()
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
            LocationPermissionNotGranted -> scanToothbrushListPrerequisitesUseCase.validateOrNavigate()
            ConnectionAllowed -> hideError()
        }
    }

    @VisibleForTesting
    fun checkForEmptyScanResult(): Disposable = viewStateFlowable
        .switchMap { viewState ->
            if (viewState.items.isEmpty()) {
                noBrushFoundTimer().map { true }.toFlowable()
            } else {
                Flowable.just(false)
            }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::onShowNoBrushFoundUpdate, Timber::e)

    private fun noBrushFoundTimer(): Single<Long> = Single.timer(
        TIME_BEFORE_SHOWING_NO_BRUSH_FOUND_SECONDS,
        TimeUnit.SECONDS,
        timeoutScheduler
    )

    private fun onShowNoBrushFoundUpdate(showNoBrushFound: Boolean) {
        updateViewState {
            copy(showNoBrushFound = showNoBrushFound)
        }
    }

    private fun scanForToothbrushes(): Disposable {
        stopScan()

        return autoExpireScanner.scan()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onExpireScanResult,
                Timber::e
            ).apply {
                scanDisposable = this
            }
    }

    private fun onExpireScanResult(result: AutoExpireScanResult) {
        when (result) {
            is AutoExpireScanResult.Batch -> onNewScanResultBatch(result)
            AutoExpireScanResult.BluetoothOff -> onBluetoothOff()
        }
    }

    private fun onNewScanResultBatch(result: AutoExpireScanResult.Batch) {
        hideError()

        updateViewState {
            withScannedResults(
                results = result.results,
                currentBlinkingConnection = blinkingConnection()
            )
        }
    }

    override fun getItClick() {
        Analytics.send(noBrushFoundGetIt())
        onShowNoBrushFoundUpdate(false)
    }

    override fun closeClick() {
        Analytics.send(noBrushFoundClose())
        onShowNoBrushFoundUpdate(false)
    }

    override fun onItemClick(item: ScanToothbrushItemBindingModel) {
        stopScan()

        disposeOnStop {
            scanListConfirmResultUseCase.confirm(item.toothbrushScanResult) {
                showProgress(true)

                updateViewState { withBlinkProgressHidden() }
            }
                .doFinally { showProgress(false) }
                .subscribe(
                    ::onBlinkEventAfterItemClick,
                    ::onItemClickError
                )
        }
    }

    private fun onBlinkEventAfterItemClick(blinkEvent: BlinkEvent) {
        when (blinkEvent) {
            is BlinkEvent.Success -> {
                when (nextNavigationActionUseCase.nextNavitationStep()) {
                    MODEL_MISMATCH -> navigator.navigateFromScanListToModelMismatch()
                    SIGN_UP -> navigator.navigateFromScanListToSignUp()
                    FINISH -> finishPairingFlow(navigator)
                    NO_BLINKING_CONNECTION -> {
                        resumeScanningFromRxSubscription()

                        Timber.w("No blinking connection after confirmed")
                    }
                }
            }
        }
    }

    private fun onItemClickError(throwable: Throwable) {
        Timber.e(throwable)

        resumeScanningFromRxSubscription()
    }

    /**
     * Use it to avoid subscribing to a new RxStream inside error subscription
     */
    @VisibleForTesting
    fun resumeScanningFromRxSubscription() {
        {
            if (isResumed()) {
                disposeOnPause { scanForToothbrushes() }
            }
        }.runOnMainThread()
    }

    override fun onBlinkClick(item: ScanToothbrushItemBindingModel) {
        Analytics.send(blink())

        disposeOnStop { blinkToothbrush(item) }
    }

    private fun blinkToothbrush(item: ScanToothbrushItemBindingModel): Disposable {
        stopScan()

        return scanListBlinkConnectionUseCase.blink(item.toothbrushScanResult)
            .doOnSubscribe { updateViewState { withBlinkProgressHidden() } }
            .subscribeOn(Schedulers.io())
            .doFinally { resumeScanningFromRxSubscription() }
            .subscribe(
                { blinkEvent -> onBlinkEventAfterBlinkClick(item, blinkEvent) },
                Timber::e
            )
    }

    private fun onBlinkEventAfterBlinkClick(
        item: ScanToothbrushItemBindingModel,
        blinkEvent: BlinkEvent
    ) {
        updateViewState {
            withProgress(item, isBlinkProgressVisible = blinkEvent == BlinkEvent.InProgress)
        }
    }

    private fun onBluetoothOff() {
        updateViewState { copy(items = listOf()) }

        // onPrerequisiteStateChanged will deal with bluetooth off
    }

    private fun stopScan() {
        return scanDisposable.forceDispose()
    }

    fun onCloseClick() {
        Analytics.send(goBack())

        navigator.navigateFromScanListToWakeYourBrush()
    }

    class Factory @Inject constructor(
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val navigator: PairingNavigator,
        @SingleThreadScheduler private val timeoutScheduler: Scheduler,
        private val scanListBlinkConnectionUseCase: ScanListBlinkConnectionUseCase,
        private val scanListConfirmResultUseCase: ScanListConfirmResultUseCase,
        private val nextNavigationActionUseCase: NextNavigationActionUseCase,
        private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase,
        private val autoExpireScanner: AutoExpireScanner,
        private val scanToothbrushListPrerequisitesUseCase: ScanToothbrushListPrerequisitesUseCase
    ) : BaseViewModel.Factory<ScanToothbrushListViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ScanToothbrushListViewModel(
                initialViewState = viewState,
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator,
                timeoutScheduler = timeoutScheduler,
                scanListBlinkConnectionUseCase = scanListBlinkConnectionUseCase,
                scanListConfirmResultUseCase = scanListConfirmResultUseCase,
                nextNavigationActionUseCase = nextNavigationActionUseCase,
                checkConnectionPrerequisitesUseCase = checkConnectionPrerequisitesUseCase,
                scanToothbrushListPrerequisitesUseCase = scanToothbrushListPrerequisitesUseCase,
                autoExpireScanner = autoExpireScanner
            ) as T
    }
}

/*
This is equivalent to 3 Scan Windows
 */
private const val TIME_BEFORE_SHOWING_NO_BRUSH_FOUND_SECONDS = 16L
