package com.kolibree.charts.persistence.room

import com.kolibree.android.clock.TrustedClock
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by guillaumeagis on 22/05/2018.
 *
 */
class StatsConvertersTest {

    private val converters = StatsConverters()

    @Test
    fun testConversionFromClockToJson() {
        val res = converters.fromClock(TrustedClock.utcClock)
        val expectedRes = TrustedClock.utcClock.zone.id

        assertEquals(expectedRes, res)
    }

    @Test
    fun testConversionFromJsonToClock() {
        val res = converters.toClock(TrustedClock.utcClock.zone.id)

        assertEquals(TrustedClock.utcClock, res)
    }
}
