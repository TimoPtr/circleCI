/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import com.kolibree.android.coachplus.controller.CoachPlusController
import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.OverpressureState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.android.sdk.connection.detectors.data.RawSensorState
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

internal class CoachPlusSensorsListener @Inject constructor(
    private val coachPlusController: CoachPlusController
) : GameSensorListener {

    override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
        coachPlusController.onPlaqlessData(isPlaying, sensorState)
    }

    override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
        coachPlusController.onPlaqlessRawData(isPlaying, sensorState)
    }

    override fun onRawData(isPlaying: Boolean, sensorState: RawSensorState) {
        coachPlusController.onRawData(isPlaying, sensorState)
    }

    override fun currentZone() = coachPlusController.getCurrentZone()

    override fun onSVMData(source: KLTBConnection, data: List<MouthZone16>) =
        coachPlusController.onSvmData(data)

    override fun onOverpressureState(overpressureState: OverpressureState) =
        coachPlusController.onOverpressureState(overpressureState)
}
