/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kolibree.android.rewards.persistence.SmilesHistoryZoneDateTimeToStringConverter
import org.threeten.bp.ZonedDateTime

/**
 * Polymorphic entity that can represent the following Feedback events
 * 1. SmilesEarned
 * 2. ChallengesCompleted. Holds 1 or more challenges
 * 3. TierReached. Holds 1 or more challenges + the tier level reached
 */
@Entity(tableName = "feedback")
@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class, LongListConverter::class)
internal data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val profileId: Long,
    /*
    Holds timestamp of the most recent history event this Feedback represents

    If a BrushingSessionEvent(t=1) is followed by a ChallengeCompletedEvent(t=2), this entity groups both into a single
    ChallengeCompletedFeedback, and the value in historyEventDateTime must be "2"
     */
    val historyEventDateTime: ZonedDateTime,
    val smilesEarned: Int = 0,
    val challengesCompleted: List<Long> = listOf(),
    val tierReached: Int = 0,
    val isConsumed: Boolean = false,
    val offlineSyncBrushings: Int = 0,
    val streakSmilesEarned: Int = 0
) {
    init {
        validatePolymorphism()
    }

    private fun validatePolymorphism() {
        when {
            // Do not reorder or everything will break
            isTierReached() -> {
                check(challengesCompleted.isNotEmpty())
                check(smilesEarned == 0)
                check(offlineSyncBrushings == 0)
                check(streakSmilesEarned == 0)
            }
            isChallengesCompleted() -> {
                check(smilesEarned == 0)
                check(tierReached == 0)
                check(offlineSyncBrushings == 0)
                check(streakSmilesEarned == 0)
            }
            isOfflineSync() -> {
                check(challengesCompleted.isEmpty())
                check(tierReached == 0)
                check(offlineSyncBrushings > 0)
                check(streakSmilesEarned == 0)
            }
            isStreakCompleted() -> {
                check(tierReached == 0)
                check(offlineSyncBrushings == 0)
                check(challengesCompleted.isEmpty())
                check(smilesEarned == 0)
                check(streakSmilesEarned > 0L)
            }
            isSmilesEarned() or isNoSmilesEarned() -> {
                check(challengesCompleted.isEmpty())
                check(tierReached == 0)
                check(offlineSyncBrushings == 0)
                check(streakSmilesEarned == 0)
            }
        }
    }

    fun isStreakCompleted() = streakSmilesEarned > 0 && smilesEarned == 0

    fun isTierReached() = tierReached > 0

    fun isChallengesCompleted() = tierReached == 0 && challengesCompleted.isNotEmpty()

    fun isSmilesEarned() = smilesEarned > 0 && offlineSyncBrushings == 0

    fun isNoSmilesEarned() = !isTierReached() && !isChallengesCompleted() &&
        smilesEarned == 0 && offlineSyncBrushings == 0

    fun isOfflineSync() = offlineSyncBrushings > 0

    companion object {
        fun createTierReachedEntity(
            profileId: Long,
            historyEventDateTime: ZonedDateTime,
            tierReached: Int,
            challengesCompleted: List<Long>
        ) = FeedbackEntity(
            profileId = profileId,
            historyEventDateTime = historyEventDateTime,
            tierReached = tierReached,
            challengesCompleted = challengesCompleted
        )

        fun createSmilesEarnedEntity(
            profileId: Long,
            historyEventDateTime: ZonedDateTime,
            smilesEarned: Int
        ) = FeedbackEntity(
            profileId = profileId,
            historyEventDateTime = historyEventDateTime,
            smilesEarned = smilesEarned
        )

        fun createChallengeCompletedEntity(
            profileId: Long,
            historyEventDateTime: ZonedDateTime,
            challengesCompleted: List<Long>
        ) = FeedbackEntity(
            profileId = profileId,
            historyEventDateTime = historyEventDateTime,
            challengesCompleted = challengesCompleted
        )

        fun createOfflineSyncEntity(
            profileId: Long,
            historyEventDateTime: ZonedDateTime,
            offlineSyncBrushings: Int,
            smilesEarned: Int
        ) = FeedbackEntity(
            profileId = profileId,
            historyEventDateTime = historyEventDateTime,
            offlineSyncBrushings = offlineSyncBrushings,
            smilesEarned = smilesEarned
        )

        fun createStreakCompletedEntity(
            relatedProfileId: Long,
            historyEventDateTime: ZonedDateTime,
            streakSmilesEarned: Int
        ) = FeedbackEntity(
            profileId = relatedProfileId,
            historyEventDateTime = historyEventDateTime,
            streakSmilesEarned = streakSmilesEarned
        )

        fun createNoSmilesEarnedEntity(
            profileId: Long,
            historyEventDateTime: ZonedDateTime
        ) = FeedbackEntity(
            profileId = profileId,
            historyEventDateTime = historyEventDateTime
        )
    }
}

internal class LongListConverter {
    @TypeConverter
    fun fromLongList(longList: List<Long>): String {
        if (longList.isEmpty()) return ""

        return longList.joinToString(separator = LONG_SEPARATOR) { it.toString() }.trim()
    }

    @TypeConverter
    fun toLongList(longListByComma: String): List<Long> {
        val textualList = longListByComma.trim()
        if (textualList.isEmpty() || textualList == LONG_SEPARATOR) return listOf()

        return textualList.split(LONG_SEPARATOR).mapNotNull { it.toLongOrNull() }
    }
}

private const val LONG_SEPARATOR = ","
