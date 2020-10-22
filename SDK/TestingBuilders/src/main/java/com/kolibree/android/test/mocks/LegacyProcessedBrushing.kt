/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.processedbrushings.BrushingPass
import com.kolibree.android.processedbrushings.LegacyProcessedBrushing
import com.kolibree.android.test.mocks.BrushingBuilder.DEFAULT_PROCESSED_DATA
import com.kolibree.kml.MouthZone16
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit

@Keep
fun createLegacyProcessedBrushing(
    durationInMillis: Long,
    offlineDate: ZonedDateTime
): LegacyProcessedBrushing {
    return object : LegacyProcessedBrushing {
        override val processedData: String = DEFAULT_PROCESSED_DATA
        override val datetime: LocalDateTime = offlineDate.toLocalDateTime()
        override val duration: Duration = Duration.of(durationInMillis, ChronoUnit.MILLIS)
        override val mouthZonePasses: Map<MouthZone16, List<BrushingPass>> = mapOf()
    }
}
