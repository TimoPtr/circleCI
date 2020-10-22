package com.kolibree.sdkws.api.request

import com.kolibree.sdkws.Constants
import com.kolibree.sdkws.data.model.RefreshTokenData
import com.kolibree.sdkws.networking.RequestMethod
import java.util.Locale

internal class RefreshTokenRequest(data: RefreshTokenData) : Request(
    RequestMethod.GET,
    String.format(
        Locale.US,
        Constants.SERVICE_REFRESH_TOKEN_V3,
        data.accessToken,
        data.refreshToken
    )
)
