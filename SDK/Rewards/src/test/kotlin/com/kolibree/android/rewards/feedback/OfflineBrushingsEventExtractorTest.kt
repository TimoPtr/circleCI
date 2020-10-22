package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.test.createOfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.test.createTierReachedHistoryEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineBrushingsEventExtractorTest : BaseUnitTest() {

    private lateinit var extractor: OfflineBrushingsEventExtractor

    override fun setup() {
        super.setup()

        extractor = OfflineBrushingsEventExtractor()
    }

    @Test
    fun `withoutOfflineBrushingEvents`() {
        val brushingEvent1 = createOfflineBrushingSessionHistoryEvent(profileId = 1L,
            brushingId = 11)
        val brushingEvent2 = createTierReachedHistoryEvent(profileId = 2L)
        val items = listOf(brushingEvent1, brushingEvent2)

        val result = extractor.withoutOfflineBrushingEvents(items)

        assertEquals(1, result.size)
        val firstResult = result[0]
        assertEquals(2L, firstResult.profileId)
    }

    @Test
    fun `onlyOfflineBrushingEvents`() {
        val brushingEvent1 = createOfflineBrushingSessionHistoryEvent(profileId = 1L,
            brushingId = 11)
        val brushingEvent2 = createTierReachedHistoryEvent(profileId = 2L)
        val brushingEvent3 = createTierReachedHistoryEvent(profileId = 3L)
        val items = listOf(brushingEvent1, brushingEvent2, brushingEvent3)

        val result = extractor.onlyOfflineBrushingEvents(items)

        assertEquals(1, result.size)
        val firstResult = result[0]
        assertEquals(1L, firstResult.profileId)
    }
}
