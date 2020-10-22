/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.data

import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

internal interface BatteryLevelApi {

    @POST("/v4/accounts/{accountId}/profiles/{profileId}/battery_level/")
    fun sendBatteryLevel(
        @Path("accountId") accountId: Long,
        @Path("profileId") profileId: Long,
        @Body body: SendBatteryLevelRequest
    ): Single<Response<ResponseBody>>
}
