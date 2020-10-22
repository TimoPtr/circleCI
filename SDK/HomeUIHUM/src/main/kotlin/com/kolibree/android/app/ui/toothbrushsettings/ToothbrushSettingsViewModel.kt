/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.account.utils.ToothbrushForgetter
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionData
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushDetailsClickableItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.BrushNameItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.binding.ToothbrushSettingsItemBindingModel
import com.kolibree.android.app.ui.toothbrushsettings.usecase.RenameToothbrushNameUseCase
import com.kolibree.android.app.ui.toothbrushsettings.usecase.UpdateIfDirtyUseCase
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.offlinebrushings.sync.LastSyncData
import com.kolibree.android.offlinebrushings.sync.LastSyncObservable
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.shop.BR
import com.kolibree.android.toothbrush.battery.domain.BatteryLevel
import com.kolibree.android.toothbrush.battery.domain.BatteryLevelUseCase
import com.kolibree.android.toothbrushupdate.OtaChecker
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.pairing.assistant.PairingAssistant
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.OnItemBindModel
import timber.log.Timber

@Suppress("LargeClass")
internal class ToothbrushSettingsViewModel(
    initialViewState: ToothbrushSettingsViewState?,
    private val navigator: ToothbrushSettingsNavigator,
    private val toothbrushRepository: ToothbrushRepository,
    private val lastSyncObservable: LastSyncObservable,
    private val toothbrushMac: String,
    private val updateIfDirtyUseCase: UpdateIfDirtyUseCase,
    private val serviceProvider: ServiceProvider,
    private val pairingAssistant: PairingAssistant,
    private val timeScheduler: Scheduler,
    private val brushHeadConditionUseCase: BrushHeadConditionUseCase,
    private val renameToothbrushNameUseCase: RenameToothbrushNameUseCase,
    private val toothbrushForgetter: ToothbrushForgetter,
    private val otaChecker: OtaChecker,
    private val batteryUseCase: BatteryLevelUseCase
) : BaseViewModel<ToothbrushSettingsViewState, ToothbrushSettingsActions>(
    initialViewState ?: ToothbrushSettingsViewState.initial(toothbrushMac)
), ToothbrushSettingsInteraction, ConnectionStateListener {

    @VisibleForTesting
    var connection: KLTBConnection? = null
    private var timerDisposable: Disposable? = null

    val items: LiveData<List<ToothbrushSettingsItemBindingModel>> =
        map(viewStateLiveData) { viewState ->
            viewState?.items() ?: emptyList()
        }

    val itemsBinding = object : OnItemBindModel<ToothbrushSettingsItemBindingModel>() {
        override fun onItemBind(
            itemBinding: ItemBinding<*>,
            position: Int,
            item: ToothbrushSettingsItemBindingModel?
        ) {
            super.onItemBind(itemBinding, position, item)
            itemBinding.bindExtra(BR.interaction, this@ToothbrushSettingsViewModel)
        }
    }

    val adapter = BindingRecyclerViewAdapter<ToothbrushSettingsItemBindingModel>()

    fun onCloseClick() {
        navigator.finishScreen()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        disposeOnStop(::observeToothbrushDetails)
        disposeOnStop(::observeLastSyncData)
        disposeOnStop(::connectService)
        disposeOnStop(::fetchHeadCondition)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        updateViewState {
            copy(otaUpdateType = null)
        }
        disposeOnPause { checkForOta() }
    }

    private fun connectService(): Disposable = serviceProvider.connectStream()
        .filter { it is ServiceConnected }
        .map { (it as ServiceConnected).service }
        .toFlowable(BackpressureStrategy.BUFFER)
        .subscribeOn(Schedulers.io())
        .switchMap(::maybeUpdateDirtyConnection)
        .observeOn(AndroidSchedulers.mainThread())
        .doFinally { unregisterConnectionState() }
        .subscribe(::refreshConnection, Timber::e)

    private fun refreshConnection(service: KolibreeService) {
        unregisterConnectionState()
        connection = service.getConnection(toothbrushMac)
        connection?.let { onConnectionStateChanged(it, it.state().current) }
        registerConnectionState()
    }

    private fun maybeUpdateDirtyConnection(service: KolibreeService): Flowable<KolibreeService> =
        Completable.defer {
            service.getConnection(toothbrushMac)?.takeIf { !it.toothbrush().isRunningBootloader }
                ?.let { connection ->
                    updateIfDirtyUseCase.maybeUpdate(connection)
                        .doOnError(Timber::e)
                        .onErrorComplete()
                } ?: Completable.complete()
        }.andThen(Flowable.just(service))

    private fun unregisterConnectionState() = connection?.state()?.unregister(this)

    private fun registerConnectionState() = connection?.state()?.register(this)

    private fun observeToothbrushDetails(): Disposable =
        toothbrushRepository.readAccountToothbrush(toothbrushMac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::refreshToothbrushDetails, Timber::e)

    private fun observeLastSyncData(): Disposable = lastSyncObservable.observable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .filter { it.toothbrushMac == toothbrushMac }
        .subscribe(::refreshLastSyncDate, Timber::e)

    private fun refreshLastSyncDate(data: LastSyncData) {
        updateViewState { withLastSyncData(data) }
    }

    private fun refreshToothbrushDetails(toothbrush: AccountToothbrush) {
        updateViewState {
            copy(
                toothbrushName = toothbrush.name,
                model = toothbrush.model.commercialName,
                serial = toothbrush.serial,
                hardware = toothbrush.hardwareVersion.toString(),
                hasDsp = toothbrush.model.hasDsp
            )
                .withBootloaderVersion(toothbrush.bootloaderVersion)
                .withFirmwareVersion(toothbrush.firmwareVersion)
                .withDspVersion(toothbrush.dspVersion)
        }
    }

    private fun fetchHeadCondition(): Disposable = brushHeadConditionUseCase
        .headCondition(toothbrushMac)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::refreshHeadCondition, Timber::e)

    private fun refreshHeadCondition(headConditionData: BrushHeadConditionData) {
        updateViewState { withBrushHeadConditionData(headConditionData) }
    }

    override fun onOTAClick() {
        ToothbrushSettingsAnalytics.ota()
        val isMandatoryOta = getViewState()?.isMandatoryOtaAvailable() ?: false
        connection?.let {
            navigator.navigateToOta(isMandatoryOta, toothbrushMac, it.toothbrush().model)
        } ?: FailEarly.fail("connection is null OTA is not possible")
    }

    override fun onConnectNewBrushClick() {
        ToothbrushSettingsAnalytics.connectNewBrush()
        getViewState()?.toothbrushName?.let {
            pushAction(ToothbrushSettingsActions.ConnectNewToothbrush(it))
        }
    }

    override fun onNotConnectingClick() {
        ToothbrushSettingsAnalytics.notConnecting()

        navigator.showNotConnectingHelpCenter()
    }

    override fun onDetailItemClick(item: BrushDetailsClickableItemBindingModel) {
        when (item) {
            is BrushNameItemBindingModel -> {
                ToothbrushSettingsAnalytics.editName()
                pushAction(ToothbrushSettingsActions.ShowEditBrushNameDialog(item.value))
            }
        }
    }

    override fun onResetCounterClick() {
        val serial = getViewState()?.serial
            ?: return FailEarly.fail("Toothbrush serial has to be known at this stage")

        ToothbrushSettingsAnalytics.resetCounter()
        disposeOnCleared {
            brushHeadConditionUseCase.resetBrushHead(toothbrushMac, serial)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(::refreshHeadCondition, Timber::e)
        }
    }

    override fun onBuyNewClick() {
        ToothbrushSettingsAnalytics.buyNew()
        navigator.navigateToShop()
    }

    override fun onHelpCenterClick() {
        ToothbrushSettingsAnalytics.help()
        navigator.showHelp()
    }

    override fun onForgetToothbrushClick() {
        ToothbrushSettingsAnalytics.forgetToothbrush()

        pushAction(ToothbrushSettingsActions.ShowForgetToothbrushDialog)
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        disposeOnCleared { checkForOta() }
        disposeOnCleared { updateBatteryLevel(connection) }
        val connectionState = toConnectionState(newState)
        startConnectingTimerIfNeeded(connectionState)
        updateViewState { copy(connectionState = connectionState) }
    }

    private fun toConnectionState(newState: KLTBConnectionState): ConnectionState = when {
        isConnected(newState) -> ConnectionState.CONNECTED
        getViewState()?.connectionState == ConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED
        else -> ConnectionState.CONNECTING
    }

    private fun startConnectingTimerIfNeeded(newState: ConnectionState) {
        if (timerDisposable == null && newState.isConnecting()) {
            timerDisposable =
                Observable.timer(MAX_CONNECTING_DURATION_SECONDS, TimeUnit.SECONDS, timeScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        updateViewState { copy(connectionState = ConnectionState.CONNECTING) }
                    }.doFinally {
                        updateViewState { copy(connectionState = ConnectionState.DISCONNECTED) }
                    }.subscribe({ disposeConnectingTimer() }, Timber::e)
            disposeOnCleared { timerDisposable }
        }
        if (newState.isConnected()) {
            disposeConnectingTimer()
        }
    }

    private fun disposeConnectingTimer() {
        timerDisposable.forceDispose()
        timerDisposable = null
    }

    private fun isConnected(state: KLTBConnectionState?): Boolean {
        return state != null && state == KLTBConnectionState.ACTIVE
    }

    fun forgetToothbrush() {
        ToothbrushSettingsAnalytics.forgetToothbrushYes()
        disposeOnCleared {
            toothbrushForgetter.forgetToothbrush(toothbrushMac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    navigator.finishScreen()
                }, ::showError)
        }
    }

    fun connectNewBrush() {
        ToothbrushSettingsAnalytics.popupForgetBrush()
        disposeOnCleared {
            toothbrushForgetter.forgetToothbrush(toothbrushMac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    navigator.navigatesToPairingScreen()
                }, ::showError)
        }
    }

    private fun updateBatteryLevel(connection: KLTBConnection) = batteryUseCase
        .batteryLevel(connection)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(::refreshBatteryLevel, Timber::e)

    private fun refreshBatteryLevel(batteryLevel: BatteryLevel) {
        updateViewState { withBatteryLevel(batteryLevel) }
    }

    fun userRenamedToothbrush(newName: String) {
        ToothbrushSettingsAnalytics.editNameSave()
        disposeOnCleared {
            renameToothbrushName(connection, newName)
        }
    }

    fun userCancelRenamedToothbrush() {
        ToothbrushSettingsAnalytics.editNameCancel()
    }

    private fun renameToothbrushName(connection: KLTBConnection?, name: String): Disposable =
        renameToothbrushNameUseCase
            .rename(connection, name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, ::showError)

    override fun onIdentifyClick() {
        ToothbrushSettingsAnalytics.identifyBrush()
        connection?.let {
            disposeOnCleared { blink(it) }
        } ?: FailEarly.fail("Click on identify Toothbrush but connection is null")
    }

    private fun blink(connection: KLTBConnection): Disposable =
        pairingAssistant.blinkBlue(connection)
            .doOnSubscribe {
                updateViewState { copy(isIdentifying = true) }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .delay(DELAY_BEFORE_BLINKING_SECOND, TimeUnit.SECONDS, timeScheduler)
            .doFinally {
                updateViewState { copy(isIdentifying = false) }
            }
            .subscribe({}, ::showError)

    private fun showError(error: Throwable) {
        Timber.e(error)
        pushAction(ToothbrushSettingsActions.SomethingWrongHappened)
    }

    private fun checkForOta(): Disposable = otaChecker.otaForConnectionsOnce()
        .subscribeOn(Schedulers.io())
        .filter {
            it.connection.mac() == toothbrushMac
        }
        .take(1)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            updateViewState {
                copy(otaUpdateType = it.otaUpdateType)
            }
        }, Timber::e)

    fun onBackPressed() {
        ToothbrushSettingsAnalytics.goBack()

        navigator.finishScreen()
    }

    class Factory @Inject constructor(
        private val navigator: ToothbrushSettingsNavigator,
        private val toothbrushRepository: ToothbrushRepository,
        private val lastSyncObservable: LastSyncObservable,
        private val toothbrushMac: String,
        private val updateIfDirtyUseCase: UpdateIfDirtyUseCase,
        private val serviceProvider: ServiceProvider,
        private val renameToothbrushNameUseCase: RenameToothbrushNameUseCase,
        private val pairingAssistant: PairingAssistant,
        @SingleThreadScheduler private val timeScheduler: Scheduler,
        private val brushHeadConditionUseCase: BrushHeadConditionUseCase,
        private val toothbrushForgetter: ToothbrushForgetter,
        private val otaChecker: OtaChecker,
        private val batteryUseCase: BatteryLevelUseCase
    ) : BaseViewModel.Factory<ToothbrushSettingsViewState>() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ToothbrushSettingsViewModel(
                initialViewState = viewState,
                navigator = navigator,
                toothbrushRepository = toothbrushRepository,
                lastSyncObservable = lastSyncObservable,
                toothbrushMac = toothbrushMac,
                serviceProvider = serviceProvider,
                pairingAssistant = pairingAssistant,
                timeScheduler = timeScheduler,
                brushHeadConditionUseCase = brushHeadConditionUseCase,
                updateIfDirtyUseCase = updateIfDirtyUseCase,
                renameToothbrushNameUseCase = renameToothbrushNameUseCase,
                toothbrushForgetter = toothbrushForgetter,
                otaChecker = otaChecker,
                batteryUseCase = batteryUseCase
            ) as T
    }
}

private const val DELAY_BEFORE_BLINKING_SECOND = 10L
private const val MAX_CONNECTING_DURATION_SECONDS = 10L
