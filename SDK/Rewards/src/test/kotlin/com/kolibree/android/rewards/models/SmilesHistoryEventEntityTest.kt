package com.kolibree.android.rewards.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.rewards.test.createSmilesHistoryEventEntity
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmilesHistoryEventEntityTest : BaseUnitTest() {

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    @Test
    fun `for type EVENT_TYPE_CHALLENGE_COMPLETED creates ChallengeCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_CHALLENGE_COMPLETED)
        assertTrue(entity is ChallengeCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION and OFFLINE_BRUSHING_TYPE_SUCCESS  creates OfflineBrushingSessionHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_BRUSHING_SESSION, OFFLINE_BRUSHING_TYPE_SUCCESS)
        assertTrue(entity is OfflineBrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.COMPLETED,
            (entity as OfflineBrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT and OFFLINE_BRUSHING_TYPE_FAILURE  creates OfflineBrushingSessionHistoryEvent`() {
        val entity =
            createEntity(EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT, OFFLINE_BRUSHING_TYPE_FAILURE)
        assertTrue(entity is OfflineBrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.DAILY_LIMIT_REACH,
            (entity as OfflineBrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE and OFFLINE_BRUSHING_TYPE_SUCCESS  creates OfflineBrushingSessionHistoryEvent`() {
        val entity =
            createEntity(EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE, OFFLINE_BRUSHING_TYPE_SUCCESS)
        assertTrue(entity is OfflineBrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.INCOMPLETE,
            (entity as OfflineBrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION and OFFLINE_BRUSHING_TYPE_FAILURE  creates OfflineBrushingSessionHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_BRUSHING_SESSION, OFFLINE_BRUSHING_TYPE_FAILURE)
        assertTrue(entity is OfflineBrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.COMPLETED,
            (entity as OfflineBrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION and not offline creates OfflineBrushingSessionHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_BRUSHING_SESSION, "Coach+")
        assertTrue(entity is BrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.COMPLETED,
            (entity as BrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT and not offline creates OfflineBrushingSessionHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT, "Coach+")
        assertTrue(entity is BrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.DAILY_LIMIT_REACH,
            (entity as BrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE and not offline creates OfflineBrushingSessionHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE, "Coach+")
        assertTrue(entity is BrushingSessionHistoryEvent)
        assertEquals(
            BrushingSessionHistoryEventStatus.INCOMPLETE,
            (entity as BrushingSessionHistoryEvent).status
        )
    }

    @Test
    fun `for type EVENT_TYPE_TIER_REACHED and not offline creates TierReachedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_TIER_REACHED)
        assertTrue(entity is TierReachedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_SMILES_REDEEMED and not offline creates SmilesRedeemedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_SMILES_REDEEMED)
        assertTrue(entity is SmilesRedeemedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_SMILES_TRANSFER and not offline creates SmilesTransferHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_SMILES_TRANSFER)
        assertTrue(entity is SmilesTransferHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_CROWN_COMPLETED and not offline creates CrownCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_CROWN_COMPLETED)
        assertTrue(entity is CrownCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_STREAK_COMPLETED and not offline creates StreakCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_STREAK_COMPLETED)
        assertTrue(entity is StreakCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_PERSONAL_CHALLENGE_COMPLETED and not offline creates ChallengeCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_PERSONAL_CHALLENGE_COMPLETED)
        assertTrue(entity is PersonalChallengeCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_SMILES_EXPIRED and not offline creates SmilesExpiredHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_SMILES_EXPIRED)
        assertTrue(entity is SmilesExpiredHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_REFERRAL and not offline creates ReferralHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_REFERRAL)
        assertTrue(entity is ReferralHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_NOTIFICATION_TAPPED and not offline creates NotificationTappedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_NOTIFICATION_TAPPED)
        assertTrue(entity is NotificationTappedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_QUESTION_OF_THE_DAY_ANSWERED and not offline creates QuestionOfTheDayAnsweredHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_QUESTION_OF_THE_DAY_ANSWERED)
        assertTrue(entity is QuestionOfTheDayAnsweredHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_SHORT_TASK_COMPLETED and not offline creates ShortTaskCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_SHORT_TASK_COMPLETED)
        assertTrue(entity is ShortTaskCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_TYPE_ACTIVITY_COMPLETED and not offline creates ActivityCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_TYPE_ACTIVITY_COMPLETED)
        assertTrue(entity is ActivityCompletedHistoryEvent)
    }

    @Test
    fun `for type EVENT_AMAZON_LINK_ACCOUNT creates ActivityCompletedHistoryEvent`() {
        val entity = createEntity(EVENT_AMAZON_LINK_ACCOUNT)
        assertTrue(entity is AmazonAccountLinkedEvent)
    }

    @Test
    fun `BrushingSessionHistoryEventStatus maps correctly COMPLETED`() {
        assertEquals(
            BrushingSessionHistoryEventStatus.COMPLETED,
            BrushingSessionHistoryEventStatus.fromEventType(EVENT_TYPE_BRUSHING_SESSION)
        )
    }

    @Test
    fun `BrushingSessionHistoryEventStatus maps correctly DAILY_LIMIT_REACH`() {
        assertEquals(
            BrushingSessionHistoryEventStatus.DAILY_LIMIT_REACH,
            BrushingSessionHistoryEventStatus.fromEventType(EVENT_TYPE_BRUSHING_SESSION_DAILY_LIMIT)
        )
    }

    @Test
    fun `BrushingSessionHistoryEventStatus maps correctly INCOMPLETE`() {
        assertEquals(
            BrushingSessionHistoryEventStatus.INCOMPLETE,
            BrushingSessionHistoryEventStatus.fromEventType(EVENT_TYPE_BRUSHING_SESSION_INCOMPLETE)
        )
    }

    @Test
    fun `BrushingSessionHistoryEventStatus maps correctly Unknown key to COMPLETED`() {
        assertEquals(
            BrushingSessionHistoryEventStatus.COMPLETED,
            BrushingSessionHistoryEventStatus.fromEventType("hello")
        )
    }

    private fun createEntity(eventType: String, brushingType: String = "default") =
        createSmilesHistoryEventEntity(
            challengeId = 12L,
            brushingId = 13L,
            brushingType = brushingType,
            eventType = eventType,
            tierLevel = 3,
            rewardsId = 15L,
            relatedProfileId = 5L
        ).toSpecificEvent()
}
