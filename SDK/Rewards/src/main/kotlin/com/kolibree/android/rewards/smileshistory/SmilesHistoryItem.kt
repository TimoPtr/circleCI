/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.models.BrushingSessionHistoryEventStatus
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

/**
 * An item contains holds the data that will be need to provide the resources to
 * the view. It's the result of aggregation of data coming from multiple sources and
 * SmilesHistoryEvent's
 */
@VisibleForApp
sealed class SmilesHistoryItem : Parcelable {
    abstract val smiles: Int
    abstract val creationTime: ZonedDateTime

    @Parcelize
    @VisibleForApp
    object UnknownHistoryItem : SmilesHistoryItem() {
        @IgnoredOnParcel
        override val smiles: Int = 0

        @IgnoredOnParcel
        override val creationTime: ZonedDateTime = TrustedClock.getNowZonedDateTime()
    }

    @Parcelize
    @VisibleForApp
    data class BrushingSessionItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val game: Game?,
        val status: BrushingSessionItemStatus
    ) : SmilesHistoryItem() {
        @VisibleForApp
        enum class BrushingSessionItemStatus {
            COMPLETED,
            INCOMPLETE,
            DAILY_LIMIT_REACH;

            internal companion object {
                fun fromBrushingSessionHistoryStatus(
                    status: BrushingSessionHistoryEventStatus
                ): BrushingSessionItemStatus =
                    when (status) {
                        BrushingSessionHistoryEventStatus.COMPLETED -> COMPLETED
                        BrushingSessionHistoryEventStatus.INCOMPLETE -> INCOMPLETE
                        BrushingSessionHistoryEventStatus.DAILY_LIMIT_REACH -> DAILY_LIMIT_REACH
                    }
            }
        }
    }

    @Parcelize
    @VisibleForApp
    data class ChallengeCompletedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val challengeName: String?,
        val pictureUrl: String?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class PersonalChallengeCompletedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val challengeName: String?,
        val pictureUrl: String?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class TierReachedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val rank: String?,
        val smilesPerBrushing: String?,
        val pictureUrl: String?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class SmilesRedeemedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val rewardsName: String?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class SmilesTransferItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val relatedProfile: String?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class StreakCompletedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class AccountCreatedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class SmilesExpiredItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class ReferralItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class NotificationTappedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class QuestionOfTheDayAnsweredItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class ActivityCompletedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class ShortTaskItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime,
        val shortTask: ShortTask?
    ) : SmilesHistoryItem()

    @Parcelize
    @VisibleForApp
    data class AmazonAccountLinkedItem(
        override val smiles: Int,
        override val creationTime: ZonedDateTime
    ) : SmilesHistoryItem()
}
