package com.kolibree.android.network.api.response

import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.annotation.VisibleForApp
import org.json.JSONException
import org.json.JSONObject

/** Created by aurelien on 18/01/16.  */
@VisibleForApp
data class RefreshTokenResponse(
    private val accessToken: String,
    private val refreshToken: String
) : RefreshTokenProvider {

    constructor(json: JSONObject) : this(
        json.getString(FIELD_ACCESS_TOKEN),
        json.optString(FIELD_REFRESH_TOKEN, "")
    )

    @Throws(JSONException::class)
    constructor(raw: String) : this(JSONObject(raw))

    override fun getAccessToken(): String = accessToken

    override fun getRefreshToken(): String = refreshToken
}

private const val FIELD_ACCESS_TOKEN = "access_token"
private const val FIELD_REFRESH_TOKEN = "refresh_token"
