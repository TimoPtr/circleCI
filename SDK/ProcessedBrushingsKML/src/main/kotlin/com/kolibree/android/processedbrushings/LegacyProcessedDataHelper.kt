/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings

import androidx.annotation.Keep
import com.kolibree.android.processedbrushings.models.ZonePass
import com.kolibree.kml.MouthZone16

/**
 * Generates Legacy Processed Data
 *
 * Not intended for production
 */
@Keep
fun computeProcessedData(data: Map<MouthZone16?, List<ZonePass?>?>, goalTime: Int): String {
    return LegacyProcessedDataGenerator.computeProcessedData(data, goalTime)
}
