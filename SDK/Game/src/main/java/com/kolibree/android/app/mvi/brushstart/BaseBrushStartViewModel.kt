/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.mvi.brushstart

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.interactor.GameInteractor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.KolibreeService
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Keep
abstract class BaseBrushStartViewModel<A : BaseAction>(
    viewState: BrushStartViewState,
    private val gameInteractor: GameInteractor
) : BaseViewModel<BrushStartViewState, A>(viewState), GameInteractor.Listener {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        gameInteractor.setLifecycleOwner(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        registerListeners()
    }

    override fun onStop(owner: LifecycleOwner) {
        unregisterListeners()
        super.onStop(owner)
    }

    @VisibleForTesting
    public override fun onCleared() {
        super.onCleared()
        unregisterListeners()
    }

    fun registerListeners() {
        gameInteractor.addListener(this)
    }

    fun unregisterListeners() {
        gameInteractor.removeListener(this)
    }

    override fun onConnectionEstablished() {
        // no-op
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        // no-op
    }

    override fun onVibratorOn(connection: KLTBConnection) {
        executeIfTheConnectionIsRight(getViewState(), connection) { _, state ->
            gameInteractor.removeListener(this@BaseBrushStartViewModel)
            onBrushStarted(state)
        }
    }

    abstract fun onBrushStarted(state: BrushStartViewState)

    override fun onVibratorOff(connection: KLTBConnection) {
        // no-op
    }

    override fun onKolibreeServiceConnected(service: KolibreeService) {
        getViewState()?.let { state ->
            executeIfTheConnectionIsRight(
                state,
                service.getConnection(state.mac)
            ) { connection, _ ->
                with(connection.vibrator()) {
                    if (isOn)
                        disposeOnCleared {
                            off().subscribeOn(Schedulers.io()).subscribe({ }, Timber::w)
                        }
                }
            }
        }
    }

    override fun onKolibreeServiceDisconnected() {
        // no-op
    }

    private inline fun executeIfTheConnectionIsRight(
        viewState: BrushStartViewState? = getViewState(),
        connection: KLTBConnection?,
        call: (KLTBConnection, BrushStartViewState) -> Unit
    ) {
        viewState?.let { state ->
            connection?.toothbrush()?.let { brush ->
                if (state.mac == brush.mac && state.model == brush.model) call(connection, state)
            }
        }
    }
}
