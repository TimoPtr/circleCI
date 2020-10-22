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
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

@GameScope
internal class PlaqlessSensorInteractor @Inject constructor(
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
            connection.detectors().plaqlessNotifications()
                .subscribe(
                    ::onPlaqlessData,
                    Timber::e
                ).apply {
                    sensorDisposable = this

                    addDisposable(this)
                }
        }
    }

    override fun unregisterListeners() {
        sensorDisposable.forceDispose()
    }

    override fun registerDelayableListeners() {
        // no-op
    }

    @VisibleForTesting
    fun onPlaqlessData(plaqlessSensorState: PlaqlessSensorState) {
        sensorListener.onPlaqlessData(isPlaying(), plaqlessSensorState)
    }

    private fun isAlreadySubscribed(): Boolean = sensorDisposable?.let {
        !it.isDisposed
    } ?: false
}
