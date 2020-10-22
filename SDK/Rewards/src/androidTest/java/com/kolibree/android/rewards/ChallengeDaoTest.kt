/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.models.CategoryEntity
import com.kolibree.android.rewards.models.ChallengeEntity
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal
import com.kolibree.android.rewards.persistence.ChallengeWithProgressInternal
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChallengeDaoTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var database: RewardsRoomDatabase

    override fun setUp() {
        super.setUp()
        database = Room.inMemoryDatabaseBuilder(context(), RewardsRoomDatabase::class.java).build()
    }

    override fun tearDown() {
        super.tearDown()

        database.close()
    }

    @Test
    fun getChallengeProgressForProfile_empty() {
        val testObservable = database.challengesDao().challengeProgressForProfile(0L)
            .test()
            .assertNoErrors()

        assertTrue(testObservable.values().single().isEmpty())
    }

    @Test
    fun getChallengeProgressForProfile_challengeProgressOrdered() {
        val profileId = 0L
        val categoryName = "cat"

        val challenge1 = ChallengeEntity(
            1L,
            "challenge1",
            "greeting",
            "description",
            "url",
            101,
            null,
            categoryName
        )

        val challenge2 = ChallengeEntity(
            2L,
            "challenge2",
            "greeting",
            "description",
            "url",
            102,
            null,
            categoryName
        )

        val challengeProgress1 = ChallengeProgressEntity(challenge1.id, profileId, null, null, 100)
        val challengeProgress2 = ChallengeProgressEntity(challenge2.id, profileId, null, null, 100)

        val challengeProgressCatalog = ChallengeProgressProfileCatalogInternal().apply {
            add(challengeProgress2)
            add(challengeProgress1)
        }
        database.categoriesDao().insertAll(listOf(CategoryEntity(categoryName)))
        database.challengesDao().insertAll(listOf(challenge2, challenge1))
        database.challengeProgressDao().insertAll(challengeProgressCatalog)

        val testObservable =
            database.challengesDao().challengeProgressForProfile(profileId).test().awaitCount(1)

        testObservable.assertValue(
            listOf(
                ChallengeWithProgressInternal(
                    1L,
                    "challenge1",
                    "description",
                    "url",
                    categoryName,
                    "greeting",
                    101,
                    100,
                    null,
                    profileId,
                    null,
                    null
                ),
                ChallengeWithProgressInternal(
                    2L,
                    "challenge2",
                    "description",
                    "url",
                    categoryName,
                    "greeting",
                    102,
                    100,
                    null,
                    profileId,
                    null,
                    null
                )
            )
        )
        testObservable.assertNoErrors()
    }

    @Test
    fun read_ordered_challenge() {
        val categoryName = "cat"

        val challenge1 = ChallengeEntity(
            1L,
            "challenge1",
            "greeting",
            "description",
            "url",
            101,
            null,
            categoryName
        )

        val challenge2 = ChallengeEntity(
            2L,
            "challenge2",
            "greeting",
            "description",
            "url",
            102,
            null,
            categoryName
        )

        database.categoriesDao().insertAll(listOf(CategoryEntity(categoryName)))
        database.challengesDao().insertAll(listOf(challenge2, challenge1))

        assertEquals(listOf(challenge1, challenge2), database.challengesDao().read(listOf(2, 1)))
        assertEquals(listOf(challenge1, challenge2), database.challengesDao().read(listOf(1, 2)))
    }

    @Test
    fun read_challenge_empty() {
        assertTrue(database.challengesDao().read(listOf()).isEmpty())
    }

    @Test
    fun completedChallenges_empty_when_no_challenge() {
        val profileId = 0L

        val testObserver = database.challengesDao().completedChallenges(profileId).test()

        testObserver.assertValue(listOf())
    }

    @Test
    fun completedChallenges_ordered() {
        val profileId = 0L
        val categoryName = "cat"

        val challengeCompleted1 = ChallengeEntity(
            1L,
            "challengeCompleted1",
            "greeting",
            "description",
            "url",
            101,
            null,
            categoryName
        )

        val challengeCompleted2 = ChallengeEntity(
            2L,
            "challengeCompleted2",
            "greeting",
            "description",
            "url",
            102,
            null,
            categoryName
        )

        val challengeNotCompleted = ChallengeEntity(
            3L,
            "challengeNotCompleted",
            "greeting",
            "description",
            "url",
            102,
            null,
            categoryName
        )

        val challengeNotUnlockByThisProfile = ChallengeEntity(
            4L,
            "challengeNotUnlock",
            "greeting",
            "description",
            "url",
            102,
            null,
            categoryName
        )

        val challengeProgress1 = ChallengeProgressEntity(
            challengeCompleted1.id,
            profileId,
            TrustedClock.getNowZonedDateTime(),
            null,
            100
        )
        val challengeProgress2 = ChallengeProgressEntity(
            challengeCompleted2.id,
            profileId,
            TrustedClock.getNowZonedDateTime(),
            null,
            100
        )
        val challengeNotCompletedProgress =
            ChallengeProgressEntity(challengeNotCompleted.id, profileId, null, null, 10)
        val challengeNotUnlockByThisProfileProgress = ChallengeProgressEntity(
            challengeNotUnlockByThisProfile.id,
            1L,
            TrustedClock.getNowZonedDateTime(),
            null,
            100
        )

        val challengeProgressCatalog = ChallengeProgressProfileCatalogInternal().apply {
            add(challengeProgress2)
            add(challengeProgress1)
            add(challengeNotCompletedProgress)
            add(challengeNotUnlockByThisProfileProgress)
        }
        database.categoriesDao().insertAll(listOf(CategoryEntity(categoryName)))
        database.challengesDao().insertAll(
            listOf(
                challengeCompleted2,
                challengeNotCompleted,
                challengeCompleted1,
                challengeNotUnlockByThisProfile
            )
        )
        database.challengeProgressDao().insertAll(challengeProgressCatalog)

        val testObservable = database.challengesDao().completedChallenges(profileId).test()

        testObservable.assertValue(listOf(challengeCompleted1, challengeCompleted2))
        testObservable.assertNoErrors()
    }
}
