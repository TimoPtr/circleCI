/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.mocks.ProfileBuilder
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FeedbackEntityTest : BaseUnitTest() {
    /*
    NO SMILES EARNED
     */
    @Test
    fun `NoSmiles entity with smiles 0 and without challenges or tier id is valid`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            smilesEarned = 0,
            historyEventDateTime = TrustedClock.getNowZonedDateTime()
        )

        assertTrue(entity.isNoSmilesEarned())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isTierReached())
        assertFalse(entity.isOfflineSync())
    }

    /*
    SMILES EARNED
     */
    @Test
    fun `SmilesEarned entity without challenges or tier id is valid`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            smilesEarned = 4,
            historyEventDateTime = TrustedClock.getNowZonedDateTime()
        )

        assertTrue(entity.isSmilesEarned())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isTierReached())
        assertFalse(entity.isOfflineSync())
    }

    @Test(expected = IllegalStateException::class)
    fun `SmilesEarned entity with challenges is invalid`() {
        FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            smilesEarned = 4,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L)
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `SmilesEarned entity with tier is invalid`() {
        FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            smilesEarned = 4,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            tierReached = 5
        )
    }

    /*
    CHALLENGE COMPLETED
     */

    @Test
    fun `ChallengeCompleted entity with challenges is challengeCompleted`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L)
        )

        assertTrue(entity.isChallengesCompleted())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isTierReached())
        assertFalse(entity.isOfflineSync())
    }

    @Test
    fun `ChallengeCompleted entity with multiple challenges is challengeCompleted`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L, 2L)
        )

        assertTrue(entity.isChallengesCompleted())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isTierReached())
        assertFalse(entity.isOfflineSync())
    }

    @Test(expected = IllegalStateException::class)
    fun `ChallengeCompleted entity with smilesEarned is invalid`() {
        FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L),
            smilesEarned = 6
        )
    }

    /*
    TIER REACHED
     */

    @Test
    fun `TierReached entity with challenges and tierReached is TierReached`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L),
            tierReached = 1
        )

        assertTrue(entity.isTierReached())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isOfflineSync())
    }

    @Test
    fun `TierReached entity with multiple challenges and tierReached is TierReached`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L, 2L),
            tierReached = 1
        )

        assertTrue(entity.isTierReached())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isOfflineSync())
    }

    @Test(expected = IllegalStateException::class)
    fun `TierReached entity with challenges, tierReached and smilesEarned is invalid`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            challengesCompleted = listOf(1L),
            tierReached = 1,
            smilesEarned = 54
        )

        assertTrue(entity.isTierReached())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isSmilesEarned())
        assertFalse(entity.isOfflineSync())
    }

    @Test(expected = IllegalStateException::class)
    fun `TierReached entity without challenges is invalid`() {
        FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            historyEventDateTime = TrustedClock.getNowZonedDateTime(),
            tierReached = 1
        )
    }

    /*
    OFFLINE BRUSHINGS SYNCED
     */
    @Test
    fun `OfflineSync entity with offlineBrushings gt 0 and without challenges or tier id is valid`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            offlineSyncBrushings = 2,
            historyEventDateTime = TrustedClock.getNowZonedDateTime()
        )

        assertTrue(entity.isOfflineSync())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isTierReached())
    }

    @Test
    fun `OfflineSync entity with offlineBrushings and smiles gt 0 and without challenges or tier id is valid`() {
        val entity = FeedbackEntity(
            profileId = ProfileBuilder.DEFAULT_ID,
            offlineSyncBrushings = 2,
            smilesEarned = 10,
            historyEventDateTime = TrustedClock.getNowZonedDateTime()
        )

        assertTrue(entity.isOfflineSync())
        assertFalse(entity.isChallengesCompleted())
        assertFalse(entity.isTierReached())
    }
}
