/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.enablebluetooth

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.enablebluetooth.EnableBluetoothAnalytics.activate
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.tracker.Analytics
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import timber.log.Timber

internal class EnableBluetoothViewModel(
    initialViewState: EnableBluetoothViewState?,
    private val pairingNavigator: PairingNavigator,
    private val bluetoothUtils: IBluetoothUtils,
    private val pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val popToScanListOnSuccess: Boolean,
    private val delayScheduler: Scheduler
) : BaseViewModel<EnableBluetoothViewState, EnableBluetoothActions>(
    initialViewState ?: EnableBluetoothViewState.initial()
) {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        disposeOnPause(::proceedWhenBluetoothEnabled)
    }

    private fun proceedWhenBluetoothEnabled(): Disposable {
        return bluetoothUtils.bluetoothStateObservable()
            .delaySubscription(
                bluetoothSubscriptionDelay.toMillis(),
                TimeUnit.MILLISECONDS,
                delayScheduler
            )
            .filter { isEnabled -> isEnabled }
            .take(1)
            .subscribe(
                { onBluetoothEnabled() },
                Timber::e
            )
    }

    fun onEnableBluetoothClicked() {
        pairingFlowSharedFacade.hideError()

        pushAction(EnableBluetoothActions.RequestBluetoothPermission)
    }

    fun onBluetoothPermissionGranted() {
        bluetoothUtils.enableBluetooth(true)
    }

    private fun onBluetoothEnabled() {
        Analytics.send(activate())
        pairingFlowSharedFacade.hideError()

        if (popToScanListOnSuccess) {
            pairingNavigator.popToScanList()
        } else {
            pairingNavigator.navigateFromEnableBluetoothToWakeYourBrush()
        }
    }

    fun onBluetoothPermissionDenied() {
        pairingFlowSharedFacade.showError(Error.from(R.string.pairing_enable_bluetooth))
    }

    class Factory @Inject constructor(
        private val pairingNavigator: PairingNavigator,
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val bluetoothUtils: IBluetoothUtils,
        private val popToScanListOnSuccess: Boolean,
        @SingleThreadScheduler private val scheduler: Scheduler
    ) :
        BaseViewModel.Factory<EnableBluetoothViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EnableBluetoothViewModel(
                initialViewState = viewState,
                pairingNavigator = pairingNavigator,
                bluetoothUtils = bluetoothUtils,
                popToScanListOnSuccess = popToScanListOnSuccess,
                delayScheduler = scheduler,
                pairingFlowSharedFacade = pairingFlowSharedFacade
            ) as T
    }
}

/**
 * Subscribe to bluetooth state with delay to avoid receiving stale values
 */
@Suppress("MagicNumber")
private val bluetoothSubscriptionDelay = Duration.ofMillis(1500)
