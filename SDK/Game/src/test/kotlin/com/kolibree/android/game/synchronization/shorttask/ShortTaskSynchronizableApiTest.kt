/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.shorttask.data.api.ShortTaskApi
import com.kolibree.android.game.shorttask.data.api.model.ShortTaskRequest
import com.kolibree.android.game.shorttask.data.api.model.ShortTaskResponse
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

internal class ShortTaskSynchronizableApiTest : BaseUnitTest() {

    private val api: ShortTaskApi = mock()
    private val connector: IKolibreeConnector = mock()

    private lateinit var syncApi: ShortTaskSynchronizableApi

    override fun setup() {
        super.setup()
        syncApi = ShortTaskSynchronizableApi(api, connector)
    }

    @Test(expected = IllegalAccessException::class)
    fun `get throws IllegalAccessException`() {
        syncApi.get(1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `createOrEdit throws when it is not a ShortTaskSynchronizableItem`() {
        syncApi.createOrEdit(mock())
    }

    @Test
    fun `createOrEdit only creates new item`() {
        val item = ShortTaskSynchronizableItem(
            ShortTask.TEST_YOUR_ANGLE,
            1,
            TrustedClock.getNowZonedDateTime(),
            TrustedClock.getNowZonedDateTime()
        )
        val accountId = 10L
        val response = mock<Response<ShortTaskResponse>>()
        val call = mock<Call<ShortTaskResponse>>()
        whenever(connector.accountId).thenReturn(accountId)
        whenever(response.isSuccessful).thenReturn(true)
        whenever(
            api.createShortTask(
                accountId,
                item.profileId,
                ShortTaskRequest(item.shortTask.internalValue, item.createdAt.toOffsetDateTime())
            )
        ).thenReturn(call)
        whenever(call.execute()).thenReturn(response)

        assertEquals(item, syncApi.createOrEdit(item))
    }
}
