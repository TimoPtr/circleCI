package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.models.OFFLINE_BRUSHING_TYPE_SUCCESS
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.test.createOfflineBrushingSessionHistoryEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryToOfflineBrushingsSyncFeedbackMapperTest : BaseUnitTest() {

    private lateinit var mapper: HistoryToOfflineBrushingsSyncFeedbackMapper

    override fun setup() {
        super.setup()

        mapper = HistoryToOfflineBrushingsSyncFeedbackMapper()
    }

    @Test
    fun `map for empty input returns empty list`() {
        val result = mapper.map(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `mapToItem for empty input returns null`() {
        val result = mapper.mapToItem(emptyList())
        assertNull(result)
    }

    @Test
    fun `mapToItem returns FeedbackEntity with newest creationTime`() {
        val events = mutableListOf<OfflineBrushingSessionHistoryEvent>()
        val newestCreationTime = TrustedClock.getNowZonedDateTime().plusDays(2)
        val brushingEvent1 = createOfflineBrushingSessionHistoryEvent(profileId = 1L,
            brushingType = OFFLINE_BRUSHING_TYPE_SUCCESS,
            creationTime = newestCreationTime.minusDays(2),
            brushingId = 11)
        val brushingEvent2 = createOfflineBrushingSessionHistoryEvent(profileId = 2L,
            brushingType = OFFLINE_BRUSHING_TYPE_SUCCESS,
            creationTime = newestCreationTime,
            brushingId = 12)
        val brushingEvent3 = createOfflineBrushingSessionHistoryEvent(profileId = 3L,
            brushingType = OFFLINE_BRUSHING_TYPE_SUCCESS,
            creationTime = newestCreationTime.minusDays(1),
            brushingId = 13)

        events += listOf(brushingEvent1, brushingEvent2, brushingEvent3)
        events.shuffle()

        val result = mapper.mapToItem(events)
        assertNotNull(result)
        assertEquals(newestCreationTime, result?.historyEventDateTime)
    }

    @Test
    fun `mapToItem returns FeedbackEntity with total smiles and brushings`() {
        val events = mutableListOf<OfflineBrushingSessionHistoryEvent>()
        val smiles1 = 78
        val smiles2 = 96
        val brushingEvent1 = createOfflineBrushingSessionHistoryEvent(profileId = 1L,
            brushingType = OFFLINE_BRUSHING_TYPE_SUCCESS,
            brushingId = 11,
            smiles = smiles1)
        val brushingEvent2 = createOfflineBrushingSessionHistoryEvent(profileId = 2L,
            brushingType = OFFLINE_BRUSHING_TYPE_SUCCESS,
            brushingId = 12,
            smiles = smiles2)

        events += listOf(brushingEvent1, brushingEvent2)
        events.shuffle()

        val result = mapper.mapToItem(events)
        assertNotNull(result)
        assertEquals(smiles1 + smiles2, result?.smilesEarned)
        assertEquals(2, result?.offlineSyncBrushings)
    }
}
