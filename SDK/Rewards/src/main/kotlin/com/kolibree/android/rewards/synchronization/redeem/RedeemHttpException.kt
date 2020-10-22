/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import com.kolibree.android.annotation.VisibleForApp
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

private const val FIELD_ERROR = "error"

@VisibleForApp
class RedeemHttpException(response: Response<RedeemApi>) : HttpException(response) {

    val userDisplayMessage: String? = response.errorBody()?.let {
        try {
            JSONObject(it.string()).optString(FIELD_ERROR, null)
        } catch (e: JSONException) {
            Timber.e(e)
            null
        }
    }
}
