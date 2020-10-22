/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.legacy.LegacyStoredBrushingsExtractor
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

class RecordHeaderTest : BaseUnitTest() {
    @Test
    fun `utcDateTime returns shanghai timestamp as a OffsetDateTime`() {
        /*
        UTC       -> Thursday, June 6, 2019 10:49:23 AM
        GMT+08:00 -> Thursday, June 6, 2019 18:49:23 AM
         */
        val timestamp = 1559818163L

        TrustedClock.systemZone = ZoneOffset.ofHours(8)

        val expectedTime = OffsetDateTime.parse("2019-06-06T18:49:23+08:00")

        val header = LegacyStoredBrushingsExtractor.RecordHeader(
            crc = 0,
            sampleCount = 0,
            samplingPeriodMillis = 0,
            timestamp = timestamp
        )

        assertEquals(expectedTime, header.dateTime())
    }

    @Test
    fun `utcDateTime returns france timestamp as a OffsetDateTime`() {
        /*
        UTC       -> Wednesday, June 12, 2019 6:20:19 AM
        GMT+02:00 -> Wednesday, June 12, 2019 8:20:19 AM
         */
        val timestamp = 1560320419L

        TrustedClock.systemZone = ZoneOffset.ofHours(2)

        val expectedTime = OffsetDateTime.parse("2019-06-12T08:20:19+02:00")

        val header = LegacyStoredBrushingsExtractor.RecordHeader(
            crc = 0,
            sampleCount = 0,
            samplingPeriodMillis = 0,
            timestamp = timestamp
        )

        assertEquals(expectedTime, header.dateTime())
    }
}
