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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.COMPLETED
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.DAILY_LIMIT_REACH
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.INCOMPLETE
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

private val defaultInfoRes = R.string.empty

internal interface ItemResources : Parcelable, SmilesHistoryListItem {
    val item: SmilesHistoryItem

    val smiles: String
        get() = if (isSmilesSpent) item.smiles.toString() else "+${item.smiles}"

    val isSmilesSpent: Boolean
        get() = item.smiles < 0

    val isSmilesAwarded: Boolean
        get() = item.smiles > 0

    val noSmilesChange: Boolean
        get() = item.smiles == 0

    val isInfoAvailable: Boolean
        get() = infoRes != defaultInfoRes

    val creationDateTime: ZonedDateTime
        get() = item.creationTime.withZoneSameInstant(TrustedClock.systemZone)

    val creationDate: String
        get() = creationDateTime.format(dateFormatter)

    val creationTime: String
        get() = creationDateTime.format(timeFormatter)

    val titleRes: Int
        @StringRes get

    val drawableRes: Int
        @DrawableRes get

    val infoRes: Int
        @StringRes get() = defaultInfoRes
}

@Parcelize
internal data class UnknownHistoryItemResources(override val item: SmilesHistoryItem) :
    ItemResources {
    override val titleRes: Int
        get() = defaultInfoRes

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

@Parcelize
internal data class BrushingSessionItemResources(
    override val item: SmilesHistoryItem.BrushingSessionItem
) : ItemResources {
    override val titleRes: Int
        get() = when (item.game) {
            Game.COACH -> R.string.smiles_history_item_coach_brushing
            Game.COACH_PLUS -> R.string.smiles_history_guided_brushing
            Game.GO_PIRATE -> R.string.smiles_history_go_pirate
            Game.TEST_BRUSHING -> R.string.smiles_history_test_brushing
            Game.RABBIDS -> R.string.smiles_history_rabbids
            Game.OFFLINE -> R.string.smiles_history_offline_brushing
            Game.TEST_ANGLES -> R.string.smiles_history_test_angle
            Game.SPEED_CONTROL -> R.string.smiles_history_mind_your_speed
            null -> R.string.smiles_history_offline_brushing
        }

    override val drawableRes: Int
        get() = if (item.status == COMPLETED) {
            when (item.game) {
                Game.OFFLINE, null -> R.drawable.ic_offline_brushing
                else -> R.drawable.ic_online_brushing
            }
        } else {
            R.drawable.ic_not_completed_brushing
        }

    override val infoRes: Int
        get() = when (item.status) {
            COMPLETED -> defaultInfoRes
            INCOMPLETE -> R.string.smiles_history_not_enough_coverage
            DAILY_LIMIT_REACH -> R.string.smiles_history_daily_limit_reached
        }
}

@Parcelize
internal data class ActivityCompletedItemResources(
    override val item: SmilesHistoryItem.ActivityCompletedItem
) : ItemResources {
    override val titleRes: Int
        get() = R.string.smiles_history_item_activity_completed

    override val drawableRes: Int
        get() = R.drawable.ic_online_brushing
}

@Parcelize
internal data class ShortTaskCompletedItemResources(
    override val item: SmilesHistoryItem.ShortTaskItem
) : ItemResources {
    override val titleRes: Int
        get() = when (item.shortTask) {
            ShortTask.TEST_YOUR_ANGLE -> R.string.smiles_history_test_angle
            ShortTask.MIND_YOUR_SPEED -> R.string.smiles_history_mind_your_speed
            else -> R.string.smiles_history_item_short_task_unknown
        }

    override val drawableRes: Int
        get() = R.drawable.ic_online_brushing
}

@Parcelize
internal data class ChallengeCompletedItemResources(
    override val item: SmilesHistoryItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_more_way_to_earn_smiles

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

@Parcelize
internal data class PersonalChallengeCompletedItemResources(
    override val item: SmilesHistoryItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_challenge_compled

    override val drawableRes: Int
        get() = R.drawable.ic_challenge_completed
}

@Parcelize
internal data class DiscountAppliedItemResources(
    override val item: SmilesHistoryItem.SmilesRedeemedItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_discount_applied

    override val drawableRes: Int
        get() = R.drawable.ic_discount_applied
}

@Parcelize
internal data class AccountCreatedItemResources(
    override val item: SmilesHistoryItem.AccountCreatedItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_account_created

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

@Parcelize
internal data class SmilesExpiredItemResources(
    override val item: SmilesHistoryItem.SmilesExpiredItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_smiles_expired

    override val drawableRes: Int
        get() = R.drawable.ic_discount_applied
}

@Parcelize
internal data class ReferralItemResources(
    override val item: SmilesHistoryItem.ReferralItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_referral

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

@Parcelize
internal data class QuestionOfTheDayAnsweredItemResources(
    override val item: SmilesHistoryItem.QuestionOfTheDayAnsweredItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_question_of_the_day

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

@Parcelize
internal data class AmazonAccountLinkedItemResources(
    override val item: SmilesHistoryItem.AmazonAccountLinkedItem
) : ItemResources {

    override val titleRes: Int
        get() = R.string.smiles_history_amazon_account_linked

    override val drawableRes: Int
        get() = R.drawable.ic_star
}

internal object SmilesItemResourcesMapper {

    fun map(item: SmilesHistoryItem): ItemResources = when (item) {
        is SmilesHistoryItem.UnknownHistoryItem -> UnknownHistoryItemResources(item)
        is SmilesHistoryItem.BrushingSessionItem -> BrushingSessionItemResources(item)
        is SmilesHistoryItem.ChallengeCompletedItem -> ChallengeCompletedItemResources(item)
        is SmilesHistoryItem.PersonalChallengeCompletedItem -> PersonalChallengeCompletedItemResources(item)
        is SmilesHistoryItem.TierReachedItem -> UnknownHistoryItemResources(item) // not available on Hum for now
        is SmilesHistoryItem.SmilesRedeemedItem -> DiscountAppliedItemResources(item)
        is SmilesHistoryItem.SmilesTransferItem -> UnknownHistoryItemResources(item) // not available on Hum for now
        is SmilesHistoryItem.StreakCompletedItem -> ChallengeCompletedItemResources(item)
        is SmilesHistoryItem.AccountCreatedItem -> AccountCreatedItemResources(item)
        is SmilesHistoryItem.SmilesExpiredItem -> SmilesExpiredItemResources(item)
        is SmilesHistoryItem.ReferralItem -> ReferralItemResources(item)
        is SmilesHistoryItem.NotificationTappedItem -> ChallengeCompletedItemResources(item)
        is SmilesHistoryItem.QuestionOfTheDayAnsweredItem ->
            QuestionOfTheDayAnsweredItemResources(item)
        is SmilesHistoryItem.ActivityCompletedItem -> ActivityCompletedItemResources(item)
        is SmilesHistoryItem.ShortTaskItem -> ShortTaskCompletedItemResources(item)
        is SmilesHistoryItem.AmazonAccountLinkedItem -> AmazonAccountLinkedItemResources(item)
    }
}
