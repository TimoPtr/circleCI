/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import com.kolibree.android.game.sensors.GameSensorListener
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessRawSensorState
import com.kolibree.android.sdk.connection.detectors.data.PlaqlessSensorState
import com.kolibree.kml.MouthZone16

internal interface RawDataSensorListener : GameSensorListener {

    override fun currentZone(): MouthZone16? = null

    override fun onPlaqlessRawData(isPlaying: Boolean, sensorState: PlaqlessRawSensorState) {
        // no-op
    }

    override fun onPlaqlessData(isPlaying: Boolean, sensorState: PlaqlessSensorState) {
        // no-op
    }

    override fun onSVMData(source: KLTBConnection, data: MutableList<MouthZone16>) {
        // no-op
    }
}
