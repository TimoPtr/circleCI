/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.api.GameProgressApi
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressRequest
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressResponse
import com.kolibree.android.game.gameprogress.data.api.model.ProfileGameProgressResponse
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.exceptions.EmptyBodyException
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import org.junit.Test
import retrofit2.Response

internal class GameProgressSynchronizableApiTest : BaseUnitTest() {

    private lateinit var gameProgressSynchronizableApi: GameProgressSynchronizableApi

    private val api = mock<GameProgressApi>()

    private val connector = mock<IKolibreeConnector>()

    override fun setup() {
        super.setup()

        gameProgressSynchronizableApi = spy(GameProgressSynchronizableApi(api, connector))
    }

    @Test
    fun `get returns SynchronizableItem when response isSucessful and body is not null`() {
        val response = mock<Response<ProfileGameProgressResponse>>()
        val profileGameProgressResponse = ProfileGameProgressResponse(1, 2, emptyList())
        whenever(connector.accountId).thenReturn(0)
        whenever(api.getProfileGameProgress(0, 1)).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(profileGameProgressResponse)

        gameProgressSynchronizableApi.get(1)
    }

    @Test(expected = EmptyBodyException::class)
    fun `get throws EmptyBodyException when response isSucessful and body is null`() {
        val response = mock<Response<ProfileGameProgressResponse>>()
        whenever(connector.accountId).thenReturn(0)
        whenever(api.getProfileGameProgress(0, 1)).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(null)

        gameProgressSynchronizableApi.get(1)
    }

    @Test(expected = ApiError::class)
    fun `get throws ApiError when response not isSucessful`() {
        val response = mock<Response<ProfileGameProgressResponse>>()
        whenever(connector.accountId).thenReturn(0)
        whenever(api.getProfileGameProgress(0, 1)).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(false)
        whenever(response.errorBody()).thenReturn(null)

        gameProgressSynchronizableApi.get(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createOrEdit throws IllegalArgumentException when synchronizable is not ProfileGameProgressSynchronizableItem`() {
        gameProgressSynchronizableApi.createOrEdit(mock())
    }

    @Test
    fun `createOrEdit invokes createOrEditInternal when synchronizable is ProfileGameProgressSynchronizableItem`() {
        val item = mock<ProfileGameProgressSynchronizableItem>()

        doReturn(mock<SynchronizableItem>()).whenever(gameProgressSynchronizableApi).createOrEditInternal(item)

        gameProgressSynchronizableApi.createOrEdit(item)

        verify(gameProgressSynchronizableApi).createOrEditInternal(item)
    }

    @Test
    fun `createOrEditInternal invokes gameProgressApi setGameProgress for each GameProgress`() {
        val gameProgress = listOf(
            GameProgress("1", "", TrustedClock.getNowZonedDateTimeUTC()),
            GameProgress("2", "", TrustedClock.getNowZonedDateTimeUTC())
        )

        val item = ProfileGameProgressSynchronizableItem(1, gameProgress)
        val response = mock<Response<GameProgressResponse>>()
        val gameProgressResponse = GameProgressResponse("hello", "rock", TrustedClock.getNowZonedDateTimeUTC())
        whenever(connector.accountId).thenReturn(0)
        whenever(api.setGameProgress(any(), any(), any(), any())).thenReturn(Single.just(response))
        whenever(response.isSuccessful).thenReturn(true)
        whenever(response.body()).thenReturn(gameProgressResponse)
        val result = gameProgressSynchronizableApi.createOrEditInternal(item) as ProfileGameProgressSynchronizableItem

        gameProgress.forEach {
            verify(api).setGameProgress(0, 1, it.gameId, GameProgressRequest(it.progress))
        }

        result.gameProgress.forEach {
            assertEquals(gameProgressResponse.toDomainGameProgress(), it)
        }

        assertEquals(1, result.profileId)
    }
}
