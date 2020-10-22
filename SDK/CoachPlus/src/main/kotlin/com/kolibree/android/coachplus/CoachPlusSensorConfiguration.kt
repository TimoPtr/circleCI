/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.sensors.SensorConfiguration
import com.kolibree.android.sdk.connection.KLTBConnection

internal object NonPlaqlessSensorConfigurationWithKML : SensorConfiguration {
    override val isMonitoredBrushing = true
    override val useSvm = false
    override val useRawData = true
    override val usePlaqless = false
    override val useOverpressure = false
}

internal object PlaqlessSensorConfiguration : SensorConfiguration {
    override val isMonitoredBrushing = true
    override val useSvm = false
    override val useRawData = false
    override val usePlaqless = true
    override val useOverpressure = false
}

internal object GlintSensorConfiguration : SensorConfiguration {
    override val isMonitoredBrushing: Boolean = true
    override val useSvm: Boolean = false
    override val useRawData: Boolean = true
    override val usePlaqless: Boolean = false
    override val useOverpressure: Boolean = true
}

@Keep
object CoachPlusSensorConfigurationFactory : SensorConfiguration.Factory {

    override fun configurationForConnection(connection: KLTBConnection): SensorConfiguration =
        when (connection.toothbrush().model) {
            ToothbrushModel.PLAQLESS -> PlaqlessSensorConfiguration
            ToothbrushModel.GLINT -> GlintSensorConfiguration
            else -> NonPlaqlessSensorConfigurationWithKML
        }
}
