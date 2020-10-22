package com.kolibree.android.network.token

import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.network.api.response.RefreshTokenResponse
import io.reactivex.Single
import javax.inject.Inject

internal interface TokenRefresher {

    fun refreshToken(accessToken: String, refreshToken: String): Single<RefreshTokenProvider>
}

internal class TokenRefresherImpl @Inject constructor(private val tokenApi: TokenApi) :
    TokenRefresher {

    override fun refreshToken(
        accessToken: String,
        refreshToken: String
    ): Single<RefreshTokenProvider> {
        return tokenApi.refreshToken(accessToken = accessToken, refreshToken = refreshToken)
            .map {
                RefreshTokenResponse(it.string())
            }
    }
}
