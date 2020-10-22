/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.models

import androidx.annotation.StringDef
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.commons.ShortTask.Companion.fromInternalValue
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.rewards.feedback.personal.toPersonalChallengeId
import com.kolibree.android.rewards.models.BrushingSessionHistoryEventStatus.Companion.fromEventType
import com.kolibree.android.rewards.persistence.SmilesHistoryZoneDateTimeToStringConverter
import org.threeten.bp.ZonedDateTime

/**
 * Sealed class for smiles History Events, which is a Polymorphic entity.
 * It contains raw data from the backend and will be use to aggregate data to
 * create a SmilesHistoryItem
 */
@VisibleForApp
sealed class SmilesHistoryEvent {
    abstract val profileId: Long
    abstract val message: String
    abstract val smiles: Int
    abstract val creationTime: ZonedDateTime

    open fun toSpecificEvent(): SmilesHistoryEvent = this
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class ChallengeCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val challengeId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        challengeId = entity.challengeId!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class PersonalChallengeCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val challengeId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity, historyEventId: Long) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        challengeId = historyEventId
    )
}

internal enum class BrushingSessionHistoryEventStatus {
    COMPLETED,
    INCOMPLETE,
    DAILY_LIMIT_REACH;

    @VisibleForApp
    companion object {
        fun fromEventType(eventType: String): BrushingSessionHistoryEventStatus {
            return when (eventType) {
                EVENT_TYPE_BRUSHING_SESSION -> COMPLETED
                EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE -> INCOMPLETE
                EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT -> DAILY_LIMIT_REACH
                else -> {
                    FailEarly.fail("$eventType is not a valid eventType")
                    COMPLETED
                }
            }
        }
    }
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class BrushingSessionHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val brushingId: Long,
    val brushingType: String,
    val status: BrushingSessionHistoryEventStatus
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        brushingId = entity.brushingId!!,
        brushingType = entity.brushingType!!,
        status = fromEventType(entity.eventType)
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class OfflineBrushingSessionHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val brushingId: Long,
    val brushingType: String,
    val status: BrushingSessionHistoryEventStatus
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        brushingId = entity.brushingId!!,
        brushingType = entity.brushingType!!,
        status = fromEventType(entity.eventType)
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class TierReachedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val tierLevel: Int
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        tierLevel = entity.tierLevel!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class SmilesRedeemedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val rewardsId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        rewardsId = entity.rewardsId!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class SmilesTransferHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val relatedProfileId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        relatedProfileId = entity.relatedProfileId!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class CrownCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val relatedProfileId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        relatedProfileId = entity.relatedProfileId!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class StreakCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val relatedProfileId: Long
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        relatedProfileId = entity.relatedProfileId!!
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class SmilesExpiredHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class ReferralHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class NotificationTappedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class QuestionOfTheDayAnsweredHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class AccountCreatedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class ShortTaskCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime,
    val shortTask: ShortTask?
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles,
        shortTask = entity.brushingType?.let(::fromInternalValue)
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class ActivityCompletedHistoryEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class AmazonAccountLinkedEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent() {
    constructor(entity: SmilesHistoryEventEntity) : this(
        profileId = entity.profileId,
        message = entity.message,
        creationTime = entity.creationTime,
        smiles = entity.smiles
    )
}

@VisibleForTesting(otherwise = PRIVATE)
internal class FakeEvent(
    override val profileId: Long,
    override val message: String,
    override val smiles: Int,
    override val creationTime: ZonedDateTime
) : SmilesHistoryEvent()

@Entity(tableName = "smiles_history_events")
@TypeConverters(SmilesHistoryZoneDateTimeToStringConverter::class)
internal data class SmilesHistoryEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    override val smiles: Int,
    override val message: String,
    override val creationTime: ZonedDateTime,
    override val profileId: Long,
    @SmilesHistoryEventType val eventType: String,
    // for Challenge completed
    val challengeId: Long? = null,
    // for Brushing session
    val brushingId: Long? = null,
    val brushingType: String? = null,
    // for Tier reached
    val tierLevel: Int? = null,
    // for Smiles redeemed
    val rewardsId: Long? = null,
    // for Smiles transfer, Crown completed, Streak completed
    val relatedProfileId: Long? = null
) : SmilesHistoryEvent() {

    @Suppress("ComplexMethod", "LongMethod")
    override fun toSpecificEvent(): SmilesHistoryEvent = when (eventType) {
        EVENT_TYPE_CHALLENGE_COMPLETED -> ChallengeCompletedHistoryEvent(this)
        EVENT_TYPE_PERSONAL_CHALLENGE_COMPLETED ->
            PersonalChallengeCompletedHistoryEvent(this, toPersonalChallengeId(id))
        EVENT_TYPE_BRUSHING_SESSION,
        EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT,
        EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE
        -> brushingEvent(brushingType)
        EVENT_TYPE_TIER_REACHED -> TierReachedHistoryEvent(this)
        EVENT_TYPE_SMILES_REDEEMED -> SmilesRedeemedHistoryEvent(this)
        EVENT_TYPE_SMILES_TRANSFER -> SmilesTransferHistoryEvent(this)
        EVENT_TYPE_CROWN_COMPLETED -> CrownCompletedHistoryEvent(this)
        EVENT_TYPE_STREAK_COMPLETED -> StreakCompletedHistoryEvent(this)
        EVENT_TYPE_SMILES_EXPIRED -> SmilesExpiredHistoryEvent(this)
        EVENT_TYPE_REFERRAL -> ReferralHistoryEvent(this)
        EVENT_TYPE_NOTIFICATION_TAPPED -> NotificationTappedHistoryEvent(this)
        EVENT_TYPE_QUESTION_OF_THE_DAY_ANSWERED -> QuestionOfTheDayAnsweredHistoryEvent(this)
        EVENT_TYPE_ACCOUNT_CREATION -> AccountCreatedHistoryEvent(this)
        EVENT_TYPE_SHORT_TASK_COMPLETED -> ShortTaskCompletedHistoryEvent(this)
        EVENT_TYPE_ACTIVITY_COMPLETED -> ActivityCompletedHistoryEvent(this)
        EVENT_AMAZON_LINK_ACCOUNT -> AmazonAccountLinkedEvent(this)
        else -> this
    }

    private fun brushingEvent(brushingType: String?) = when (brushingType) {
        OFFLINE_BRUSHING_TYPE_SUCCESS,
        OFFLINE_BRUSHING_TYPE_FAILURE -> OfflineBrushingSessionHistoryEvent(this)
        else -> BrushingSessionHistoryEvent(this)
    }
}

