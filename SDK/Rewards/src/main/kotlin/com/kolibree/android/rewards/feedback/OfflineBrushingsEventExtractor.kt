package com.kolibree.android.rewards.feedback

import androidx.annotation.VisibleForTesting
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import javax.inject.Inject

internal class OfflineBrushingsEventExtractor @Inject constructor() {

    fun onlyOfflineBrushingEvents(itemsToProcess: List<SmilesHistoryEvent>): List<OfflineBrushingSessionHistoryEvent> {
        return itemsToProcess.map {
            it.toSpecificEvent()
        }.filter {
            isOfflineBrushing(it)
        }.map {
            it as OfflineBrushingSessionHistoryEvent
        }
    }

    fun withoutOfflineBrushingEvents(itemsToProcess: List<SmilesHistoryEvent>): List<SmilesHistoryEvent> {
        return itemsToProcess.map {
            it.toSpecificEvent()
        }.filter {
            !isOfflineBrushing(it)
        }
    }

    @VisibleForTesting
    internal fun isOfflineBrushing(event: SmilesHistoryEvent) = event is OfflineBrushingSessionHistoryEvent
}
