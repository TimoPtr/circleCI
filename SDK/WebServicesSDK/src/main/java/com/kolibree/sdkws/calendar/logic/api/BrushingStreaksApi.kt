/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.calendar.logic.api

import com.kolibree.android.calendar.logic.api.model.BrushingStreaksResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

internal interface BrushingStreaksApi {

    @GET("/v1/rewards/streaks/profile/list/{profileId}/")
    fun getStreaksForProfile(@Path("profileId") profileId: Long): Single<Response<BrushingStreaksResponse>>
}
