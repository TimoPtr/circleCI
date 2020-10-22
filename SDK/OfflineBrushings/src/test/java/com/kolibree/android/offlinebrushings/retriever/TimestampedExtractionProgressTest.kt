/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.retriever

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.test.extensions.withFixedInstant
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TimestampedExtractionProgressTest : BaseUnitTest() {
    @Test
    fun `fromExtractionProgress creates a TimestampedExtractionProgress instance that holds the parameter and current timestamp`() =
        withFixedInstant {
            val extractionProgress = ExtractionProgress.empty()

            val timestampedExtractionProgress =
                TimestampedExtractionProgress.fromExtractionProgress(extractionProgress)

            val expectedTimestampedExtractionProgress = TimestampedExtractionProgress(
                extractionProgress = extractionProgress,
                happenedAt = TrustedClock.getNowOffsetDateTime()
            )

            assertEquals(
                expectedTimestampedExtractionProgress,
                timestampedExtractionProgress
            )
        }
}
