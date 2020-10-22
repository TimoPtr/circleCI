package com.kolibree.charts.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.toUTCEpochMilli
import com.kolibree.charts.persistence.models.StatInternal
import com.kolibree.sdkws.data.model.Brushing
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

/**
 * Created by guillaumeagis on 22/05/2018.
 */
class StatInternalTest : BaseUnitTest() {

    private val clock = TrustedClock.utcClock

    @Test
    fun init_empty_setsNullValues() {
        val stat = StatInternal(0L, 0L, 0L, clock, "")

        assertEquals(0, stat.duration)
        assertEquals(0, stat.timestamp)
        assertEquals("", stat.processedData)
        assertEquals(0, stat.profileId)
    }

    @Test
    fun init_WithValues() {

        val currentDate = TrustedClock.getNowZonedDateTime()
        val stat = StatInternal(PROFILE_ID, DURATION, currentDate.toUTCEpochMilli(), clock, PROCESSED_DATA)

        assertEquals(DURATION, stat.duration)
        assertEquals(currentDate.toUTCEpochMilli(), stat.timestamp)
        assertEquals(PROCESSED_DATA, stat.processedData)
        assertEquals(PROFILE_ID, stat.profileId)
    }

    @Test
    fun init_WithValuesWithoutProcessedData() {

        val currentDate = TrustedClock.getNowZonedDateTime()
        val stat = StatInternal(PROFILE_ID, DURATION, clock, currentDate.toUTCEpochMilli())

        assertEquals(DURATION, stat.duration)
        assertEquals(currentDate.toUTCEpochMilli(), stat.timestamp)
        assertEquals("", stat.processedData)
        assertEquals(PROFILE_ID, stat.profileId)
    }

    @Test
    fun creationStatObjectFromStatInternal() {

        val morningDate = TrustedClock.getNowOffsetDateTime()
            .truncatedTo(ChronoUnit.DAYS)
            .withHour(1)

        val statInternal = StatInternal(PROFILE_ID, DURATION, morningDate.toInstant().toEpochMilli(), clock, PROCESSED_DATA)
        val brushing = Brushing(
            DURATION,
            120,
            morningDate,
            10,
            10,
            PROCESSED_DATA,
            PROFILE_ID,
            10,
            "game1"
        )

        assertEquals(statInternal, StatInternal.fromBrushing(brushing, clock))
    }

    companion object {
        private const val DURATION = 1234234L
        private const val PROCESSED_DATA = "process_data"
        private const val PROFILE_ID = 14L
    }
}
