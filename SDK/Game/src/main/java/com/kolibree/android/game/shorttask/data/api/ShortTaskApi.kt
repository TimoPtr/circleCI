/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.api

import com.kolibree.android.game.shorttask.data.api.model.ShortTaskRequest
import com.kolibree.android.game.shorttask.data.api.model.ShortTaskResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface ShortTaskApi {

    @POST("/v1/short_tasks/{accountId}/{profileId}/short_task/")
    fun createShortTask(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body request: ShortTaskRequest
    ): Call<ShortTaskResponse>
}
