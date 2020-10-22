package com.kolibree.android.offlinebrushings

import androidx.annotation.Keep
import com.kolibree.android.offlinebrushings.OfflineRetrieverEventId.EVENT_NONE
import com.kolibree.android.offlinebrushings.OfflineRetrieverEventId.EVENT_OFFLINE_RECORDS_RETRIEVED_FOR_CURRENT_PROFILE

@Keep
data class OfflineBrushingsRetrieverViewState(
    val eventId: OfflineRetrieverEventId,
    val recordsRetrieved: Int
) {
    @Keep
    companion object {
        @JvmStatic
        fun empty() = OfflineBrushingsRetrieverViewState(EVENT_NONE, 0)

        @JvmStatic
        fun withRecordsRetrieved(recordsRetrieved: Int) = OfflineBrushingsRetrieverViewState(
            EVENT_OFFLINE_RECORDS_RETRIEVED_FOR_CURRENT_PROFILE,
            recordsRetrieved
        )
    }

    fun haveRecordsBeenRetrievedForCurrentProfile() =
        eventId == EVENT_OFFLINE_RECORDS_RETRIEVED_FOR_CURRENT_PROFILE
}

@Keep
enum class OfflineRetrieverEventId {
    EVENT_NONE,
    EVENT_OFFLINE_RECORDS_RETRIEVED_FOR_CURRENT_PROFILE
}
