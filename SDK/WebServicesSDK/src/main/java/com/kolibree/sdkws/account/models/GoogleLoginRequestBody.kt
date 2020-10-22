/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import androidx.annotation.VisibleForTesting
import com.google.gson.annotations.SerializedName
import com.kolibree.sdkws.data.request.CreateAccountData

internal data class GoogleLoginRequestBody @VisibleForTesting constructor(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("email") val email: String,
    @SerializedName("google_id") val googleId: String,
    @SerializedName("google_id_token") val googleIdToken: String
) {

    companion object {

        fun createFrom(packageName: String, data: CreateAccountData): GoogleLoginRequestBody {
            return GoogleLoginRequestBody(
                packageName = packageName,
                email = requireNotNull(data.email),
                googleId = requireNotNull(data.googleId),
                googleIdToken = requireNotNull(data.googleIdToken)
            )
        }
    }
}
