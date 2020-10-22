/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.feedback.personal.CompletedPersonalChallengeProvider
import com.kolibree.android.rewards.feedback.personal.toPersonalChallengeId
import com.kolibree.android.rewards.persistence.ChallengesDao
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class CompletedChallengesProviderTest : BaseUnitTest() {

    private lateinit var provider: CompletedChallengesProvider

    private val personalChallengeProvider = mock<CompletedPersonalChallengeProvider>()

    private val challengesDao = mock<ChallengesDao>()

    override fun setup() {
        super.setup()

        provider = CompletedChallengesProvider(challengesDao, personalChallengeProvider)
    }

    @Test
    fun `provide() invokes challengeDao read`() {
        val ids = emptyList<Long>()
        whenever(challengesDao.read(ids)).thenReturn(emptyList())
        whenever(personalChallengeProvider.provide(ids)).thenReturn(emptyList())

        provider.provide(ids)

        verify(challengesDao).read(ids)
    }

    @Test
    fun `provide() invokes personalChallengeProvider provide`() {
        val ids = emptyList<Long>()
        whenever(challengesDao.read(ids)).thenReturn(emptyList())
        whenever(personalChallengeProvider.provide(ids)).thenReturn(emptyList())

        provider.provide(ids)

        verify(personalChallengeProvider).provide(ids)
    }

    @Test
    fun `provide() invokes challengesDao with appropriate ids`() {
        val ids = listOf(1L, 2L)
        val personalChallengeIds = listOf(toPersonalChallengeId(1L), toPersonalChallengeId(2L))
        val allIds = ids + personalChallengeIds
        whenever(challengesDao.read(ids)).thenReturn(emptyList())
        whenever(personalChallengeProvider.provide(personalChallengeIds)).thenReturn(emptyList())

        provider.provide(allIds)

        verify(challengesDao).read(ids)
    }

    @Test
    fun `provide() invokes personalChallengeProvider with appropriate ids`() {
        val ids = listOf(1L, 2L)
        val personalChallengeIds = listOf(toPersonalChallengeId(1L), toPersonalChallengeId(2L))
        val allIds = ids + personalChallengeIds
        whenever(challengesDao.read(ids)).thenReturn(emptyList())
        whenever(personalChallengeProvider.provide(personalChallengeIds)).thenReturn(emptyList())

        provider.provide(allIds)

        verify(personalChallengeProvider).provide(personalChallengeIds)
    }
}
