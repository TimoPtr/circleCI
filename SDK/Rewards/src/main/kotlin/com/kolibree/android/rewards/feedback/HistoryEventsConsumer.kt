/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import androidx.annotation.VisibleForTesting
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.persistence.FeedbackDao
import javax.inject.Inject
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

/**
 * Accepts a List of N SmilesHistoryEventEntity and inserts [0-N] FeedbackEntity
 *
 * Events aren't necessarily grouped. They can be intertwined if we create multiple
 * brushings in a single call. There's a Rewards queue that processes the brushing and creates the events related
 *
 * Current implementation ignores that issue and processes events as if they were sequential, following iOS'
 * implementation
 */
internal class HistoryEventsConsumer
@Inject constructor(
    private val feedbackDao: FeedbackDao,
    private val historyToFeedbackMapper: HistoryToFeedbackMapper,
    private val firstLoginDateProvider: FirstLoginDateProvider,
    private val historyToOfflineFeedbackMapper: HistoryToOfflineBrushingsSyncFeedbackMapper,
    private val extractor: OfflineBrushingsEventExtractor
) {

    fun accept(historyEvents: List<SmilesHistoryEventEntity>) {
        Timber.d("Accepted entities $historyEvents")
        /*
        State machine that processes events and inserts Feedback entities

        1. History events to be ignored for now: SmilesRedeemed, SmilesTransfer, CrownCompleted, StreakCompleted
        2. History events need to be grouped following these rules
            2.1 A Brushing without ChallengeCompleted or TierReached shows a “you earned X smiles” feedback
            2.2. A brushing followed by X ChallengeCompleted shows a ChallengeCompleted or MultipleChallengesCompleted
            feedback. If there’s also a Tier Reached, we also create a TierReached feedback
        3. Don't process smile events that have already been processed. If our Actions table is empty, ignore items
        prior to now
         */

        val allItems = extractItemsToProcess(historyEvents)
        val itemsToProcess = extractor.withoutOfflineBrushingEvents(allItems)
        Timber.d("entities to process $itemsToProcess")

        val feedbackEntities = mutableListOf<FeedbackEntity>()
        feedbackEntities += historyToFeedbackMapper.map(itemsToProcess)

        val offlineBrushingsToProcess = extractor.onlyOfflineBrushingEvents(allItems)
        Timber.d("offline entities to process $offlineBrushingsToProcess")
        feedbackEntities += historyToOfflineFeedbackMapper.map(offlineBrushingsToProcess)

        Timber.d("entities to insert $feedbackEntities")
        feedbackDao.insert(feedbackEntities)
    }

    /**
     * Returns a List of events that happened after creationTimeFilter, sorted from oldest to most recent
     *
     * Avoids processing SmilesHistoryEvents twice by filtering out events already processed in the past
     */
    @VisibleForTesting
    fun extractItemsToProcess(historyEvents: List<SmilesHistoryEventEntity>): List<SmilesHistoryEventEntity> {
        if (historyEvents.isEmpty()) {
            return listOf()
        }

        val profileId = historyEvents.first().profileId
        val creationTimeFilter = creationTimeFilter(profileId)

        return historyEvents
            .filter { it.creationTime.isAfter(creationTimeFilter) }
            .sortedBy(SmilesHistoryEventEntity::creationTime)
    }

    /**
     * Returns the time threshold after which we'll consider a History event for further processing
     *
     * All events prior or equal to the returned time will be discarded
     *
     * The reasoning behind this decision is
     * - If the DB is empty, it means it's the first time we are processing events after login. We don't want to show
     * dozens of feedback items
     * - If an item is equal to or older than the most recent feedback action item in the DB, it means that it has
     * already been processed
     */
    @VisibleForTesting
    fun creationTimeFilter(profileId: Long): ZonedDateTime {
        return feedbackDao.mostRecentFeedbackDateTime(profileId)?.dateTime
            ?: firstLoginDateProvider.firstRunDate()
    }
}
