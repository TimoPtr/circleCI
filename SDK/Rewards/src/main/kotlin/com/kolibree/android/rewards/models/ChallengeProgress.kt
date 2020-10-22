package com.kolibree.android.rewards.models

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.kolibree.android.rewards.persistence.ChallengeProgressZoneDateTimeToStringConverter
import com.kolibree.android.rewards.synchronization.challengeprogress.ChallengeCompletionDetails
import org.threeten.bp.ZonedDateTime

/**
 * Interface to be used by non-persistence consumers
 */
@Keep
interface ChallengeWithProgress : Challenge {
    val profileId: Long
    val percentage: Int
    val completionTime: ZonedDateTime?

    fun stepsToReward(): Int

    fun hasRewardSteps(): Boolean

    fun shouldShowProgress(): Boolean

    fun isCompleted() = completionTime != null
}

@Keep
@Entity(
    tableName = "challenge_progress",
    foreignKeys = [ForeignKey(
        entity = ChallengeEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("challengeId"),
        onDelete = ForeignKey.CASCADE
    )],
    primaryKeys = ["challengeId", "profileId"],
    indices = [Index(value = ["challengeId", "profileId"])]
)
@TypeConverters(ChallengeCompletionDetailsConverter::class, ChallengeProgressZoneDateTimeToStringConverter::class)
internal data class ChallengeProgressEntity(
    val challengeId: Long,
    val profileId: Long,
    val completionTime: ZonedDateTime?,
    val completionDetails: ChallengeCompletionDetails?,
    val percentage: Int
)

internal class ChallengeCompletionDetailsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromChallengeCompletionDetails(challengeCompletionDetails: ChallengeCompletionDetails?): String? {
        return challengeCompletionDetails?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun fromStringToChallengeCompletionDetails(string: String?): ChallengeCompletionDetails? =
        string?.let {
            gson.fromJson<ChallengeCompletionDetails>(
                it,
                ChallengeCompletionDetails::class.java
            )
        }
}
