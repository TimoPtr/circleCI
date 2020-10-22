/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.domain.logic

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import java.util.UUID
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class GameProgressRepositoryImplTest : BaseUnitTest() {

    private lateinit var repository: GameProgressRepositoryImpl

    private val dao = mock<GameProgressDao>()
    private val synchronizator = mock<Synchronizator>()

    override fun setup() {
        super.setup()

        repository = spy(GameProgressRepositoryImpl(dao, synchronizator))
    }

    @Test
    fun `saveProgress invokes synchronizator complete and updateGameProgress`() {
        doReturn(Single.just(mock<SynchronizableItem>())).whenever(repository).updateGameProgress(any(), any())
        whenever(synchronizator.synchronizeCompletable()).thenReturn(Completable.complete())

        repository.saveProgress(1, "gameId", "progress").test().assertComplete()

        verify(repository).updateGameProgress(any(), any())
        verify(synchronizator).synchronizeCompletable()
    }

    @Test
    fun `updateGameProgress invokes synchronizator create if no uuid can be found`() {
        TrustedClock.setFixedDate()
        val expectedProfileID = 1L
        val expectedDateTime = TrustedClock.getNowZonedDateTimeUTC()
        val expectedProgress = GameProgress("gameID", "progress", expectedDateTime)
        val singleResult = Single.just(mock<SynchronizableItem>())

        whenever(dao.getGameProgressEntitiesForProfile(expectedProfileID)).thenReturn(emptyList())
        whenever(synchronizator.create(any())).thenReturn(singleResult)

        assertEquals(singleResult, repository.updateGameProgress(expectedProfileID, expectedProgress))

        verify(dao).getGameProgressEntitiesForProfile(expectedProfileID)

        verify(repository).pendingProfileGameProgress(eq(expectedProfileID), any(), eq(null))
        verify(synchronizator).create(any())
        verify(synchronizator, never()).update(any())
    }

    @Test
    fun `updateGameProgress invokes synchronizator update if uuid can be found`() {
        TrustedClock.setFixedDate()
        val expectedProfileID = 1L
        val expectedGameId = "hello world"
        val expectedUUID = mock<UUID>()
        val expectedDateTime = TrustedClock.getNowZonedDateTimeUTC()
        val expectedProgress = GameProgress(expectedGameId, "progress", expectedDateTime)
        val singleResult = Single.just(mock<SynchronizableItem>())
        val existingGameProgress =
            GameProgressEntity(expectedProfileID, expectedGameId, "no progress", expectedDateTime, expectedUUID)

        whenever(dao.getGameProgressEntitiesForProfile(expectedProfileID)).thenReturn(listOf(existingGameProgress))
        whenever(synchronizator.update(any())).thenReturn(singleResult)

        assertEquals(singleResult, repository.updateGameProgress(expectedProfileID, expectedProgress))

        verify(dao).getGameProgressEntitiesForProfile(expectedProfileID)

        val captor = argumentCaptor<List<GameProgress>>()
        verify(repository).pendingProfileGameProgress(eq(expectedProfileID), captor.capture(), eq(expectedUUID))
        val updatedGameProgress = captor.firstValue
        assertEquals(1, updatedGameProgress.size)
        assertEquals(expectedProgress, updatedGameProgress[0])
        verify(synchronizator, never()).create(any())
        verify(synchronizator).update(any())
    }

    @Test
    fun `getProgress returns empty GameProgress when dao getGameProgressForProfileAndGame returns null`() {
        TrustedClock.setFixedDate()
        val expectedProfileID = 1L
        val expectedDateTime = TrustedClock.getNowZonedDateTimeUTC()
        val expectedGameId = "hello world"

        whenever(dao.getGameProgressEntityForProfileAndGame(expectedProfileID, expectedGameId)).thenReturn(null)

        val result = repository.getProgress(expectedProfileID, expectedGameId)

        assertEquals(expectedGameId, result.gameId)
        assertEquals("", result.progress)
        assertEquals(expectedDateTime, result.updatedAt)

        verify(dao).getGameProgressEntityForProfileAndGame(expectedProfileID, expectedGameId)
    }

    @Test
    fun `getProgress returns GameProgress from dao when not null`() {
        TrustedClock.setFixedDate()
        val expectedProfileID = 1L
        val expectedDateTime = TrustedClock.getNowZonedDateTimeUTC()
        val expectedGameId = "hello world"
        val expectedProgress = "progress"
        val expectedUUID = mock<UUID>()
        val expectedGameProgress =
            spy(GameProgressEntity(expectedProfileID, expectedGameId, expectedProgress, expectedDateTime, expectedUUID))

        whenever(dao.getGameProgressEntityForProfileAndGame(expectedProfileID, expectedGameId)).thenReturn(
            expectedGameProgress
        )

        val result = repository.getProgress(expectedProfileID, expectedGameId)

        assertEquals(expectedGameProgress.toGameProgress(), result)

        verify(dao).getGameProgressEntityForProfileAndGame(expectedProfileID, expectedGameId)
    }
}
