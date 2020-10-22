/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionStateListener
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.state.KLTBConnectionState.ACTIVE
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.di.KLTBConnectionScope
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

/**
 * Observes Brushing Mode changes on [connection] and forces a synchronization of the stored
 * BrushingMode associated to the toothbrush owner
 */
@KLTBConnectionScope
internal class BrushingModeObserver @Inject constructor(
    private val connection: InternalKLTBConnection,
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCaseImpl
) : ConnectionStateListener {

    @VisibleForTesting
    var brushingModeChangedDisposable: Disposable? = null

    init {
        if (connection.toothbrush().model.supportsVibrationSpeedUpdate()) {
            connection.state().register(this)
        }
    }

    override fun onConnectionStateChanged(
        connection: KLTBConnection,
        newState: KLTBConnectionState
    ) {
        when (newState) {
            ACTIVE -> synchronizeOnNewBrushingMode()
            else -> stopListeningToBrushingModeChanges()
        }
    }

    @VisibleForTesting
    fun synchronizeOnNewBrushingMode() {
        brushingModeChangedDisposable.forceDispose()

        val brushingModeStateObserver = connection.brushingMode() as BrushingModeStateObserver

        brushingModeChangedDisposable = brushingModeStateObserver
            .brushingModeStateFlowable()
            .concatMapCompletable {
                synchronizeBrushingModeUseCase.synchronizeBrushingMode(connection)
            }
            .subscribe(
                {
                    // no-op
                },
                Timber::e
            )
    }

    @VisibleForTesting
    fun stopListeningToBrushingModeChanges() {
        brushingModeChangedDisposable.forceDispose()
    }
}
