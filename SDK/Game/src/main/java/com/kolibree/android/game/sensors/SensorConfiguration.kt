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
import com.kolibree.android.sdk.connection.KLTBConnection

@Keep
interface SensorConfiguration {
    val isMonitoredBrushing: Boolean
    val useSvm: Boolean
    val useRawData: Boolean
    val usePlaqless: Boolean
    val useOverpressure: Boolean

    interface Factory {

        fun configurationForConnection(connection: KLTBConnection): SensorConfiguration
    }
}
