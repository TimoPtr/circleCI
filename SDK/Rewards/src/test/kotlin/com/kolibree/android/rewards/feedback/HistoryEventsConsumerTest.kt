/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.persistence.FeedbackDao
import com.kolibree.android.rewards.persistence.MostRecentFeedbackDatetime
import com.kolibree.android.rewards.test.createSmilesHistoryEventEntity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HistoryEventsConsumerTest : BaseUnitTest() {
    private val feedbackDao: FeedbackDao = mock()
    private val mapper: HistoryToFeedbackMapper = mock()
    private val firstLoginDateProvider: FirstLoginDateProvider = mock()
    private val offlineMapper: HistoryToOfflineBrushingsSyncFeedbackMapper = mock()
    private val extractor: OfflineBrushingsEventExtractor = mock()

    private val historyEventsConsumer = spy(
        HistoryEventsConsumer(
            feedbackDao,
            mapper,
            firstLoginDateProvider,
            offlineMapper,
            extractor
        )
    )

    /*
    CREATION TIME FILTER
     */

    @Test
    fun `creationTimeFilter returns time from firstSynchronizationDateProvider if feedbackDao returns null`() {
        val expectedTime = TrustedClock.getNowZonedDateTime().minusMinutes(13)
        whenever(firstLoginDateProvider.firstRunDate()).thenReturn(expectedTime)

        assertEquals(expectedTime, historyEventsConsumer.creationTimeFilter(1L))
    }

    @Test
    fun `creationTimeFilter returns time from firstSynchronizationDateProvider if feedbackDao returns item with null datetime`() {
        val profileId = 1L
        whenever(feedbackDao.mostRecentFeedbackDateTime(profileId)).thenReturn(
            MostRecentFeedbackDatetime(dateTime = null)
        )

        val expectedTime = TrustedClock.getNowZonedDateTime().minusMinutes(13)
        whenever(firstLoginDateProvider.firstRunDate()).thenReturn(expectedTime)

        assertEquals(expectedTime, historyEventsConsumer.creationTimeFilter(profileId))
    }

    @Test
    fun `creationTimeFilter returns item datetime from feedbackDao`() {
        val profileId = 1L
        val expectedTime = TrustedClock.getNowZonedDateTime().minusDays(2)
        whenever(feedbackDao.mostRecentFeedbackDateTime(profileId)).thenReturn(
            MostRecentFeedbackDatetime(dateTime = expectedTime)
        )
        assertEquals(expectedTime, historyEventsConsumer.creationTimeFilter(profileId))
    }

    /*
    EXTRACT ITEMS TO PROCESS
     */

    @Test
    fun `extractItemsToProcess returns empty list when input is empty`() {
        assertTrue(historyEventsConsumer.extractItemsToProcess(listOf()).isEmpty())
    }

    @Test
    fun `extractItemsToProcess returns empty list when events are before creation time filter`() {
        val creationTimeFilter = TrustedClock.getNowZonedDateTime()

        val event1 =
            createSmilesHistoryEventEntity(creationTime = creationTimeFilter.minusSeconds(1))
        val event2 = createSmilesHistoryEventEntity(creationTime = creationTimeFilter.minusDays(1))

        doReturn(creationTimeFilter).whenever(historyEventsConsumer).creationTimeFilter(any())

        assertTrue(historyEventsConsumer.extractItemsToProcess(listOf(event1, event2)).isEmpty())
    }

    @Test
    fun `extractItemsToProcess filters out events before creation time filter`() {
        val creationTimeFilter = TrustedClock.getNowZonedDateTime()

        val discardEvent1 =
            createSmilesHistoryEventEntity(creationTime = creationTimeFilter.minusSeconds(1))
        val expectedEvent =
            createSmilesHistoryEventEntity(creationTime = creationTimeFilter.plusSeconds(1))
        val discardEvent2 =
            createSmilesHistoryEventEntity(creationTime = creationTimeFilter.minusDays(1))

        doReturn(creationTimeFilter).whenever(historyEventsConsumer).creationTimeFilter(any())

        val filteredEvents =
            historyEventsConsumer.extractItemsToProcess(
                listOf(
                    discardEvent1,
                    expectedEvent,
                    discardEvent2
                )
            )

        assertEquals(1, filteredEvents.size)
        assertTrue(filteredEvents.contains(expectedEvent))
    }

    @Test
    fun `extractItemsToProcess sorts events by creation time descending`() {
        val creationTimeFilter = TrustedClock.getNowZonedDateTime()

        val discardEvent1 =
            createSmilesHistoryEventEntity(creationTime = creationTimeFilter.minusSeconds(1))
        val expectedEvent1 =
            createSmilesHistoryEventEntity(id = 1, creationTime = creationTimeFilter.plusSeconds(1))
        val expectedEvent2 =
            createSmilesHistoryEventEntity(id = 2, creationTime = creationTimeFilter.plusSeconds(2))
        val expectedEvent3 =
            createSmilesHistoryEventEntity(id = 3, creationTime = creationTimeFilter.plusDays(3))

        doReturn(creationTimeFilter).whenever(historyEventsConsumer).creationTimeFilter(any())

        val filteredEvents =
            historyEventsConsumer.extractItemsToProcess(
                listOf(
                    discardEvent1,
                    expectedEvent3,
                    expectedEvent1,
                    expectedEvent2
                )
            )

        assertEquals(3, filteredEvents.size)
        assertEquals(expectedEvent1, filteredEvents.get(0))
        assertEquals(expectedEvent2, filteredEvents.get(1))
        assertEquals(expectedEvent3, filteredEvents.get(2))
    }
}
