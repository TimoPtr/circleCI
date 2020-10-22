/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.IBrushing

internal data class DayCheckupData(
    val coverage: Float?,
    val durationPercentage: Float,
    val duration: Long,
    val checkupData: Map<MouthZone16, Float>,
    val iBrushing: IBrushing
)
