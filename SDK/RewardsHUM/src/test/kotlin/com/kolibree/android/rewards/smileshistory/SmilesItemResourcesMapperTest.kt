/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import android.os.Parcel
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

internal class SmilesItemResourcesMapperTest : BaseUnitTest() {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private fun createStubItemRes(smiles: Int, date: ZonedDateTime): ItemResources =
        object : ItemResources {
            override val item: SmilesHistoryItem
                get() = SmilesHistoryItem.ReferralItem(smiles, date)
            override val titleRes: Int
                get() = 101
            override val drawableRes: Int
                get() = 102

            override fun writeToParcel(dest: Parcel?, flags: Int) {
                error("not impl")
            }

            override fun describeContents(): Int {
                error("not impl")
            }
        }

    @Test
    fun `ItemResources default fields with positive smiles amount are well defined`() {
        val date = TrustedClock.getNowZonedDateTime()

        val res = createStubItemRes(50, date)

        assertEquals("+50", res.smiles)
        assertFalse(res.isInfoAvailable)
        assertFalse(res.isSmilesSpent)
        assertFalse(res.noSmilesChange)
        assertTrue(res.isSmilesAwarded)
        assertEquals(
            date.withZoneSameInstant(TrustedClock.systemZone),
            res.creationDateTime
        )
        assertEquals(
            res.creationDateTime.format(
                dateFormatter
            ),
            res.creationDate
        )
        assertEquals(
            res.creationDateTime.format(
                timeFormatter
            ),
            res.creationTime
        )
        assertEquals(R.string.empty, res.infoRes)
    }
    @Test
    fun `ItemResources default fields with no smiles change are well defined`() {
        val date = TrustedClock.getNowZonedDateTime()

        val res = createStubItemRes(0, date)

        assertEquals("+0", res.smiles)
        assertFalse(res.isInfoAvailable)
        assertFalse(res.isSmilesSpent)
        assertTrue(res.noSmilesChange)
        assertFalse(res.isSmilesAwarded)
        assertEquals(
            date.withZoneSameInstant(TrustedClock.systemZone),
            res.creationDateTime
        )
        assertEquals(
            res.creationDateTime.format(
                dateFormatter
            ),
            res.creationDate
        )
        assertEquals(
            res.creationDateTime.format(
                timeFormatter
            ),
            res.creationTime
        )
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ItemResources default fields with negative smiles amount are well defined`() {
        val date = TrustedClock.getNowZonedDateTime()

        val res = createStubItemRes(-50, date)

        assertEquals("-50", res.smiles)
        assertFalse(res.isInfoAvailable)
        assertTrue(res.isSmilesSpent)
        assertFalse(res.noSmilesChange)
        assertFalse(res.isSmilesAwarded)
        assertEquals(
            date.withZoneSameInstant(TrustedClock.systemZone),
            res.creationDateTime
        )
        assertEquals(
            res.creationDateTime.format(
                dateFormatter
            ),
            res.creationDate
        )
        assertEquals(
            res.creationDateTime.format(
                timeFormatter
            ),
            res.creationTime
        )
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `UnknownHistoryItem well mapped`() {
        val item = SmilesHistoryItem.UnknownHistoryItem
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(UnknownHistoryItemResources(item), res)
        assertEquals(R.string.empty, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem Coach well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0, TrustedClock.getNowZonedDateTime(), Game.COACH,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_item_coach_brushing, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem CoachPlus well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.COACH_PLUS,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_guided_brushing, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem GoPirate well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.GO_PIRATE,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_go_pirate, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem TestBrushing well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.TEST_BRUSHING,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_test_brushing, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem Rabbids well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.RABBIDS,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_rabbids, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem TestAngles well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.TEST_ANGLES,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_test_angle, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem SpeedControl well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.SPEED_CONTROL,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_mind_your_speed, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem Offline well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                Game.OFFLINE,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_offline_brushing, res.titleRes)
        assertEquals(R.drawable.ic_offline_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem null well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0, TrustedClock.getNowZonedDateTime(), null,
                BrushingSessionItemStatus.COMPLETED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_offline_brushing, res.titleRes)
        assertEquals(R.drawable.ic_offline_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `BrushingSessionItem incomplete well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0, TrustedClock.getNowZonedDateTime(), null,
                BrushingSessionItemStatus.INCOMPLETE
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_offline_brushing, res.titleRes)
        assertEquals(R.drawable.ic_not_completed_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.smiles_history_not_enough_coverage, res.infoRes)
        assertTrue(res.isInfoAvailable)
    }

    @Test
    fun `BrushingSessionItem daily limit reach well mapped`() {
        val item =
            SmilesHistoryItem.BrushingSessionItem(
                0, TrustedClock.getNowZonedDateTime(), null,
                BrushingSessionItemStatus.DAILY_LIMIT_REACH
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(BrushingSessionItemResources(item), res)
        assertEquals(R.string.smiles_history_offline_brushing, res.titleRes)
        assertEquals(R.drawable.ic_not_completed_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.smiles_history_daily_limit_reached, res.infoRes)
        assertTrue(res.isInfoAvailable)
    }

    @Test
    fun `ChallengeCompletedItem well mapped`() {
        val item =
            SmilesHistoryItem.ChallengeCompletedItem(0, TrustedClock.getNowZonedDateTime(), "", "")
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ChallengeCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_more_way_to_earn_smiles, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `PersonalChallengeCompletedItem well mapped`() {
        val item =
            SmilesHistoryItem.PersonalChallengeCompletedItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                "",
                ""
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(PersonalChallengeCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_challenge_compled, res.titleRes)
        assertEquals(R.drawable.ic_challenge_completed, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `SmilesRedeemedItem well mapped`() {
        val item =
            SmilesHistoryItem.SmilesRedeemedItem(0, TrustedClock.getNowZonedDateTime(), "")
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(DiscountAppliedItemResources(item), res)
        assertEquals(R.string.smiles_history_discount_applied, res.titleRes)
        assertEquals(R.drawable.ic_discount_applied, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `AccountCreatedItem well mapped`() {
        val item =
            SmilesHistoryItem.AccountCreatedItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(AccountCreatedItemResources(item), res)
        assertEquals(R.string.smiles_history_account_created, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `SmilesExpiredItem well mapped`() {
        val item =
            SmilesHistoryItem.SmilesExpiredItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(SmilesExpiredItemResources(item), res)
        assertEquals(R.string.smiles_history_smiles_expired, res.titleRes)
        assertEquals(R.drawable.ic_discount_applied, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ReferralItem well mapped`() {
        val item =
            SmilesHistoryItem.ReferralItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ReferralItemResources(item), res)
        assertEquals(R.string.smiles_history_referral, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `QuestionOfTheDayAnsweredItem well mapped`() {
        val item =
            SmilesHistoryItem.QuestionOfTheDayAnsweredItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(QuestionOfTheDayAnsweredItemResources(item), res)
        assertEquals(R.string.smiles_history_question_of_the_day, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `TierReachedItem well mapped`() {
        val item =
            SmilesHistoryItem.TierReachedItem(0, TrustedClock.getNowZonedDateTime(), "", "", "")
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(UnknownHistoryItemResources(item), res)
        assertEquals(R.string.empty, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `SmilesTransferItem well mapped`() {
        val item =
            SmilesHistoryItem.SmilesTransferItem(0, TrustedClock.getNowZonedDateTime(), "")
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(UnknownHistoryItemResources(item), res)
        assertEquals(R.string.empty, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ActivityCompletedItem well mapped`() {
        val item =
            SmilesHistoryItem.ActivityCompletedItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ActivityCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_item_activity_completed, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `AmazonAccountLinkedItem well mapped`() {
        val item =
            SmilesHistoryItem.AmazonAccountLinkedItem(0, TrustedClock.getNowZonedDateTime())
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(AmazonAccountLinkedItemResources(item), res)
        assertEquals(R.string.smiles_history_amazon_account_linked, res.titleRes)
        assertEquals(R.drawable.ic_star, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ShortTaskItem Mind your speed well mapped`() {
        val item =
            SmilesHistoryItem.ShortTaskItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                ShortTask.MIND_YOUR_SPEED
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ShortTaskCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_mind_your_speed, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ShortTaskItem Test your angle well mapped`() {
        val item =
            SmilesHistoryItem.ShortTaskItem(
                0,
                TrustedClock.getNowZonedDateTime(),
                ShortTask.TEST_YOUR_ANGLE
            )
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ShortTaskCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_test_angle, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }

    @Test
    fun `ShortTaskItem unknown mapped`() {
        val item =
            SmilesHistoryItem.ShortTaskItem(0, TrustedClock.getNowZonedDateTime(), null)
        val res = SmilesItemResourcesMapper.map(item)
        assertEquals(ShortTaskCompletedItemResources(item), res)
        assertEquals(R.string.smiles_history_item_short_task_unknown, res.titleRes)
        assertEquals(R.drawable.ic_online_brushing, res.drawableRes)
        assertEquals(item, res.item)
        assertEquals(R.string.empty, res.infoRes)
    }
}
