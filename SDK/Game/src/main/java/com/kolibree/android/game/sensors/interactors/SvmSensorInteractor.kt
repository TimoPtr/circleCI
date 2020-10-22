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
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.listener.SVMDetectorListener
import com.kolibree.android.utils.callSafely
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

@GameScope
internal class SvmSensorInteractor @Inject constructor(
    private val holder: GameSensorListener,
    gameLifecycleProvider: GameLifecycleProvider,
    gameToothbrushEventProvider: GameToothbrushEventProvider
) : BaseSensorInteractor(gameLifecycleProvider, gameToothbrushEventProvider),
    SVMDetectorListener {

    @VisibleForTesting
    var registeredProbableMouthZoneListener: Boolean = false

    override fun registerListeners() {
        enableDetectionNotifications()
    }

    override fun registerDelayableListeners() {
        maybeRegisterProbableMouthZonesListener()
    }

    @VisibleForTesting
    fun enableDetectionNotifications() {
        connection.detectors().enableDetectionNotifications()
    }

    @VisibleForTesting
    @MainThread
    fun maybeRegisterProbableMouthZonesListener() {
        if (!registeredProbableMouthZoneListener) {
            connection.detectors().probableMouthZones().register(this)

            registeredProbableMouthZoneListener = true
        }
    }

    override fun unregisterListeners() {
        unregisterProbableMouthZonesListener()

        disableDetectionNotifications()
    }

    @VisibleForTesting
    fun disableDetectionNotifications() {
        callSafely { connection.detectors().disableDetectionNotifications() }
    }

    override fun onSVMData(source: KLTBConnection, data: MutableList<MouthZone16>) {
        if (isPlaying()) {
            holder.onSVMData(source, data)
        }
    }

    @VisibleForTesting
    fun unregisterProbableMouthZonesListener() {
        callSafely { connection.detectors().probableMouthZones().unregister(this) }

        registeredProbableMouthZoneListener = false
    }
}
