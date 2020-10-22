/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.app.ui.pairing.BlinkingConnectionHolder
import com.kolibree.android.app.ui.pairing.PairingFlowHost
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.finishPairingFlow
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.app.ui.pairing.usecases.NextNavigationActionUseCase
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.tracker.Analytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

/**
 * ViewModel for Brush Found screen
 *
 * User can only reach this screen after a toothbrush has been paired (stored in
 * [BlinkingConnectionHolder])
 *
 * We don't listen to Bluetooth or Location state because it doesn't matter at this point. Even if
 * bluetooth was switched off, the toothbrush will still be associated when completing [PairingFlowHost].
 */
internal class BrushFoundViewModel(
    pairingFlowSharedFacade: PairingFlowSharedFacade,
    private val navigator: PairingNavigator,
    private val brushFoundConfirmConnectionUseCase: BrushFoundConfirmConnectionUseCase,
    private val timeoutScheduler: Scheduler,
    private val nextNavigationActionUseCase: NextNavigationActionUseCase
) : BaseViewModel<EmptyBaseViewState, BaseAction>(EmptyBaseViewState),
    PairingFlowSharedFacade by pairingFlowSharedFacade {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        if (blinkingConnection() == null) {
            onNoBlinkingConnection()
        }
    }

    private fun onNoBlinkingConnection() = navigator.navigateFromBrushFoundToWakeYourBrush()

    fun confirmConnectionClick() {
        Analytics.send(BrushFoundAnalytics.connect())
        showProgress(true)
        disposeOnCleared {
            Completable.timer(
                SHOW_FAKE_CONNECTING_DIALOG_SECONDS,
                TimeUnit.SECONDS,
                timeoutScheduler
            )
                .andThen(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
                .doFinally { showProgress(false) }
                .subscribe(
                    ::onConnectionConfirmed,
                    ::onConfirmConnectionError
                )
        }
    }

    private fun onConnectionConfirmed() {
        when (nextNavigationActionUseCase.nextNavitationStep()) {
            MODEL_MISMATCH -> navigator.navigateToToothbrushModelMismatch()
            SIGN_UP -> navigator.navigateFromBrushFoundToSignUp()
            FINISH -> finishPairingFlow(navigator)
            NO_BLINKING_CONNECTION -> onNoBlinkingConnection()
        }
    }

    fun notRightConnectionClick() {
        disposeOnCleared(::unpairBlinkingConnection)

        Analytics.send(BrushFoundAnalytics.notRightToothbrush())

        navigator.navigateToScanList()
    }

    private fun unpairBlinkingConnection(): Disposable {
        return unpairBlinkingConnectionCompletable()
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    // no-op
                },
                Timber::e
            )
    }

    private fun onConfirmConnectionError(throwable: Throwable) {
        Timber.e(throwable)

        showError(Error.from(R.string.pairing_something_went_wrong))

        navigator.navigateFromBrushFoundToWakeYourBrush()
    }

    class Factory @Inject constructor(
        private val pairingFlowSharedFacade: PairingFlowSharedFacade,
        private val navigator: PairingNavigator,
        private val brushFoundConfirmConnectionUseCase: BrushFoundConfirmConnectionUseCase,
        @SingleThreadScheduler private val timeoutScheduler: Scheduler,
        private val nextNavigationActionUseCase: NextNavigationActionUseCase
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BrushFoundViewModel(
                pairingFlowSharedFacade = pairingFlowSharedFacade,
                navigator = navigator,
                brushFoundConfirmConnectionUseCase = brushFoundConfirmConnectionUseCase,
                nextNavigationActionUseCase = nextNavigationActionUseCase,
                timeoutScheduler = timeoutScheduler
            ) as T
    }
}

@VisibleForTesting
const val SHOW_FAKE_CONNECTING_DIALOG_SECONDS = 2L
