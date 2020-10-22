/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import androidx.annotation.VisibleForTesting
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.callSafelyIfActive
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

@GameScope
internal class PlaqlessRawDataSensorInteractor @Inject constructor(
    private val sensorListener: GameSensorListener,
    gameLifecycleProvider: GameLifecycleProvider,
    gameToothbrushEventProvider: GameToothbrushEventProvider
) : BaseSensorInteractor(
    gameLifecycleProvider,
    gameToothbrushEventProvider
) {
    @VisibleForTesting
    var sensorDisposable: Disposable? = null

    override fun registerListeners() {
        if (!isAlreadySubscribed()) {
            connection.callSafelyIfActive {
                detectors().plaqlessRawDataNotifications()
                    .subscribe(
                        ::onPlaqlessRawData,
                        Timber::e
                    ).apply {
                        sensorDisposable = this

                        addDisposable(this)
                    }
            }
        }
    }

    private fun isAlreadySubscribed(): Boolean {
        return sensorDisposable?.let {
            !it.isDisposed
        } ?: false
    }

    @VisibleForTesting
    fun onPlaqlessRawData(sensorState: PlaqlessRawSensorState) {
        sensorListener.onPlaqlessRawData(isPlaying(), sensorState)
    }

    override fun unregisterListeners() {
        sensorDisposable.forceDispose()
    }

    override fun registerDelayableListeners() {
        // no-op
    }
}
