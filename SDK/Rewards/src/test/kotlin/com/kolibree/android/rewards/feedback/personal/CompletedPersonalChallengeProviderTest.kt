/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback.personal

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.persistence.SmilesHistoryEventsDao
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CompletedPersonalChallengeProviderTest : BaseUnitTest() {

    private lateinit var provider: CompletedPersonalChallengeProvider

    private val smilesHistoryEventsDao: SmilesHistoryEventsDao = mock()

    override fun setup() {
        super.setup()

        provider = CompletedPersonalChallengeProvider(smilesHistoryEventsDao)
    }

    @Test
    fun `provide() reads personal challenge ids from smileHistory dao`() {
        val id1 = toPersonalChallengeId(12L)
        val id2 = toPersonalChallengeId(404L)
        val ids = listOf(id1, id2)
        whenever(smilesHistoryEventsDao.read(ids)).thenReturn(emptyList())

        provider.provide(ids)

        verify(smilesHistoryEventsDao).read(listOf(12L, 404L))
    }

    @Test
    fun `isPersonalChallengeId checks if id is from personal challenges range`() {
        assertTrue(isPersonalChallengeId(PERSONAL_CHALLENGE_START_ID + 123L))
        assertTrue(isPersonalChallengeId(PERSONAL_CHALLENGE_START_ID + 1L))
        assertTrue(isPersonalChallengeId(PERSONAL_CHALLENGE_START_ID + 52535L))
        assertFalse(isPersonalChallengeId(123L))
        assertFalse(isPersonalChallengeId(1L))
        assertFalse(isPersonalChallengeId(56789L))
    }

    @Test
    fun `toPersonalChallengeId increase id to personal challenge range`() {
        assertEquals(PERSONAL_CHALLENGE_START_ID + 1234L, toPersonalChallengeId(1234L))
        assertEquals(PERSONAL_CHALLENGE_START_ID + 2020L, toPersonalChallengeId(2020))
    }

    @Test
    fun `toIdFromPersonalChallengeId returns original id`() {
        assertEquals(
            1234L,
            toIdFromPersonalChallengeId(PERSONAL_CHALLENGE_START_ID + 1234L)
        )
        assertEquals(
            2020L,
            toIdFromPersonalChallengeId(PERSONAL_CHALLENGE_START_ID + 2020)
        )
    }

    @Test
    fun `id operations are transitive`() {
        val id = 234L
        assertFalse(isPersonalChallengeId(id))

        val personalChallengeId = toPersonalChallengeId(id)
        assertTrue(isPersonalChallengeId(personalChallengeId))

        assertEquals(id, toIdFromPersonalChallengeId(personalChallengeId))
    }
}
