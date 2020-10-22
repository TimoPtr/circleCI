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
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import javax.inject.Inject

internal class HistoryToOfflineBrushingsSyncFeedbackMapper @Inject constructor() {

    fun map(itemsToProcess: List<OfflineBrushingSessionHistoryEvent>): List<FeedbackEntity> {
        val item = mapToItem(itemsToProcess)
        if (item != null) {
            return listOf(item)
        }
        return emptyList()
    }

    @VisibleForTesting
    internal fun mapToItem(itemsToProcess: List<OfflineBrushingSessionHistoryEvent>): FeedbackEntity? {
        val events = itemsToProcess.sortedByDescending { it.creationTime.toEpochSecond() }
        val brushings = events.size
        if (brushings > 0) {
            val profileId = events.first().profileId
            val creationTime = events.first().creationTime
            val smiles = events.map { it.smiles }.sum()
            return FeedbackEntity.createOfflineSyncEntity(
                profileId,
                creationTime,
                brushings,
                smiles
            )
        }
        return null
    }
}
