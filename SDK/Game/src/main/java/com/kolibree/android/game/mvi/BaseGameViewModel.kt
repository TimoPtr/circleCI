/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.mvi

import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.google.common.base.Optional
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.extensions.runOnMainThread
import com.kolibree.android.game.KeepScreenOnController
import com.kolibree.android.game.lifecycle.GameLifecycle
import com.kolibree.android.game.sensors.interactors.GameToothbrushInteractorFacade
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.emitsVibrationStateAfterLostConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTING
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_ACTIVE
import com.kolibree.android.sdk.disconnection.LostConnectionHandler.State.CONNECTION_LOST
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Keep
abstract class BaseGameViewModel<GVS : BaseGameViewState>(
    baseViewState: GVS,
    private val macAddress: Optional<String>,
    protected val gameInteractor: GameInteractor,
    protected val facade: GameToothbrushInteractorFacade,
    private val lostConnectionHandler: LostConnectionHandler,
    protected val keepScreenOnController: KeepScreenOnController
) :
    BaseViewModel<GVS, BaseGameAction>(baseViewState), GameInteractor.Listener {

    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        gameInteractor.setLifecycleOwner(owner)
        gameInteractor.addListener(this)
    }

    @CallSuper
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop(::subscribeGameLifeCycle)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        if (macAddress.isPresent) {
            macAddress.get().let {
                disposeOnPause {
                    subscribeToLostConnection(it)
                }
            }
        }
    }

    private fun subscribeToLostConnection(macAddress: String): Disposable {
        return lostConnectionHandler.connectionObservable(macAddress)
            .switchMap { newState ->
                maybeForceVibratorOffOnConnectionActive(newState)
                    .andThen(Observable.just(newState))
            }
            .subscribe(
                { newState ->
                    connectionWeCareAbout(gameInteractor.connection)?.let { connection ->
                        pushAction(ConnectionHandlerStateChanged(newState))

                        notifyLostConnectionStateChanged(connection, newState)
                    }
                },
                Timber::e
            )
    }

    /**
     * If Polling for vibration state is not supported by TB + FW, we won't be able to notify
     * whether TB is vibrating or not after it transitions to ACTIVE. Thus, we need to show pause
     * screen. The user will have to resume or stop&resume vibration.
     *
     * As of May 2020, the only consumer toothbrush that doesn't support polling is M1, which is
     * still on FW 1.4.0
     *
     * In principle, all other consumer TBs are on FWs that support polling and a vibrator changed
     * event will follow after a connection becomes ACTIVE
     *
     * @see <a href="https://docs.google.com/spreadsheets/d/1XyTFLQrZ9D2PUrbRZ-eSqEkjjRnL588rzCylTWBTOl0/edit#gid=506526620&range=A15">BLE spreadsheet</a>
     */
    @VisibleForTesting
    fun maybeForceVibratorOffOnConnectionActive(
        state: LostConnectionHandler.State
    ): Completable {
        return Completable.defer {
            if (state == CONNECTION_ACTIVE) {
                connectionWeCareAbout(gameInteractor.connection)?.let { connection ->
                    if (!connection.emitsVibrationStateAfterLostConnection()) {
                        onVibratorOff(connection)

                        return@defer connection.vibrator().off()
                    }
                }
            }

            Completable.complete()
        }
    }

    @VisibleForTesting
    fun notifyLostConnectionStateChanged(
        connection: KLTBConnection,
        it: LostConnectionHandler.State
    ) {
        // invoke alien method on main thread to avoid potential deadlocks
        {
            onLostConnectionHandleStateChanged(connection, it)
        }.runOnMainThread()
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        gameInteractor.removeListener(this)
        super.onDestroy(owner)
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        gameInteractor.removeListener(this)
    }

    @CallSuper
    override fun onConnectionEstablished() {
        connectionWeCareAbout()?.let { connection ->
            onConnectionEstablished(connection)

            facade.onConnectionEstablished(connection)
        }
    }

    abstract fun onConnectionEstablished(connection: KLTBConnection)

    protected fun connectionWeCareAbout(connection: KLTBConnection? = gameInteractor.connection): KLTBConnection? =
        if (macAddress.isPresent && connection.mac() == macAddress.get()) connection else null

    private inline fun withConnectionWeCareAbout(
        connection: KLTBConnection? = gameInteractor.connection,
        execute: (KLTBConnection) -> Unit
    ) {
        connectionWeCareAbout(connection)?.let { execute(it) }
    }

    /**
     * Will be called after Start state has been reach
     */
    abstract fun onLostConnectionHandleStateChanged(
        connection: KLTBConnection,
        state: LostConnectionHandler.State
    )

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        // no-op
    }

    @CallSuper
    override fun onVibratorOn(connection: KLTBConnection) {
        pushAction(VibratorStateChanged(true))
    }

    @CallSuper
    override fun onVibratorOff(connection: KLTBConnection) {
        getViewState()?.lostConnectionState?.let { state ->
            if (state == CONNECTION_ACTIVE) {
                pushAction(VibratorStateChanged(false))
            } else {
                pushAction(ConnectionHandlerStateChanged(state))
            }
        }
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        // no-op - probably
    }

    override fun onKolibreeServiceDisconnected() {
        // no-op - probably
    }

    fun resumeGame() {
        withConnectionWeCareAbout {
            disposeOnDestroy {
                it.vibrator().on().subscribeOn(Schedulers.io())
                    .subscribe({}, Timber::e)
            }
        }
    }

    @VisibleForTesting
    fun subscribeGameLifeCycle(): Disposable {
        return facade.gameLifeCycleObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                ::onGameLifecycleEvent,
                Timber::e
            )
    }

    @VisibleForTesting
    fun onGameLifecycleEvent(lifecycleState: GameLifecycle) {
        when (lifecycleState) {
            GameLifecycle.Started,
            GameLifecycle.Resumed,
            GameLifecycle.Restarted -> {
                keepScreenOnController.keepScreenOn()
            }
            GameLifecycle.Paused -> {
                keepScreenOnController.allowScreenOff()
            }
            else -> {
                // no-op
            }
        }
    }
}

@Keep
fun KLTBConnection.lostConnectionState(): LostConnectionHandler.State =
    when (state().current) {
        KLTBConnectionState.ACTIVE -> CONNECTION_ACTIVE
        KLTBConnectionState.ESTABLISHING -> CONNECTING
        else -> CONNECTION_LOST
    }
