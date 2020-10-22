package com.kolibree.android.rewards.persistence

import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Completable
import org.junit.Test

class ChallengeProgressDaoTest {

    private val challengeProgressDao = spy(StubChallengeProgressDao())

    /*
    REPLACE
     */
    @Test
    fun `replace does nothing if ChallengeProgressProfileCatalogInternal is empty`() {
        challengeProgressDao.replace(ChallengeProgressProfileCatalogInternal())

        verify(challengeProgressDao, never()).truncate(any())
        verify(challengeProgressDao, never()).insertAll(any())
    }

    @Test
    fun `replace invokes truncate for each profileId`() {
        val profileId1 = 9L
        val profileId2 = 77L
        challengeProgressDao.replace(
            ChallengeProgressProfileCatalogInternal().apply {
                add(createChallengeProgressInternal(profileId1))
                add(createChallengeProgressInternal(profileId2))
            }
        )

        verify(challengeProgressDao).truncate(profileId1)
        verify(challengeProgressDao).truncate(profileId2)
    }

    @Test
    fun `replace invokes truncate and insertAll in order`() {
        val profileId = 9L
        val expectedCatalog = ChallengeProgressProfileCatalogInternal().apply {
            add(createChallengeProgressInternal(profileId))
        }

        challengeProgressDao.replace(expectedCatalog)

        inOrder(challengeProgressDao) {
            verify(challengeProgressDao).truncate(profileId)
            verify(challengeProgressDao).insertAll(expectedCatalog)
        }
    }

    private fun createChallengeProgressInternal(profileId: Long) = ChallengeProgressEntity(
        challengeId = 0,
        completionDetails = null,
        completionTime = null,
        percentage = 0,
        profileId = profileId
    )

    private class StubChallengeProgressDao : ChallengeProgressDao() {
        override fun insertAll(challengeProgressProfileCatalog: ChallengeProgressProfileCatalogInternal) {
        }

        override fun truncate(profileId: Long) {
        }

        override fun truncate(): Completable = Completable.complete()
    }
}
