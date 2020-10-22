/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.push

import androidx.annotation.Keep
import com.kolibree.sdkws.Constants.SERVICE_BASE_ACCOUNT_URL
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/*
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755030/Device+Token
 */
@Keep
interface PushNotificationApi {

    @POST("$SERVICE_BASE_ACCOUNT_URL/{accountId}/deviceToken/")
    fun updatePushNotificationToken(
        @Path("accountId") accountId: Long,
        @Body body: PushNotificationTokenRequestBody
    ): Single<Response<Void>>
}
