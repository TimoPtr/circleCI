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

internal data class GoogleSignUpRequestBody @VisibleForTesting constructor(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("country") val country: String,
    @SerializedName("email") val email: String,
    @SerializedName("beta") val isBetaAccount: Boolean,
    @SerializedName("parental_consent") val parentalConsentGiven: Boolean,
    @SerializedName("google_id") val googleId: String,
    @SerializedName("google_id_token") val googleIdToken: String
) {

    companion object {

        fun createFrom(packageName: String, data: CreateAccountData): GoogleSignUpRequestBody {
            return GoogleSignUpRequestBody(
                packageName = packageName,
                firstName = requireNotNull(data.firstName),
                country = requireNotNull(data.country),
                email = requireNotNull(data.email),
                isBetaAccount = requireNotNull(data.isBetaAccount),
                parentalConsentGiven = requireNotNull(data.parentalConsentGiven),
                googleId = requireNotNull(data.googleId),
                googleIdToken = requireNotNull(data.googleIdToken)
            )
        }
    }
}
