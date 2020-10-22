package com.kolibree.charts.persistence.models

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.BaseInstrumentationTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatInternalTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun hashCodeMethod() {
        val statInternal = StatInternal(
            id = 1L,
            clock = TrustedClock.utcClock,
            duration = 120L,
            processedData = "",
            profileId = 12L,
            timestamp = 12345L
        )

        statInternal.hashCode()
    }
}
