/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.retriever

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.ExtractionProgress
import org.threeten.bp.OffsetDateTime

@Keep
data class TimestampedExtractionProgress(
    val happenedAt: OffsetDateTime,
    val extractionProgress: ExtractionProgress
) {
    companion object {
        fun fromExtractionProgress(extractionProgress: ExtractionProgress): TimestampedExtractionProgress {
            return TimestampedExtractionProgress(
                happenedAt = TrustedClock.getNowOffsetDateTime(),
                extractionProgress = extractionProgress
            )
        }
    }
}
