/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import com.kolibree.android.game.sensors.SensorConfiguration
import com.kolibree.android.sdk.connection.KLTBConnection

internal object AngleAndSpeedSensorConfiguration : SensorConfiguration {

    override val isMonitoredBrushing: Boolean = false
    override val useSvm: Boolean = false
    override val useRawData: Boolean = true
    override val usePlaqless: Boolean = false
    override val useOverpressure: Boolean = false

    object Factory : SensorConfiguration.Factory {

        override fun configurationForConnection(connection: KLTBConnection) =
            AngleAndSpeedSensorConfiguration
    }
}
