/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.sensors

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.android.sdk.connection.detectors.listener.SVMDetectorListener
import com.kolibree.kml.MouthZone16

@Keep
interface GameSensorListener : SVMDetectorListener {

    fun onRawData(isPlaying: Boolean, sensorState: RawSensorState)

    fun currentZone(): MouthZone16?

    fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState)

    fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState)

    fun onOverpressureState(overpressureState: OverpressureState)
}
