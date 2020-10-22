/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors.interactors

import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.lifecycle.GameLifecycleProvider
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.game.toothbrush.GameToothbrushEventProvider
import com.kolibree.android.sdk.connection.callSafelyIfActive
import com.kolibree.android.sdk.connection.detectors.listener.RawDetectorListener
import com.kolibree.android.utils.callSafely
import javax.inject.Inject

@GameScope
internal class RawSensorInteractor @Inject constructor(
    private val sensorListener: GameSensorListener,
    gameLifecycleProvider: GameLifecycleProvider,
    gameToothbrushEventProvider: GameToothbrushEventProvider
) : BaseSensorInteractor(
    gameLifecycleProvider,
    gameToothbrushEventProvider
) {

    @VisibleForTesting
    var rawDetectorListener: RawDetectorListener? = null

    @VisibleForTesting
    var registeredRawDetectorListener: Boolean = false

    override fun registerListeners() {
        setupRawDataListener()
    }

    @VisibleForTesting
    fun setupRawDataListener() {
        initRawDataListener()

        enableRawDataNotifications()

        maybeRegisterRawDetectorListener()
    }

    override fun unregisterListeners() {
        unregisterRawDetectorListener()
    }

    fun initRawDataListener() {
        if (rawDetectorListener == null) {
            rawDetectorListener = RawDetectorListener { _, sensorState ->
                val isPlaying = isPlaying()
                sensorListener.onRawData(isPlaying, sensorState)
            }
        }
    }

    override fun registerDelayableListeners() {
        // no-op
    }

    @VisibleForTesting
    fun enableRawDataNotifications() {
        connection.callSafelyIfActive {
            detectors().enableRawDataNotifications()
        }
    }

    @VisibleForTesting
    fun disableRawDataNotifications() {
        connection.callSafelyIfActive { detectors().disableRawDataNotifications() }
    }

    @VisibleForTesting
    @MainThread
    fun maybeRegisterRawDetectorListener() {
        if (!registeredRawDetectorListener) {
            connection.callSafelyIfActive {
                rawDetectorListener?.let {
                    detectors().rawData().register(it)
                }

                registeredRawDetectorListener = true
            }
        }
    }

    fun unregisterRawDetectorListener() {
        callSafely {
            rawDetectorListener?.let {
                disableRawDataNotifications()

                connection.detectors().rawData().unregister(it)
            }
        }

        registeredRawDetectorListener = false

        rawDetectorListener = null
    }
}