internal const val EVENT_TYPE_BRUSHING_SESSION = "Brushing session"
internal const val EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT = "Brushing session daily rewards reached"
internal const val EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE = "Brushing session incomplete"

internal const val EVENT_TYPE_CHALLENGE_COMPLETED = "Challenge completed"
internal const val EVENT_TYPE_TIER_REACHED = "Tier reached"
internal const val EVENT_TYPE_SMILES_REDEEMED = "Smiles redeemed"
internal const val EVENT_TYPE_SMILES_TRANSFER = "Smiles transfer"
internal const val EVENT_TYPE_CROWN_COMPLETED = "Crown completed"
internal const val EVENT_TYPE_STREAK_COMPLETED = "Streak completed"
internal const val EVENT_TYPE_PERSONAL_CHALLENGE_COMPLETED = "Personal challenge completed"

internal const val EVENT_TYPE_SMILES_EXPIRED = "Points expired"
internal const val EVENT_TYPE_REFERRAL = "Additional referral"
internal const val EVENT_TYPE_NOTIFICATION_TAPPED = "Notifications tapped"
internal const val EVENT_TYPE_QUESTION_OF_THE_DAY_ANSWERED = "QuestionOfTheDayAnswered"
internal const val EVENT_TYPE_ACCOUNT_CREATION = "Account created"
internal const val EVENT_TYPE_SHORT_TASK_COMPLETED = "Short Task Completed"
internal const val EVENT_TYPE_ACTIVITY_COMPLETED = "Activities Completed"
internal const val EVENT_AMAZON_LINK_ACCOUNT = "Link Amazon account"

internal const val OFFLINE_BRUSHING_TYPE_SUCCESS = "Offline"
internal const val OFFLINE_BRUSHING_TYPE_FAILURE = "Offline has been not completed"

@StringDef(
    EVENT_TYPE_CHALLENGE_COMPLETED,
    EVENT_TYPE_BRUSHING_SESSION,
    EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT,
    EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE,
    EVENT_TYPE_TIER_REACHED,
    EVENT_TYPE_SMILES_REDEEMED,
    EVENT_TYPE_SMILES_TRANSFER,
    EVENT_TYPE_CROWN_COMPLETED,
    EVENT_TYPE_STREAK_COMPLETED,
    EVENT_TYPE_PERSONAL_CHALLENGE_COMPLETED,
    EVENT_TYPE_SMILES_EXPIRED,
    EVENT_TYPE_REFERRAL,
    EVENT_TYPE_NOTIFICATION_TAPPED,
    EVENT_TYPE_QUESTION_OF_THE_DAY_ANSWERED,
    EVENT_TYPE_ACCOUNT_CREATION,
    EVENT_TYPE_SHORT_TASK_COMPLETED,
    EVENT_TYPE_ACTIVITY_COMPLETED,
    EVENT_AMAZON_LINK_ACCOUNT
)
@Retention(AnnotationRetention.SOURCE)
internal annotation class SmilesHistoryEventType
