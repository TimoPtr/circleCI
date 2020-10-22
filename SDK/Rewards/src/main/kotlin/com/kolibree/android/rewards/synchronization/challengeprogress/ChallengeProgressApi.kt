package com.kolibree.android.rewards.synchronization.challengeprogress

import androidx.annotation.Keep
import com.google.gson.annotations.JsonAdapter
import com.kolibree.android.rewards.synchronization.ChallengeProgressZonedDateTimeTypeAdapter
import org.threeten.bp.ZonedDateTime

@Keep
internal data class ChallengeProgressApi(
    val progress: Progress,
    val totalSmiles: Int
) {
    fun allChallenges(): List<ChallengesItem> {
        return progress.catalog.map { it.challenges }.flatten()
    }
}

@Keep
internal data class Progress(
    val catalog: List<CatalogItem>,
    val language: String
)

@Keep
internal data class CatalogItem(
    val categoryId: Long,
    val challenges: List<ChallengesItem>,
    val category: String
)

@Keep
internal data class ChallengesItem(
    @JsonAdapter(ChallengeProgressZonedDateTimeTypeAdapter::class) val completionTime: ZonedDateTime?,
    val pictureUrl: String,
    val challengeId: Long,
    val percentage: Int,
    val challengeName: String,
    val completionDetails: ChallengeCompletionDetails? = null,
    val smilesReward: Int,
    val group: Int? = null
)
