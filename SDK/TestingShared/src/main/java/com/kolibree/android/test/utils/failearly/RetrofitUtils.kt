/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.failearly

import androidx.annotation.Keep
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

@Keep
inline fun <reified T : Any> retrofitError(httpErrorCode: Int): Response<T> {
    return Response.error(
        httpErrorCode,
        ResponseBody.create(MediaType.parse("application/json"), "")
    )
}
