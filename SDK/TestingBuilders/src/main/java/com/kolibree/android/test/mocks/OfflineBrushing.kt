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
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.test.mocks.BrushingBuilder.DEFAULT_PROCESSED_DATA
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

@JvmOverloads
@Keep
fun createOfflineBrushing(
    durationInSeconds: Long = DEFAULT_BRUSHING_GOAL.toLong(),
    dateTime: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    processedData: String = DEFAULT_PROCESSED_DATA
) = createOfflineBrushingFromLocalDateTime(
    durationInSeconds,
    dateTime.toLocalDateTime(),
    processedData
)

@Keep
fun createOfflineBrushingFromLocalDateTime(
    durationInSeconds: Long,
    dateTime: LocalDateTime = TrustedClock.getNowLocalDateTime(),
    processedData: String = DEFAULT_PROCESSED_DATA
): OfflineBrushing {
    return object : OfflineBrushing {
        override val datetime: LocalDateTime = dateTime
        override val duration: Duration = Duration.of(durationInSeconds, ChronoUnit.SECONDS)
        override val processedData: String = processedData
    }
}
